# Micrometer Observation — конспект

## Что такое Observation

**Observation** в Micrometer — это абстракция над выполнением операции, которая позволяет
единым API собирать:

- метрики (Timer / Counter),
- трейсы (Span / Trace),
- события и ошибки,
- и связывать всё это через общий контекст.

Код описывает **что наблюдаем**, а **как и куда это отправляется** — решают handlers, подключённые к `ObservationRegistry`.

---

## Ключевая идея

> Один instrumentation → много сигналов (metrics + traces + logs)

Observation — это “точка наблюдения” вокруг операции:
HTTP-запрос, Kafka-сообщение, SQL, бизнес-метод и т.д.

---

## Основные компоненты

### Observation

Объект с жизненным циклом:

- `start()` — начало
- `stop()` — завершение
- `error(Throwable)` — ошибка
- `event(Event)` — событие внутри
- `contextualName(String)` — человекочитаемое имя
- `lowCardinalityKeyValue(k, v)` — теги для метрик
- `highCardinalityKeyValue(k, v)` — теги для трейсов

Кардинальность:
- **low** — безопасна для Prometheus (region, method, status)
- **high** — только для трейсов (orderId, userId)

---

### ObservationRegistry

Центральный реестр Observation.

Содержит:
- список `ObservationHandler`
- `ObservationConvention`
- текущую Observation (thread-local)
- интеграции с MeterRegistry и Tracing

Через него создаются и запускаются Observation.

---

### Observation.Context

Контекст конкретного Observation.

Хранит:
- имя observation
- теги
- carrier (HTTP request, message и т.п.)
- произвольные атрибуты
- состояние выполнения

Контекст передаётся во все handlers.

---

### ObservationHandler

Подписчик на жизненный цикл Observation.

Типичные колбэки:
- `onStart(context)`
- `onStop(context)`
- `onError(context)`
- `onEvent(event, context)`
- `supportsContext(context)`

Примеры handlers:
- метрики → создаёт Timer
- трейсинг → открывает/закрывает Span
- логирование → пишет ошибки/события

---

## ASCII-схема
```
               +-------------+       modifies
               | Context     |<----------------------------------------------
               +------+------+                                              |
                      ^                                                     |
                      |  contains                                           |
  callbacks    +-------------+                                              |
  -------------| Observation |                                              |
  |            +------+------+                                              |
  |                   ^                                                     |
  |                   |  observation created with registry                  |
  |                   |                                                     |
  |                   |                                                     |
  |       +------------------------+                                        |
  |       | ObservationRegistry    |                                        |
  |       |  - handlers            | contains                               |
  |       |  - conventions         |---------------------------------       |
  |       +-----------+------------+                                |       |
  |                   | contains ObservationHandlers                |       |    
  |     +-------------+--------------------+                        |       |
  |     |                                  |                        |       |
  |     |                                  |                        |       |    
  |     |                                  |                        |       |    
  v     v                                  v                        v       |
+---------------------+        +----------------------+        ┌▽────────────────┐ 
| Metrics Handler     |        | Tracing Handler      |        │ObservationFilter│ 
| (Timer / Counter)   |        | (Span / Trace)       |        └─────────────────┘ 
+---------------------+        +----------------------+
        |                                  |
        v                                  v
 Prometheus / Metrics               OTLP / Zipkin
```

---

## Использование Observation вручную

### Базовый вариант

```java
Observation observation =
    Observation.createNotStarted("my.operation", registry)
        .contextualName("CreateOrder")
        .lowCardinalityKeyValue("region", "kg");

try {
    observation.start();
    // бизнес-логика
} catch (Exception e) {
    observation.error(e);
    throw e;
} finally {
    observation.stop();
}
```

---

### Упрощённый вариант

```java
Observation.createNotStarted("my.operation", registry)
    .lowCardinalityKeyValue("region", "kg")
    .observe(() -> {
        // бизнес-логика
        return "ok";
    });
```

Если внутри выброшено исключение:
- оно автоматически помечается как `error`
- затем пробрасывается дальше

---

## Использование в Spring через @Observed

Spring предоставляет аннотацию `@Observed`
(AOP-обёртка вокруг метода).

```java
@Observed(
    name = "payment.charge",
    contextualName = "ChargeCard"
)
public void charge(String provider) {
    // бизнес-логика
}
```

---

## Когда что использовать

| Сценарий | Рекомендация |
|--------|-------------|
| Быстро обернуть сервис | `@Observed` |
| Нужен полный контроль | `Observation.observe()` |
| Kafka / SQL / HTTP | Observation + Convention |
| Связать метрики и трейсы | Observation обязательно |

---

## Итог

Observation — фундамент Micrometer Observability:
- Registry управляет жизненным циклом
- Context несёт данные
- Handlers превращают события в метрики и трейсы
- Один instrumentation → много сигналов

