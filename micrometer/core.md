# Конспект: Micrometer в Java / Spring Boot

## Что такое Micrometer
**Micrometer** — это фасад для метрик: единый API, через который приложение собирает метрики (count, time, gauge),
а дальше они экспортируются в разные системы мониторинга (Prometheus, OTLP, Influx, Datadog и т.д.).
Часто говорят: *SLF4J для метрик*.

---

## Базовые понятия

### Meter
**Meter** — это измерительный инструмент + метаданные:
- имя (например `orders.created`)
- теги (`region=kg`, `status=ok`)
- описание, базовая единица (опционально)

Один и тот же Meter может существовать в нескольких вариациях по тегам.

---

## Типы метрик

### Counter
**Counter** — монотонно растущий счётчик.
Используется для подсчёта событий: запросы, ошибки, созданные объекты.

```java
Counter counter = Counter.builder("orders.created")
    .tag("region", "kg")
    .register(registry);

counter.increment();
```

Аннотация:
```java
@Counted("orders.created")
public void createOrder() {}
```

---

### Gauge
**Gauge** — показывает *текущее значение* в момент опроса.
Типовые кейсы: размер очереди, количество активных потоков, текущее состояние.

```java
AtomicInteger queueSize = new AtomicInteger();

Gauge.builder("queue.size", queueSize, AtomicInteger::get)
    .register(registry);
```

Важно: gauge не хранит историю — её строит система мониторинга.

---

### Timer
**Timer** измеряет:
- количество вызовов
- суммарное время выполнения
- максимальное время
- (опционально) процентили и гистограммы

```java
Timer timer = Timer.builder("external.call")
    .register(registry);

timer.record(() -> callExternalService());
```

Аннотация:
```java
@Timed("auth.login")
public void login() {}
```

---

## Процентили (percentiles)

**P-ый процентиль** — это значение, ниже которого лежит p% измерений.

Примеры:
- p50 — медиана (типичное значение)
- p95 — 95% запросов быстрее этого значения
- p99 — «хвост», показывает редкие, но медленные запросы

Процентили особенно важны для latency:
среднее время часто *маскирует* редкие, но очень медленные операции.

### Как включить процентили для Timer

#### Программно
```java
Timer timer = Timer.builder("http.client.requests")
    .publishPercentiles(0.5, 0.95, 0.99)//считать указанные процентили
    .publishPercentileHistogram()//считать значения, не превышающие значений из стандартного набора (бакеты)
    .register(registry);
```

#### Через аннотацию
```java
@Timed(
  value = "http.client.requests",
  percentiles = {0.5, 0.95, 0.99},
  histogram = true
)
public void call() {}
```

> Примечание: реальные процентили корректно считаются в Prometheus/Grafana
> именно через histogram + `histogram_quantile()`.

---

## MeterRegistry

**MeterRegistry** — центральный реестр метрик.
Все Counter / Gauge / Timer регистрируются именно в нём.

В Spring Boot `MeterRegistry` автоматически создаётся и доступен через DI.

```java
@RequiredArgsConstructor
@Service
public class Service {
  private final MeterRegistry registry;
}
```

---

## CompositeMeterRegistry

**CompositeMeterRegistry** позволяет публиковать метрики сразу в несколько бэкендов.

```
CompositeMeterRegistry
 ├─ PrometheusMeterRegistry
 ├─ OtlpMeterRegistry
 └─ JmxMeterRegistry
```

Ты регистрируешь метрику один раз — она уходит во все дочерние registry.

---

## ASCII-диаграмма

```
                ┌──────────────────────┐
                │      Application     │
                │ Counter / Timer ...  │
                └──────────┬───────────┘
                           │
                           │
                ┌──────────▼───────────┐
                │CompositeMeterRegistry│
                └───────┬────────┬─────┘
                        │        │
                        v        v
        ┌────────────────────┐  ┌────────────────┐
        │ PrometheusRegistry │  │ OTLPRegistry   │
        └────────────────────┘  └────────────────┘
```

---

## Spring Boot 4 + Actuator

### Зависимость
```properties
implementation("org.springframework.boot:spring-boot-starter-actuator")
#prometheus - сам приходит за метриками
implementation("io.micrometer:micrometer-registry-prometheus")
```

### Включить endpoints
```properties
management.endpoints.web.exposure.include=health,metrics,prometheus
#или публиковатьв все
management.endpoints.web.exposure.include=*
```

### Смотреть метрики
- `/actuator/metrics`
- `/actuator/metrics/jvm.memory.used`
- `/actuator/metrics/http.server.requests?tag=status:200`
- `/actuator/prometheus`

---

## Короткие рекомендации
- Counter — только для событий, которые *не уменьшаются*
- Gauge — для текущего состояния
- Timer + percentiles — для latency
- Не плодить теги с высокой кардинальностью (userId, orderId ❌)
