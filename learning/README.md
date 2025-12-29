# learning (Gradle, Java 11)

Минимальный проект на Gradle (Groovy DSL), Java 11, пакет `kg.arbocdi.learning`.

## Быстрый старт

1) Убедись, что установлен JDK 11 и Gradle (или сгенерируй wrapper командой ниже).
2) В корне проекта:

```bash
# Сгенерировать gradle wrapper (если gradle установлен глобально):
gradle wrapper

# Сборка и тесты:
./gradlew build

# Запуск приложения:
./gradlew run
```

Либо без wrapper (если используешь системный Gradle):

```bash
gradle build
gradle run
```

## Структура
```
learning/
  build.gradle
  settings.gradle
  src/
    main/java/kg/arbocdi/learning/App.java
    test/java/kg/arbocdi/learning/AppTest.java
```
