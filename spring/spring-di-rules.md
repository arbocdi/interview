# Spring DI — правила инжекции бинов (шпаргалка)

## 1. Общий алгоритм выбора бина (по типу)

Spring при инжекции зависимости проходит шаги:

1. Ищет **все бины подходящего типа**
2. Если **1 бин** → инжектит его
3. Если **>1 бина**:
   - если есть `@Qualifier` → фильтрует по нему
   - иначе, если есть `@Primary` → берёт primary
   - иначе → ❌ `NoUniqueBeanDefinitionException`

---

## 2. Конструкторная инъекция

```java
@Component
class B {
  private final A a;

  public B(A a) {}
}
```

- Если **один конструктор** → `@Autowired` не нужен
- Если конструкторов несколько → нужен `@Autowired`
- Рекомендуемый способ DI (immutability, fail-fast)

---

## 3. `@Primary`

```java
@Component
@Primary
class CardPayService implements PayService {}
```

- Используется **только если нет `@Qualifier`**
- Это “бин по умолчанию”
- Не участвует, если есть явный `@Qualifier`

---

## 4. `@Qualifier`

```java
@Component("crypto")
@Qualifier("fast")
class CryptoPayService implements PayService {}
```

```java
@Autowired
@Qualifier("fast")
PayService ps;
```

`@Qualifier("X")` матчится по:
1. `@Qualifier("X")` на бине
2. **имени бина / alias**

> `@Qualifier` сильнее `@Primary`

---

## 5. Имя бина

### `@Component`
```java
@Component("crypto")
class CryptoPayService {}
```

### `@Bean`
```java
@Bean
PayService crypto() {}
```

- Имя бина = имя метода
- Используется в `@Qualifier` и `@Resource`

---

## 6. `@Resource` (JSR-250)

```java
@Resource(name = "crypto")
PayService ps;
```

- Инжекция **строго по имени бина**
- ❌ не понимает `@Qualifier`
- ❌ не учитывает `@Primary`

Эквивалент:
```java
@Autowired
@Qualifier("crypto")
PayService ps;
```

---

## 7. Generics-инъекция

### `List<T>`

```java
List<PayService> services;
```

- Всегда инжектятся **ВСЕ бины типа**
- Контекст поднимается даже при множестве кандидатов
- Учитывает `@Order`

---

### `Map<String, T>`

```java
Map<String, PayService> services;
```

- Ключ = **имя бина**
- Значение = бин
- ❌ `@Order` НЕ учитывается
- Порядок = порядок регистрации

---

## 8. `@Order`

```java
@Order(1)
class CardPayService {}
```

Учитывается в:
- `List<T>`
- массиве
- `ObjectProvider.stream()`

❌ Не учитывается в `Map`

---

## 9. `ObjectProvider<T>`

```java
ObjectProvider<PayService> provider;
```

Плюсы:
- ленивое получение
- безопасно при scope mismatch
- можно получить:
  - `getIfAvailable()`
  - `getIfUnique()`
  - `stream()` (с `@Order`)

---

## 10. Scope mismatch

❌ Нельзя:
```java
@Scope("request")
class RequestCtx {}

@Component
class Singleton {
  public Singleton(RequestCtx ctx) {}
}
```

✔ Правильно:
- `ObjectProvider<RequestCtx>`
- `@Lazy`
- scoped proxy

---

## 11. Циклические зависимости

```java
class A { A(B b) {} }
class B { B(A a) {} }
```

- ❌ constructor–constructor → ошибка
- ✔ допустимо:
  - `@Lazy`
  - setter injection
  - `ObjectProvider`
- ❌ setter-инъекция как решение — плохой дизайн

---

## 12. Приоритеты (кто сильнее)

1. `@Qualifier`
2. имя бина
3. `@Primary`
4. порядок регистрации
5. ❌ ошибка

---
```java
@Component
@DependsOn("b")
class A {
  public A() {}
}

@Component("b")
@Lazy
class B {
  public B() {
    System.out.println("B created");
  }
}

```
B создается перед A из-за `@DependsOn("b")`
---

## TL;DR

- Один бин → ок
- Несколько → `@Qualifier` или `@Primary`
- `@Qualifier` > `@Primary`
- `@Resource` = только имя
- `List<T>` — все бины
- `Map<String, T>` — имя → бин
- `ObjectProvider` — универсальный спасатель
