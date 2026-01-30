# AtomicStampedReference (конспект)

## Зачем нужен

`AtomicStampedReference<V>` решает **ABA-проблему** для `AtomicReference<V>`.

Идея:

> CAS сравнивает не только ссылку, но и **версию (stamp)**.

Аналогия:

* CAS + stamp ≈ optimistic locking + `@Version` в БД

---

## Что такое ABA (кратко)

```
A → B → A
```

* CAS видит снова `A`
* считает, что состояние не менялось
* но структура **менялась логически**

Опасно, когда `A` — **указатель на структуру** (узел, head списка).

---

## Что хранит AtomicStampedReference

```java
AtomicStampedReference<V>
```

Внутри логически хранится пара:

```
(V reference, int stamp)
```

* `reference` — ссылка (как в `AtomicReference`)
* `stamp` — версия / счётчик изменений

CAS проверяет **оба значения**.

---

## Основные операции

### Создание

```java
AtomicStampedReference<Node> ref =
    new AtomicStampedReference<>(null, 0);
```

---

### Чтение ссылки и stamp

```java
int[] stampHolder = new int[1];
Node value = ref.get(stampHolder);
int stamp = stampHolder[0];
```

> `int[]` используется, чтобы вернуть stamp по ссылке.

---

### CAS с проверкой версии

```java
boolean success = ref.compareAndSet(
    expectedRef,
    newRef,
    expectedStamp,
    expectedStamp + 1
);
```

CAS успешен **только если**:

* ссылка совпала
* stamp совпал

---

## Как stamp решает ABA

Сценарий:

```
A(1) → B → A(2)
```

Попытка:

```java
CAS(A, B, 1 → 2)
```

Результат:

* ссылка `A` совпала ✅
* stamp `1 ≠ 2` ❌

➡️ CAS не проходит, поток перечитывает актуальное состояние.

---

## Пример: pop() в lock-free стеке

```java
Node pop() {
    int[] stamp = new int[1];
    Node oldHead;
    Node newHead;

    do {
        oldHead = head.get(stamp);
        if (oldHead == null) return null;
        newHead = oldHead.next;
    } while (!head.compareAndSet(
            oldHead,
            newHead,
            stamp[0],
            stamp[0] + 1));

    return oldHead;
}
```

Линеаризация происходит **в момент успешного CAS**.

---

## Почему GC не решает ABA

* ABA — **логическая** проблема
* объект может:

    * быть жив
    * переиспользоваться
    * иметь ту же ссылку

Stamp фиксирует **факт изменения**, а не время жизни объекта.

---

## AtomicStampedReference vs AtomicMarkableReference

| Класс                   | Что хранит     | Когда использовать  |
| ----------------------- | -------------- | ------------------- |
| AtomicStampedReference  | `int stamp`    | счётчик версий, ABA |
| AtomicMarkableReference | `boolean mark` | логическое удаление |

---

## Цена использования

Минусы:

* больше памяти
* больше CAS-параметров
* сложнее код

Плюсы:

* корректность lock-free структур
* защита от скрытых возвратов узлов

---

## Когда НЕ нужен

Не нужен, если:

* значение — число / флаг
* нет ссылок на структуру
* ABA семантически допустимо

Примеры:

* `AtomicInteger`
* `AtomicLong`

---

## Ключевая мысль

> **AtomicStampedReference = CAS + версия.**

Используется, когда:

* CAS работает со ссылками
* важна история изменений
* нужна защита от ABA

---

## Связанные понятия

* ABA problem
* CAS (Compare-And-Set)
* Optimistic locking
* TOCTOU
* Lock-free algorithms
