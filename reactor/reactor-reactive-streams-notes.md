# Project Reactor и Reactive Streams — конспект для собеседования

> Основано на референсе Reactor Core: *Reactive Programming* (Project Reactor).  
> Фокус: практическое понимание, типовые вопросы интервью, примеры кода.

---

## Содержание
- [1. Reactive Streams в двух словах](#1-reactive-streams-в-двух-словах)
- [2. Сигналы и контракт](#2-сигналы-и-контракт)
- [3. Publisher / Subscriber / Subscription / Processor](#3-publisher--subscriber--subscription--processor)
- [4. Flux vs Mono](#4-flux-vs-mono)
- [5. Создание Flux/Mono](#5-создание-fluxmono)
- [6. Подписка (subscribe) и управление demand](#6-подписка-subscribe-и-управление-demand)
- [7. Backpressure (обратное давление)](#7-backpressure-обратное-давление)
- [8. Основные операторы (map/filter/flatMap/concatMap/zip/merge/...)](#8-основные-операторы-mapfilterflatmapconcatmapzipmerge)
- [9. Schedulers, subscribeOn vs publishOn](#9-schedulers-subscribeon-vs-publishon)
- [10. Обработка ошибок (onErrorX, retry)](#10-обработка-ошибок-onerrorx-retry)
- [11. Cold vs Hot (share/publish/refCount/replay, Sinks)](#11-cold-vs-hot-sharepublishrefcountreplay-sinks)
- [12. Шпаргалка вопросов для собеса](#12-шпаргалка-вопросов-для-собеса)

---

## 1. Reactive Streams в двух словах

**Reactive Streams** — стандарт взаимодействия асинхронных потоков данных на JVM: издатель (**Publisher**) отправляет элементы подписчику (**Subscriber**) и *обязан* уважать **backpressure** (спрос подписчика), чтобы не перегрузить потребителя.

Реактор (Project Reactor) реализует Reactive Streams и даёт удобные типы:
- `Flux<T>` — 0..N элементов
- `Mono<T>` — 0..1 элемент

---

## 2. Сигналы и контракт

В реактивном потоке есть 3 вида сигналов:

- `onNext(T)` — данные (0..N раз)
- `onComplete()` — успешное завершение (1 раз максимум)
- `onError(Throwable)` — завершение с ошибкой (1 раз максимум)

После `onComplete` или `onError` **никаких onNext больше быть не может**.  
`onComplete/onError` — *терминальные* события.

---

## 3. Publisher / Subscriber / Subscription / Processor

### Publisher
Источник данных. Начинает эмитить элементы **только после подписки**.

В Reactor: `Flux` и `Mono` — это `Publisher`.

### Subscriber
Потребитель данных. Получает:
- `onSubscribe(Subscription)`
- `onNext`
- `onError` или `onComplete`

Важно: если в `subscribe(...)` не задан обработчик ошибки, Reactor может поднять `OnErrorNotImplemented`.

### Subscription
Связь между Publisher и Subscriber. Два ключевых метода:
- `request(n)` — запросить n элементов (это и есть backpressure)
- `cancel()` — отменить подписку

`request(Long.MAX_VALUE)` = “неограниченный спрос” (фактически отключает backpressure).

### Processor
И Publisher, и Subscriber одновременно (промежуточное звено).  
В современном Reactor чаще говорят про **Sinks** как про рекомендуемый механизм “ручной эмиссии” вместо старых Processor-ов.

---

## 4. Flux vs Mono

### Flux<T>
0..N элементов. Может быть конечным или бесконечным.

Примеры:
- поток сообщений из брокера
- `Flux.interval(...)` (бесконечный поток тиков)
- последовательность из коллекции

### Mono<T>
0..1 элемент. Часто используют как аналог “асинхронного результата”:
- HTTP запрос → `Mono<Response>`
- запись в БД → `Mono<Void>`
- вычисление одного значения → `Mono<T>`

Mono может:
- выдать 1 элемент и complete
- просто complete (пустой результат)
- завершиться error

---

## 5. Создание Flux/Mono

```java
Flux<String> f1 = Flux.just("a", "b", "c");
Flux<Integer> f2 = Flux.range(1, 5);
Flux<String> f3 = Flux.fromIterable(List.of("x", "y"));

Mono<String> m1 = Mono.just("hello");
Mono<String> m2 = Mono.empty();
Mono<String> m3 = Mono.error(new RuntimeException("boom"));
```

Отложенное выполнение (важно для собеса: “ленивость”):
```java
Mono<String> m = Mono.fromCallable(() -> blockingCall());
Mono<String> d = Mono.defer(() -> Mono.just(loadFromSomewhere()));
```

---

## 6. Подписка (subscribe) и управление demand

Перегрузки `subscribe`:

```java
flux.subscribe(); // просто запустить (опасно без обработчика ошибок)
flux.subscribe(v -> log(v));
flux.subscribe(v -> log(v), err -> log(err));
flux.subscribe(v -> log(v), err -> log(err), () -> log("done"));
flux.subscribe(v -> {}, err -> {}, () -> {}, sub -> sub.request(10));
```

`subscribe()` возвращает `Disposable`, можно отменить:
```java
Disposable d = flux.subscribe(...);
d.dispose(); // cancel()
```

### Ручной контроль request
Удобный вариант — `BaseSubscriber`:

```java
source.subscribe(new BaseSubscriber<Integer>() {
  @Override protected void hookOnSubscribe(Subscription s) { request(1); }
  @Override protected void hookOnNext(Integer v) { process(v); request(1); }
});
```

---

## 7. Backpressure (обратное давление)

**Backpressure** = механизм регулирования скорости: подписчик говорит издателю “сколько я готов принять”.

Важно для:
- быстрых источников + медленных обработчиков
- бесконечных потоков
- предотвращения OOM/перегруза

Факт, который любят спрашивать:
- многие “простые подписки” в Reactor **сразу запрашивают Long.MAX_VALUE**, то есть *без лимита*.

Полезные операторы:
- `limitRate(n)` — дробит запросы партиями (сглаживает нагрузку)
- `onBackpressureBuffer(...)` — буферизовать “лишнее”
- `onBackpressureDrop()` — дропать элементы при отсутствии спроса
- `onBackpressureLatest()` — держать только последний

---

## 8. Основные операторы (map/filter/flatMap/concatMap/zip/merge/...)

### Преобразования
```java
flux.map(x -> x * 2)
    .filter(x -> x % 2 == 0);
```

### flatMap vs concatMap
**flatMap**: параллельно/конкурентно, **порядок не гарантирован**  
**concatMap**: строго последовательно, **порядок сохраняется**

```java
Flux<User> users = ids.flatMap(service::getUser);      // быстрее, порядок может “плыть”
Flux<User> users2 = ids.concatMap(service::getUser);   // порядок сохраняется, но медленнее
```

Компромисс: `flatMapSequential`.

### Буферизация
```java
flux.buffer(10)        // Flux<List<T>>
flux.window(10)        // Flux<Flux<T>>
```

### Объединение потоков
- `merge` — параллельное слияние
- `concat` — последовательное соединение

```java
Flux.merge(f1, f2)
Flux.concat(f1, f2)
```

### zip
Объединение “по индексам” (параллельная сборка результата):
```java
Mono<UserProfile> p = Mono.zip(userMono, accMono,
    (u, a) -> new UserProfile(u, a)
);
```

### Side effects (для отладки/метрик)
```java
flux.doOnNext(v -> log(v))
    .doOnError(e -> log(e))
    .doFinally(st -> cleanup());
```

---

## 9. Schedulers, subscribeOn vs publishOn

По умолчанию всё выполняется в потоке, где произошёл `subscribe()`.

### Базовые Scheduler-ы
- `Schedulers.parallel()` — CPU-bound
- `Schedulers.boundedElastic()` — блокирующие I/O задачи
- `Schedulers.single()` — один поток
- `Schedulers.immediate()` — без переключения

### subscribeOn
Задаёт Scheduler для **источника и всей цепочки сверху** (обычно ставят один раз):
```java
Mono.fromCallable(this::blockingCall)
    .subscribeOn(Schedulers.boundedElastic())
    .subscribe();
```

### publishOn
Переключает поток выполнения **с этого места вниз по цепочке**:
```java
flux.publishOn(Schedulers.parallel())
    .map(this::cpuHeavy)
    .publishOn(Schedulers.boundedElastic())
    .flatMap(this::blockingIo)
    .subscribe();
```

**Любимый вопрос:**  
- `subscribeOn` влияет “наверх” (на источник),  
- `publishOn` — “вниз” (на последующие операторы).

---

## 10. Обработка ошибок (onErrorX, retry)

Главное: **ошибка терминальна**. Нельзя “просто продолжить тот же поток”, можно только:
- заменить ошибку значением
- переключиться на fallback-поток
- повторить (retry) подписку

### onErrorReturn
```java
flux.onErrorReturn(-1);
```

### onErrorResume (fallback)
```java
mono.onErrorResume(e -> fallbackMono());
mono.onErrorResume(IOException.class, e -> cacheMono());
```

### onErrorMap (переобернуть)
```java
mono.onErrorMap(e -> new MyBusinessException(e));
```

### retry
```java
mono.retry(3);
mono.retryWhen(Retry.backoff(3, Duration.ofMillis(200)));
```

> Осторожно: `retry` перезапускает источник целиком — побочные эффекты могут случиться повторно.

---

## 11. Cold vs Hot (share/publish/refCount/replay, Sinks)

### Cold (холодный)
Каждый подписчик получает свою “копию” последовательности с начала.  
Пример: `Flux.fromIterable(...)`, `Mono.fromCallable(...)`.

### Hot (горячий)
Источник “идёт сам”, подписчики подключаются “к текущему моменту” и могут пропустить прошлые элементы.

Как сделать hot/мультикаст:
- `share()` ≈ `publish().refCount(1)`
- `publish()` → `ConnectableFlux` + `connect()`
- `replay(n)` — кэширует N последних элементов для новых подписчиков

### Sinks (современный способ ручной эмиссии)
Используют, чтобы обернуть колбэки/события в Flux:

```java
Sinks.Many<String> sink = Sinks.many().multicast().directAllOrNothing();
Flux<String> hot = sink.asFlux();

hot.subscribe(System.out::println);
sink.tryEmitNext("a");
sink.tryEmitComplete();
```

---

## 12. Шпаргалка вопросов для собеса

### База
1. Что такое Reactive Streams? Назови 4 интерфейса.
2. Какие сигналы есть и почему error/complete терминальны?
3. Flux vs Mono — когда что использовать?
4. Что такое backpressure и как он реализован (request/cancel)?

### Операторы
5. `map` vs `flatMap` (и почему порядок может нарушаться)?
6. `flatMap` vs `concatMap` vs `flatMapSequential`.
7. `merge` vs `concat`.
8. `zip` — зачем и как работает.

### Потоки/производительность
9. `subscribeOn` vs `publishOn`.
10. Какие Scheduler выбрать для CPU vs blocking IO? Почему `boundedElastic()`?
11. Что будет, если сделать блокирующий вызов на `parallel()`?

### Ошибки
12. onErrorReturn/onErrorResume/onErrorMap — в чём разница?
13. `retry` — что именно повторяется? Какие риски?

### Hot/Cold
14. Что будет при двух подписках на cold Flux?
15. Как сделать hot поток? Что делает `share()`?
16. Что такое Sinks и зачем они нужны?

---

## Мини-конспект “на 30 секунд”
- Reactor = `Flux`(0..N) и `Mono`(0..1), оба `Publisher`.
- Контракт: много `onNext`, затем **один** `onComplete` **или** `onError`.
- Backpressure = `Subscription.request(n)`, `cancel()`.
- По умолчанию часто `request(Long.MAX_VALUE)` → без лимита.
- `subscribeOn` задаёт Scheduler для источника, `publishOn` — с места вниз.
- Ошибка терминальна: `onErrorResume` (fallback), `onErrorReturn` (default), `retry` (переподписка).
- Cold: каждый подписчик с начала. Hot: подписчик подключается “в момент времени” (`share/publish/refCount/replay`, `Sinks`).
