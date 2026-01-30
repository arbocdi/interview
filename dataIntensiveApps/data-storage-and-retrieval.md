# Storage and Retrieval

Краткий, структурированный конспект по устройству хранилищ данных и индексам
(по мотивам *Designing Data‑Intensive Applications*).

---

## 1. Log (журнал)

**Log** — append‑only последовательность записей.

**Идея:**

* Есть key → value.
* При каждом изменении значения ключа мы **добавляем новую запись в конец лога**.

**Свойства:**

* Запись — быстрая (последовательная).
* Чтение без индекса — `O(N)`.

### Индекс

**Index** — дополнительная структура для ускорения чтения.

**Hash‑index:**

* Для каждого ключа хранится offset → позиция записи в логе.

```
key ──▶ offset ──▶ log file
```

### Сегменты

* Лог растёт бесконечно → разбивается на **segments**.
* Для каждого сегмента — свой индекс.

### Compaction

**Compaction** — удаление дубликатов ключей, оставляя последнее значение.

* Старые сегменты **не изменяются** → можно компактифицировать в фоне.
* Compaction уменьшает размер данных.

```
[seg1] [seg2] [seg3]
   │      │
   └── merge + drop old keys ──▶ [compact segment]
```

---

## 2. Sorted String Table (SSTable)

**SSTable** — файл, где записи **отсортированы по ключу**.

### Почему это важно

* Можно делать **merge sort** при compaction.
* Индекс можно сделать **разреженным (sparse)**.

### Sparse‑index + блоки

* Индекс хранит не каждый ключ, а ссылку на диапазон (блок) ключей.
* Блоки данных сжимаются.

```
Sparse index
k1-k99 ──▶ block 1 (compressed)
k100-k199 ─▶ block 2 (compressed)
```

### Memtable → SSTable

Как получить отсортированные данные:

1. Данные пишутся:

    * в **memtable** (in‑memory, сбалансированное дерево поиска)
    * в **append‑only log** (для crash‑safety)
2. Memtable переполняется → сбрасывается на диск как SSTable
3. Соответствующий лог удаляется

```
        write
          │
          ▼
   +-------------+
   |  Memtable   |  (balanced tree)
   +-------------+
          │ flush
          ▼
      +--------+
      | SSTable|
      +--------+

   +-------------+
   |   WAL Log   |
   +-------------+
```

### Merge + Compaction

```
SSTable 1 ─┐
SSTable 2 ─┼─▶ compact+merge sort ─▶ compacted SSTable
SSTable 3 ─┘
```

---

## 3. Log‑Structured Merge Tree (LSM Tree)

**LSM‑tree** — подход, основанный на:

* последовательных записях
* периодическом merge sort + compaction отсортированных файлов

**Используется в:**

* LevelDB
* RocksDB
* Cassandra
* HBase

---

## 4. B‑Trees

**B‑tree** — классический индекс.

### Основные идеи

* Данные хранятся в **страницах (pages)** фиксированного размера (обычно 4 KB)
* Каждая страница содержит:

    * диапазоны ключей
    * ссылки на дочерние страницы

### Структура

```
            [ 100 ref 200 ref 300 ref  400 ref ] Root page
           /                     |      \
      [110 ref 120 ref] Page   [Page]   [Page]
        |                        |
     [110 111 112] Leaf        [Leaf]
```

### Поиск ключа

```
Root → child → child → leaf → value
```

### Операции

* **Search:** `O(log N)`
* **Update:** найти leaf → изменить значение
* **Insert:** при переполнении страницы → split

### Branching factor

* Количество ссылок на детей в одной странице
* Пример:

> 4‑уровневое дерево, 4 KB страницы, branching factor = 500
> может хранить ~256 TB данных

### Надёжность (WAL or redo log)

Проблема:

* Изменения страниц **in‑place** → crash = corruption

Решение:

* **Write‑Ahead Log (WAL)**
* При повторном запуске изменения в WAL применяются к соотв страницам

```
append to WAL → modify B‑tree page
on restart: WAL -> apply to B-tree
```

### Конкурентность

* Используются **latches** — (lightweight locks) лёгкие блокировки страниц

---

## 5. LSM vs B‑Tree

| Характеристика | LSM‑Tree         | B‑Tree    |
| -------------- | ---------------- | --------- |
| Запись         | ⭐ быстрее        | медленнее |
| Чтение         | медленнее        | ⭐ быстрее |
| IO             | последовательный | случайный |
| Фрагментация   | нет              | есть      |
| Compaction     | да (фон)         | нет       |

**Минус LSM:** compaction может мешать latency.

---

## 6. Secondary Indexes

* Secondary index можно строить:
    * на B‑tree
    * на LSM‑tree
* Secondary index можно строить на основе первичного индекса

---

## 7. Где хранится value

### Варианты

1. **Clustered index**

    * value хранится рядом с key
2. **Heap file**

    * индекс → pointer в heap file

### Covering index

* Индекс содержит **часть колонок**
* Запрос обслуживается **без чтения основной таблицы**
* This allows some queries to be answered by using the index alone
in which case, the index is said to cover the query.

---

## 8. Multi‑column indexes

### Concatenated index

```
key=(key1, key2, key3) → value
```

* Хорошо работает для prefix‑запросов

### Multi‑dimensional indexes

Используются для:

* геоданных
* диапазонных запросов по нескольким координатам
* PostGis allows to use such indexes to query for latitude and longitude values within a given box:
```sql
SELECT *
FROM restaurants
WHERE latitude  BETWEEN 51.4946 AND 51.5079
  AND longitude BETWEEN -0.1162 AND -0.1004;
```

---

## 9. OLAP (Online Analytical Processing)
* OLAP is different from OLTP (online transaction processing) in that it is designed to answer queries 
over large amounts of data instead of transaction creation.
### OLTP vs OLAP

| OLTP                       | OLAP                          |
| -------------------------- | ----------------------------- |
| много маленьких транзакций | длинные аналитические запросы |
| low latency                | throughput                    |
| текущие данные             | исторические данные           |

### Data Warehouse

* Отдельная БД для аналитики
* Данные загружаются через **ETL**:

```
Extract → Transform → Load
```

### Star Schema

```
           [ Dimension: Date ]
                  │
[ Dim: User ] ─ [ Fact Table ] ─ [ Dim: Product ]
                  │
           [ Dimension: Location ]
```

* **Fact table** — события, метрики
* **Dimension tables** — справочники

---

## 10. Column‑oriented storage

### Row storage (OLTP)

```
Row → (c1, c2, c3, c4)
```

### Column storage (OLAP)

```
Column c1 → v1, v2, v3
Column c2 → v1, v2, v3
```

### Преимущества

* Читаются только нужные колонки
* Лучше сжатие
* Быстрее агрегаты

---

## Краткое резюме

* **LSM‑tree** — быстрые записи, sequential IO
* **B‑tree** — быстрые чтения, random IO
* **OLTP ≠ OLAP** — разные требования → разные движки
* Column storage — основа аналитических БД

---

