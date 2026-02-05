# Spring MVC — краткий конспект для техскрининга

## 1. Общая архитектура Spring MVC

Spring MVC реализует паттерн **Front Controller**.

Центральный компонент:
- **DispatcherServlet** — единая точка входа для всех HTTP-запросов.

---

## 2. Жизненный цикл HTTP-запроса

1. Клиент отправляет HTTP-запрос  
2. **DispatcherServlet** принимает запрос  
3. **HandlerMapping** ищет подходящий handler (контроллер + метод) по аннотациям  
4. **HandlerAdapter**:
   - резолвит аргументы метода (`@PathVariable`, `@RequestParam`, `@RequestBody`, `Model`)
   - применяет `HttpMessageConverter`
   - выполняет валидацию (`@Valid`)
5. Вызывается метод контроллера
6. Обработка результата:
   - view name → `ViewResolver`
   - `@ResponseBody` → сериализация в body
7. Формируется `HttpServletResponse`
8. Ответ отправляется клиенту

---

## 3. Controller vs RestController

### @Controller
- Используется для MVC
- Возвращаемое значение трактуется как **имя view**
- Работает с `ViewResolver`

```java
@Controller
public class PageController {
    @GetMapping("/page")
    public String page(Model model) {
        return "index";
    }
}
```

### @RestController
- Используется для REST API
- `@RestController = @Controller + @ResponseBody`
- Результат метода пишется напрямую в HTTP body

```java
@RestController
public class ApiController {
    @GetMapping("/user")
    public User user() {
        return new User();
    }
}
```

---

## 4. HttpMessageConverter

Используется **HandlerAdapter**, а не контроллером напрямую.

Подключается при:
- `@RequestBody`
- `@ResponseBody` (явно или через `@RestController`)

Назначение:
- Request: JSON → Object
- Response: Object → JSON / XML / String / bytes

---

## 5. Model и View

- `Model` создаётся Spring автоматически
- Контроллер наполняет модель данными
- `ViewResolver` по имени view находит шаблон
- **View рендерится на сервере**
- Клиент получает готовый HTML

---

## 6. @ResponseBody vs ResponseEntity

### @ResponseBody
- Пишет результат метода в body
- Статус: 200 OK
- Заголовки — стандартные

### ResponseEntity
- Полный контроль над ответом:
  - HTTP статус
  - headers
  - body

```java
return ResponseEntity
    .status(HttpStatus.CREATED)
    .header("X-Test", "1")
    .body(user);
```

---

## 7. Обработка ошибок

### @ControllerAdvice / @RestControllerAdvice
- Глобальная обработка исключений
- Используется `@ExceptionHandler`
- Удобно для единого формата ошибок

### @ResponseStatus
- Привязка исключения к HTTP статусу
- Без дополнительной логики

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {}
```

### HandlerExceptionResolver
- Низкоуровневый механизм
- Используется Spring внутри

---

## 8. Filter vs HandlerInterceptor

### Filter (Servlet API)
- Работает **до DispatcherServlet**
- Не знает о Spring MVC
- Используется для:
  - security
  - CORS
  - логирования
  - gzip

### HandlerInterceptor (Spring MVC)
- Работает **внутри MVC**
- Знает о handler и контроллере
- Методы:
  - preHandle
  - postHandle
  - afterCompletion

---

## 9. RequestParam vs PathVariable

### @PathVariable
- Часть URI
- Используется для идентификации ресурса

```java
GET /users/10
```

### @RequestParam
- Query-параметры
- Используется для фильтров, опций

```java
GET /users?active=true&page=2
```

---

## 10. Типовые собеседовательные ловушки

- Возврат объекта из `@Controller` без `@ResponseBody` → попытка резолва view
- HttpMessageConverter зависит от аннотаций, а не типа контроллера
- View рендерится на сервере, а не в браузере

---
