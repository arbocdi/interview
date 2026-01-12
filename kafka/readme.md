### Kafka

* <span style="color:blue">Kafka</span> - is a message broker.
* <span style="color:blue">Topic</span> - это именованный поток сообщений.
* <span style="color:blue">Producer</span> посылает сообщение в один топик, <span style="color:blue">cоnsumer</span> потребляет из множества топиков.
* Kafka гарантирует доставку сообщений  at least once => возможны дубликаты.

![producers-consumers.png](producers-consumers.png)

### Kafka Consumer Group and Offsets

* <span style="color:blue">Partition</span>  - это упорядоченный набор сообщений, это часть топика. 
Каждый topic разбивается на партиции для репликации данных (replica) и повышение быстродействия (parallel consumers).
Sending a message to a topic appends it to the selected partition. 
* <span style="color:blue">Offset</span> - это порядковый уникальный номер сообщения в партиции. Полный адрес сообщения можно записакть как: <span style="color:red">{topic, partition, offset}</span>.
* <span style="color:blue">Current Offset</span> - это номер последнего добавленного сообщения.
* <span style="color:blue">Commited Offset</span> - это номер последнего успешно обработанного сообщения (хранится в кафка для определенного потребителя).
* <span style="color:blue">Consumer offset commit</span> - когда потребитель обрабатывает сообщение он информирует кафку об offset 
последнего обработанного сообщения, если потребитель остановится и вновь подключится, то он получит все сообщения начиная с последнего commited offset.

![commit.png](commit.png)

* <span style="color:blue">Consumer group</span> - это группа однотипных потребителей сообщений, обрабатывающих сообщения параллельно.Kafka хранит offset в партиции для каждого потребителя.
Consumer group получает сообщения из топика один раз, один потребитель может быть подписан на 0..* партиций, партиция посылает сообщения только одному потребителю из группы(!)
=> число партиций определяет степень параллелизма обработки сообщений:

![consumer-groups.png](consumer-groups.png)
![too much consumers](moreConsumersThenPartitions.png)

Kafka хранит offsets так:
```
(group.id, topic, partition) -> offset
```
Когда consumer из группы коммитит offset:
  - он коммитит позицию группы
  - но только для тех partition, которые сейчас ему назначены

### Kafka delivery modes

* At most once  — commit до обработки → нет дублей, возможна потеря сообщений.
* At least once — commit после обработки → нет потерь сообщений, возможны дубли.
* Exactly once не достижим для kafka+postgres, но можно существенно приблизиться, используя режим At least once и:
  * Idempotent messages
  * Inbox pattern

### Inbox pattern
* Позволяет получить exactly once доставку в рамках одного сервиса.
* Consumer получил сообщение → начинаем транзакцию в БД:
  1. Попытаться вставить запись в таблицу INBOX(message_id, payload, status)
      - если запись уже есть → дубликат → пропускаем
      - если нет → значит новое сообщение
  2. Выполнить бизнес-логику (insert/update в других таблицах)
  3. Отметить inbox.status = PROCESSED
  4. Зафиксировать транзакцию
  5. Коммит offset в Kafka

### Outbox pattern
1. Обрабатываем команду и в той же транзакции записываем сообщение в табл. outbox
2. Другой поток:
   - открывает транзакцию в бд и вычитывает сообщения
   - внутри кафка транзакции посылает сообщения в кафка
   - удаляет сообщения в бд и коммитит транзакцию

### Replicas

* Replicas are copies of partitions.
* Replicas are maintained for high availability and fault tolerance
* No two replicas of same partition will ever reside in the same broker
* Какие реплики бывают в Kafka:
  - Leader: 
    - Ровно одна реплика на партицию
    - Принимает все записи от продюсеров
    - Отдаёт данные консьюмерам
  - Follower
    - Остальные реплики партиции
    - Читают лог у лидера
    - Сами продюсеров и консьюмеров не обслуживают
* ISR (In-Sync Replicas) — это подмножество реплик (leader + followers), которые:
  - не отстали от лидера больше чем на replica.lag.time.max.ms
  - не имеют слишком большого лагa по offset’ам
  - считаются консистентными
  - Leader всегда входит в ISR
  - Могут стать лидером при фейловере
* Out-of-Sync Replicas. Это реплики, которые:
  - входят в replicas
  - не входят в ISR
* П: Всего 3 реплики и с такими настройками лидер и 1 follower должны подтвердить сохранение сообщения: 
```
acks=all

replication.factor = 3
min.insync.replicas = 2
```
* 
* ![replica.png](replica.png)

### Kafka inside docker

* use docker-compose project
* start containers
```bash
sudo docker-compose up -d
```
* stop
```bash
sudo docker-compose down -v
```
* auto-start
always 	Always restart the container if it stops. If it is manually stopped, it is restarted only when Docker daemon restarts or the container itself is manually restarted. 
```
sudo docker-compose up -d --restart always redis
```
* send message to qr-tablet-topic
```bash
sudo docker exec -it kafka /opt/kafka/bin/kafka-console-producer.sh --broker-list localhost:19092 --topic tmc-topic
```
* показать все топики:
```bash
sudo docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

* consume messages qr-tablet-topic
```bash
sudo docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:19092 --topic fbucks-topic
#on sandbox
docker exec -it kafka bash -c "unset JMX_PORT && /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic department-topic"
```
* consume messages from multiple topics (using regular expressions)
```bash
sudo docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --whitelist '.*'
```
* kafka на sandbox.foodband.ru, контейнер с именем kafka, compose лежит в: /srv/fbadmin/kafka
* создать топик:
```bash
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --create --topic qr-tablet-topic --partitions 1 --replication-factor 1
```
