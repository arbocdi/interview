# Kafka Transactions — краткий конспект

## 1. Что такое Kafka-транзакции

Kafka-транзакции — это механизм, который позволяет атомарно, в рамках одной транзакции:
  - читать сообщения
  - обрабатывать их
  - отправлять новые сообщения
  - фиксировать offsets
---
## 3. Изоляция на стороне consumer

Уровень изоляции задаётся **у consumer’а**.

### `read_uncommitted` (по умолчанию)

```properties
spring.kafka.consumer.isolation-level=read_uncommitted
```

- consumer видит **все сообщения**
- включая сообщения из **незакоммиченных** и **aborted** транзакций
- быстрее, но **небезопасно**

Использовать:
- если транзакции не применяются
- или порядок/дубли не критичны

---

### `read_committed`

```properties
spring.kafka.consumer.isolation-level=read_committed
```

- consumer видит **только закоммиченные транзакции**
- aborted-сообщения **никогда не читаются**
---

## 4. Producer и транзакции

### Обязательные свойства

```properties
#all ISR replicas must acknowledge the record
spring.kafka.producer.acks=all
spring.kafka.producer.retries=2147483647
# Ensures that duplicate records are not produced to Kafka
spring.kafka.producer.properties.enable.idempotence=true
#It is used to identify transactions from a specific producer. 
# A producer that performs transactions is known in the Kafka documentation as a “transactional producer”
spring.kafka.producer.properties.transactional.id=tx-${random.uuid}
```
```
transactional.id ───► producer instance
                         ├─ tx #1
                         ├─ tx #2
                         ├─ tx #3
```
зачем ${random.uuid}?
```
tx-${random.uuid}
```
Это делается на старте приложения, чтобы:
  - каждый инстанс приложения получил уникальный transactional.id
  - не было конфликта, если поднято несколько копий сервиса

❗ UUID подставляется один раз при старте, а не на каждую TX.

Если два живых producer’а используют один и тот же transactional.id:
  - Kafka считает одного из них зомби
  - старый producer будет fencing’нут
  - ты получишь:
```
ProducerFencedException
```
Транзакционный consumer-producer ВСЕГДА работает как обычный consumer из consumer group,
но offset’ы этой группы коммитятся не consumer’ом, а producer’ом — в транзакции.
* The transactional.id helps avoid “zombie producers” – a producer that was thought to have failed, or been deliberately killed, but is in fact still running.
* Zombie producers would result in duplicate messages as they would be publishing the same events that any new replacement producer would be creating
* A way to avoid these potential duplicates is to use a transactional producer for the creation of all events. If a replacement producer is created, it should then use the same transactional.id as the original.
Original one will be fenced out (огорожен).
## 5. Жизненный цикл транзакции

```text
initTransactions()
  ↓
beginTransaction()
  ↓
send(msg1)
send(msg2)
send(msg3)
  ↓
commitTransaction()   ← или abortTransaction()
```

В Spring:
```java
kafkaTemplate.executeInTransaction(kt -> {
    kt.send(...);
    kt.send(...);
    return true;
});
```

* Если в spring определить свойство:
```properties
spring.kafka.producer.transaction-id-prefix=tx-
```
то ProducerFactory станет транзакционным и будет добавлен бин **KafkaTrtansactionManager**.
* Если при этом есть **TransactionManager для БД**, то при использовании **@Transactional** возникнет конфликт 
тк 2 бина реализуют один и тот же **@PlatformTransactionManager**. И придется различать бины по имени.

---
## 6. Порядок сообщений

### Гарантируется
- порядок **внутри одного partition**
- порядок **внутри одной транзакции**

Условие:
- один ключ → один partition

### Не гарантируется
- порядок между partition
- порядок между разными транзакциями
- глобальный порядок в системе

---

## 7. Exactly-once (EOS)

Kafka-транзакции дают:
- exactly-once **producer → Kafka**
- и **consume → process → produce** (с offset’ами)

Не дают:
- exactly-once **Kafka → БД**
- это решается outbox / inbox

