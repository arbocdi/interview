# Spring MVC — краткий конспект

## Что это
**Spring MVC** — модуль Spring Framework для построения веб‑приложений и REST API на основе паттерна **MVC (Model–View–Controller)**.

Основная идея:
> HTTP-запрос → Java-метод → HTTP-ответ

---

## Зачем нужен
Spring MVC берёт на себя:
- парсинг HTTP-запроса\генерацию ответа
- маршрутизацию URL
- валидацию данных
- сериализацию / десериализацию (JSON ↔ Java)
- обработку ошибок и HTTP-статусов

Разработчик пишет только бизнес‑код.

---

## Основные компоненты

### DispatcherServlet
Центральный фронт‑контроллер.
Принимает **все HTTP-запросы** и управляет их обработкой.

### HandlerMapping
Определяет, какой контроллер и метод подходят под запрос.

### Controller
Класс с аннотациями, который:
- принимает запрос
- вызывает сервисы
- возвращает результат

### HttpMessageConverter
Конвертирует:
- JSON → Java (RequestBody)
- Java → JSON (Response)

---

## Поток обработки запроса

1. Клиент отправляет HTTP-запрос
2. DispatcherServlet принимает его
3. HandlerMapping находит нужный контроллер метод
4. Spring подготавливает аргументы метода
5. Вызывается метод контроллера
6. Результат сериализуется
7. Клиент получает HTTP-ответ

---
# Additional information: 

---
## MVC в Spring

### Model
Данные:
- Entity
- DTO
- Value Object

### View
Формат ответа:
- JSON (чаще всего)
- HTML (Thymeleaf / JSP)

### Controller
Связующее звено между HTTP и бизнес‑логикой

---

## Основные аннотации

### Контроллеры
```java
@RestController
@Controller
```

### Маршруты
```java
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
@RequestMapping
```

### Параметры запроса
```java
@PathVariable
@RequestParam
@RequestBody
@RequestHeader
```

### Ответы
```java
@ResponseStatus
ResponseEntity<T>
```

### Обработка ошибок
```java
@ExceptionHandler
@ControllerAdvice
```

---

## Пример контроллера

```java
@RestController
@RequestMapping("/orders")
class OrderController {

    @GetMapping("/{id}")
    OrderDto get(@PathVariable long id) {
        return service.getOrder(id);
    }
}
```

---

## Spring MVC и REST

В REST‑приложениях:
- View = JSON
- Контроллеры тонкие
- Вся логика в сервисах / use‑case

Контроллер ≠ бизнес‑логика.

---

## Место в архитектуре

### Монолит
- Контроллеры + сервисы + БД

### Микросервисы
- REST API
- Stateless
- JSON

### Hexagonal / Clean Architecture
- Контроллер = Inbound Adapter
- HTTP → команда / use‑case

---

## Что НЕ стоит делать в контроллерах
- бизнес‑логику
- транзакции
- сложные if/else
- работу с БД

Контроллер — только адаптер.

---

## Коротко
Spring MVC позволяет писать обычные Java‑методы, которые автоматически становятся полноценными HTTP‑эндпоинтами с минимальным количеством кода.
