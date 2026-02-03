# FactoryBean / ObjectFactory / Supplier — краткий конспект

## 1. FactoryBean<T>
**Что это:** бин, который производит другой бин.

### Ключевая идея
- `getBean("myBean")` → результат `getObject()`
- `getBean("&myBean")` → сам FactoryBean

### Интерфейс
```java
public interface FactoryBean<T> {
    T getObject();
    Class<?> getObjectType();
    boolean isSingleton();
}
```
- **FactoryBean** myClient при этом станет бином, хотя я просто создал его через new.
- Имя бина MyClient будет "myClient", а имя фабрики "&myClient"
- ObjectFactory и Supplier реализованные через new(), вернут просто экз. класса, а не бин. Однако их реализации по умолчанию
будут искать бин по его классу в ApplicationContext.
- FactoryBean — это способ научить Spring создавать ваш объект (вместо конструктора).
- ObjectFactory\Supplier — это способ попросить Spring достать уже существующий (или создать Scoped) бин в нужный момент.
```java
@Component("myClient")
public class MyClientFactory implements FactoryBean<MyClient> {

    @Override
    public MyClient getObject() {
        return new MyClient("https://api.example.com");
    }

    @Override
    public Class<?> getObjectType() {
        return MyClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
```

### Когда использовать
- сложная логика создания объектов
- прокси, клиенты, адаптеры
- инфраструктурный код, интеграции, легаси
- активно используется внутри Spring

### Плюсы / минусы
+ гибкость, контроль жизненного цикла  
– много магии, тяжёл для бизнес-кода

---

## 2. ObjectFactory<T>
**Что это:** ленивый провайдер бина.

### Интерфейс
```java
@FunctionalInterface
public interface ObjectFactory<T> {
    T getObject();
}
```
```java
@Component
public class AuditService {

    private final ObjectFactory<RequestContext> ctxFactory;

    public AuditService(ObjectFactory<RequestContext> ctxFactory) {
        this.ctxFactory = ctxFactory;
    }

    public void audit() {
        RequestContext ctx = ctxFactory.getObject();
        // каждый вызов — актуальный request scope
    }
}

```

### Зачем нужен
- ленивое получение бина
- разрыв циклических зависимостей
- доступ к scoped-бинам из singleton

### Типовой кейс
```java
singleton → request/session scope
```

### Аналоги
- `ObjectProvider<T>` (расширенная версия)
- `Provider<T>` (JSR-330)

---

## 3. Supplier<T>
**Что это:** стандартный Java-функциональный интерфейс.

```java
@FunctionalInterface
public interface Supplier<T> {
    T get();
}
```

### Использование в Spring
- регистрация бинов
```java
@Bean
public MyService myService() {
    return new MyService();
}
//эквивалентно
context.registerBean(
        MyService.class,
    () -> new MyService()
);

```
- lazy-доступ
```java
@Autowired
Supplier<HeavyService> heavy;

public void doWork() {
    heavy.get().run();
}

```
- конфигурации

### Плюсы / минусы
- просто, без магии, современно  
- нет информации о метаданных бина

---

## Сравнение

| Механизм | Уровень | Назначение |
|--------|--------|-----------|
| FactoryBean | Spring internals | сложное создание бинов |
| ObjectFactory | Spring DI | lazy + scope |
| Supplier | Java | простое и явное создание |

---

## Практическое правило
- бизнес-код → Supplier / ObjectProvider
- scoped / lazy → ObjectFactory
- фреймворки / интеграции → FactoryBean
