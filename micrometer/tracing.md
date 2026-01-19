# Micrometer Tracing — конспект (traceId / spanId + Observation)

## Что такое distributed tracing

**Трейсинг** (distributed tracing) — это способ увидеть *один пользовательский запрос* как цепочку операций через разные компоненты и сервисы:
- входящий REST,
- внутренние вызовы,
- запросы в БД,
- ошибки и задержки.

Цель: ответить на вопросы **«где тормозит?»**, **«кто кого вызвал?»**, **«на каком шаге упало?»**.

---

## Trace, traceId

**Trace** (трейс) — “история” выполнения одного запроса end‑to‑end.

- **traceId** — общий идентификатор всего трейса.
- Он **один и тот же** во всех сервисах/компонентах, участвующих в обработке запроса.

---

## Span, spanId

**Span** (спан) — один шаг/операция внутри трейса.

- **spanId** — идентификатор конкретного спана.
- У спана обычно есть:
  - `traceId`
  - `spanId`
  - `parentSpanId` (кроме корневого)
  - имя (например: `HTTP GET /orders`, `SQL SELECT`, `ChargeCard`)
  - start/end + duration
  - теги/атрибуты (http.method, db.system, status, error, …)

Итого:
- `traceId` отвечает: **к какому запросу относится запись**
- `spanId` отвечает: **какой именно шаг внутри запроса**

---

## Пример: 2 сервиса + REST + DB (ASCII)

Сценарий:
- Клиент вызывает **Service A** по REST
- Service A вызывает **Service B** по REST
- Service B делает запрос в **DB**

Диаграмма (время слева направо):

```
Client                Service A                    Service B                     DB
  |                      |                           |                          |
  |--HTTP GET /orders--> |                           |                          |
  |   traceId=T          |                           |                          |
  |                      |-- span A1: "HTTP SERVER"  |                          |
  |                      |   (traceId=T, spanId=1)   |                          |
  |                      |                           |                          |
  |                      |--HTTP GET /pricing------->|                          |
  |                      |   inject context:         |                          |
  |                      |   traceId=T, parent=1     |                          |
  |                      |   span A2: "HTTP CLIENT"  |                          |
  |                      |   (traceId=T, spanId=2,   |                          |
  |                      |    parentSpanId=1)        |                          |
  |                      |                           |-- span B1: "HTTP SERVER" |
  |                      |                           |   (traceId=T, spanId=3,  |
  |                      |                           |    parentSpanId=2)       |
  |                      |                           |                          |
  |                      |                           |-- span B2: "DB QUERY" -->|
  |                      |                           |   (traceId=T, spanId=4,  |
  |                      |                           |    parentSpanId=3)       |
  |                      |                           |<-- result --------------|
  |                      |                           |-- stop span B2           |
  |                      |                           |-- stop span B1           |
  |                      |<--HTTP 200 ---------------|                          |
  |                      |-- stop span A2            |                          |
  |<--HTTP 200 ----------|-- stop span A1            |                          |
```

Ключевое:
- `traceId=T` один на весь путь.
- вложенность спанов: `A1 -> A2 -> B1 -> B2`.

---

## Как контекст переезжает между сервисами

Чтобы Service B продолжил тот же trace, Service A передаёт trace‑контекст в HTTP‑заголовках.

Обычно используется один из форматов:
- **W3C Trace Context**: `traceparent`, `tracestate`
- **B3**: `X-B3-TraceId`, `X-B3-SpanId`, …

Смысл один: вместе с REST запросом летит `traceId` + информация о родительском спане, чтобы на стороне B построить child‑span.

---

## Как трейсинг реализуется через Observation (Micrometer)

### Главная идея
**Observation** — единый API жизненного цикла операции (start/stop/error/events), а **трейсинг** появляется когда в `ObservationRegistry` подключён tracing handler (bridge Micrometer Tracing).

### Архитектура
```
Your code / Spring
   |
   |  Observation.start()/stop()/error()
   v
ObservationRegistry
   |
   +--> Metrics handler  -> Timer/Counter
   |
   +--> Tracing handler  -> Span.start()/Span.end()
```

### Что делает tracing handler
Трейсинг реализован как `ObservationHandler` (концептуально — “TracingObservationHandler”):

- `onStart(context)`:
  - создаёт **Span** (root или child)
  - активирует его (scope/current span)
  - кладёт span в `Observation.Context` (как attribute)
- `onError(context)`:
  - помечает span как error, добавляет детали
- `onStop(context)`:
  - закрывает span и освобождает scope

Важный вывод:
- `Observation.Context` сам по себе “пустой”
- его наполняют instrumentation + convention + handlers
- **traceId/spanId живут в Span**, который создаёт tracer (Brave/OTel), а Observation управляет lifecycle.

---

## Кто “заполняет” Observation.Context

1) **Instrumentation** (Spring MVC, WebClient, JDBC, Kafka, @Observed):
- создаёт observation и конкретный context
- кладёт carrier (request/response/message)

2) **ObservationConvention**:
- извлекает данные из carrier
- добавляет теги (low/high cardinality) и имена

3) **ObservationHandler’ы**:
- metrics handler пишет Timer/Counter
- tracing handler создаёт/закрывает Span, переносит ошибки, делает propagation

---

## Практические советы

- Давай понятные имена спанам: `CreateOrder`, `ChargeCard`, `Kafka process account-event`.
- **Low-cardinality теги** — только ограниченный набор значений (region=kg/kz, method, status).
- **High-cardinality** (orderId/userId) лучше держать в трейсе, а не в метриках.
- Для асинхронщины важно корректно переносить контекст (в Spring часто уже решено, зависит от стека).

---

## Итог

- **traceId** — идентификатор всего запроса (один на цепочку)
- **spanId** — идентификатор шага внутри запроса
- **Observation** — оркестратор “наблюдения”
- **Tracing (Span/Trace)** подключается через ObservationHandler (bridge)
