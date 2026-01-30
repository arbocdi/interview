
# Designing Data-Intensive Applications — Chapter 2
## Data Models and Query Languages (конспект)

Этот конспект объясняет основные модели данных из главы 2 DDIA
на простом примере набора данных о **Люси**:
- где она **родилась**
- где она **живет сейчас**

Будем считать количество людей, рожденных в Париже и живущих в Берлине.

---

## 1. Реляционная модель данных (PostgreSQL)

### Подход к моделированию данных

Реляционная модель:
- данные нормализованы
- связи выражаются через внешние ключи
- схема фиксированная
- отлично подходит для сложных join-ов и строгих инвариантов

Часто используется, когда:
- важна целостность данных
- много связей
- нужны транзакции

### Логическая модель данных (Люси)

**Таблица people**

| id | name |
|----|------|
| 1  | Lucy |

**Таблица places**

| id | name        | type |
|----|-------------|------|
| 1  | Paris       | city |
| 2  | Berlin      | city |

**Таблица person_places**

| person_id | place_id | relation |
|-----------|----------|----------|
| 1         | 1        | born     |
| 1         | 2        | lives    |

### SQL-запрос

```sql
SELECT count(1)
FROM people p
JOIN person_places born_rel  ON born_rel.person_id = p.id AND born_rel.relation = 'born'
JOIN places born_place  ON born_place.id = born_rel.place_id
JOIN person_places live_rel  ON live_rel.person_id = p.id AND live_rel.relation = 'lives'
JOIN places live_place  ON live_place.id = live_rel.place_id
WHERE born_place.name = 'Paris' and live_place.name = 'Berlin';
```

---

## 2. Документная модель данных (MongoDB)

### Подход к моделированию данных

Документная модель:
- данные хранятся как JSON-подобные документы
- схема гибкая или отсутствует
- данные часто денормализуются
- отлично подходит для чтения целых агрегатов

Хорошо подходит, когда:
- данные читаются «целиком»
- структура может меняться
- мало сложных join-ов

### Логическая модель данных (Люси)
Данные представляются просто документами со свободной схемой.

Коллекция **people**:

```json
{
  "_id": "lucy",
  "name": "Lucy",
  "born_in": {
    "city": "Paris",
    "country": "France"
  },
  "lives_in": {
    "city": "Berlin",
    "country": "Germany"
  }
}
```

### Map-Reduce запрос

```js
db.people.mapReduce(
        // map emits key-value pairs: Paris->Berlin : 1
        function () {
         
          if (
                  this.born_in &&
                  this.lives_in &&
                  this.born_in.city === "Paris" &&
                  this.lives_in.city === "Berlin"
          ) {
            emit("Paris->Berlin", 1);
          }
        },
        // reduce recieives a key and an array of values
        function (key, values) {
          return Array.sum(values);
        },
        {
          out: { inline: 1 }
        }
);
```

## 3. Графовая модель данных (Neo4j)

### Подход к моделированию данных
Графовая модель:
- данные представлены в виде вершин и ребер
- вершина имеет: id,label,свойства,ребра
- ребро имеет: id,label,начальный и конечный узлы,свойства 

Хорошо подходит, когда:
- много связей many-to-many
- нужно гибко обходить граф
- важны path-запросы

### Логическая модель данных (Люси)

**Граф**
```cypher
// 1) Создаём/находим узлы
MERGE (lucy:Person {name: "Lucy"})
MERGE (paris:City  {name: "Paris"})
MERGE (berlin:City {name: "Berlin"})

// 2) Создаём/находим связи
MERGE (lucy)-[:BORN_IN]->(paris)
MERGE (lucy)-[:LIVES_IN]->(berlin);
//lucy,paris,berlin - псевдонимы
```
### Cypher-запрос

```cypher
MATCH (p:Person)-[:BORN_IN]->(:City {name: "Paris"})
MATCH (p)-[:LIVES_IN]->(:City {name: "Berlin"})
RETURN count(DISTINCT p) AS people_count;
```

---
## 4. Triple store
Triple store — это модель хранения данных в виде троек (triples):
```triple
(subject, predicate, object)
(подлежащее, сказуемое, дополнение)
```

* Это базовый формат RDF (Resource Description Framework).
* subject — сущность (обычно URI)
* predicate — отношение/свойство (тоже URI)
* object — значение или другая сущность (URI или литерал: строка/число/дата)

В отличие от реляционки/документов:
  - схема почти отсутствует (“schema-on-read”)
  - всё — граф утверждений
  - запросы обычно через SPARQL

**Turtle-формат:**
```turtle
(subject, predicate, object)
(подлежащее, сказуемое, дополнение)

a = rdf:type,: = namespace

@prefix : <http://example.org/> .

:Lucy   a :Person .
:Paris  a :City .
:Berlin a :City .

:Lucy   :bornIn  :Paris .
:Lucy   :livesIn :Berlin .
```

**SPARQL запрос:**

```sparksql
PREFIX : <http://example.org/>

SELECT (COUNT(DISTINCT ?person) AS ?people_count)
WHERE {
  ?person a :Person .
  ?person :bornIn  :Paris .
  ?person :livesIn :Berlin .
}
```
## Краткое сравнение моделей

| Модель       | Сильные стороны                         | Ограничения              |
|--------------|------------------------------------------|-------------------------|
| Реляционная  | Транзакции, join-ы, инварианты            | Жесткая схема          |
| Документная  | Простота, скорость чтения агрегатов       | Дублирование данных    |
| Графовая     | Гибкие связи, сложные отношения           | Сложнее масштабировать |

---

## Итого:

- **Нет универсальной модели данных**
- Модель данных должна соответствовать:
  - паттернам доступа
  - требованиям к консистентности
  - сложности связей
- Query language отражает философию модели данных

