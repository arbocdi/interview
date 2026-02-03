# @DltHandler — что делает и когда вызывается

`@DltHandler` — это обработчик сообщений из **DLT (Dead Letter Topic)** в Spring Kafka.  
Он вызывается **только тогда**, когда сообщение признано необрабатываемым и **больше не будет ретраиться**.

---

## Когда вызывается `@DltHandler`

### 1. Исчерпаны все retry-попытки

```
main-topic
   ↓
retry-1
   ↓
retry-2
   ↓
retry-N
   ↓
DLT  →  @DltHandler
```

Условия:
- исключение считается retryable
- количество `attempts` закончилось

---

### 2. Исключение попало в `notRetryOn`

```
main-topic
   ↓
DLT  →  @DltHandler
```

Условия:
- бизнес-ошибка
- повторная попытка не имеет смысла

---

## Что делает `@DltHandler`

Типичные задачи:

- логирование ошибок
- отправка алертов
- метрики по DLT
- сохранение сообщений для ручного разбора
- аудит проблемных событий

---

## Что `@DltHandler` НЕ делает

- не выполняет retry
- не возвращает сообщение обратно
- не влияет на ack основного listener’а
- не «чинит» сообщение автоматически

Сообщение уже:
- скоммичено
- навсегда ушло в DLT

---

## Пример

```java
@DltHandler
public void onDlt(
    String payload,
    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
    @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error
) {
    log.error("DLT message from {}: {}", topic, error);
}
```

---

## Потоки и изоляция

- DLT обрабатывается **отдельным consumer-контуром**
- у DLT свои потоки и свой `concurrency`
- обработка DLT **не блокирует** main и retry

---

## Как правильно думать

> DLT — это последняя станция обработки  
> `@DltHandler` — место диагностики и учёта, а не восстановления

---

## Коротко

```
retryable + attempts исчерпаны → DLT → @DltHandler
notRetryOn → сразу DLT → @DltHandler
```
