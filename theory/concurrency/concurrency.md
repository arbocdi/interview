
# Конспект по многопоточности в Java

## 0. ASCII-диаграмма жизненного цикла потока

```
                      ┌──────────────┐
                      │   NEW        │
                      │ (создан)     │
                      └──────┬───────┘
                             │ start()
                             ▼
                      ┌──────────────┐
                      │ RUNNABLE     │
                      │ готов к CPU  │
                      └─┬──────┬─────┘
            блокировка/ │      │ планировщик
            ожидание    │      │ даёт CPU
                        ▼      ▼
               ┌──────────────┐
               │   BLOCKED    │  ← поток ждёт монитор
               └──────────────┘
               ┌──────────────┐
               │  WAITING     │  ← wait(), park()
               └──────────────┘
               ┌──────────────┐
               │ TIMED_WAITING│ ← sleep(), wait(timeout)
               └──────────────┘
                             │ уведомление/таймаут/разблокировка
                             ▼
                      ┌──────────────┐
                      │ RUNNABLE     │
                      └──────┬───────┘
                             │ завершение run()
                             ▼
                      ┌──────────────┐
                      │ TERMINATED   │
                      └──────────────┘
```

---

## 1. Две фундаментальные проблемы многопоточности

### 1.1 Race Conditions (условия гонки)
Проблема атомарности и порядка записи/чтения общего состояния.

Типичные проявления:
- lost update
- check-then-act
- read-modify-write
- неправильный double-checked locking

### 1.2 Memory Visibility (проблемы видимости)
Потоки не видят актуальные изменения друг друга из-за:
- кэш-линий CPU
- регистров
- reorder от CPU/JIT
- отсутствия синхронизации

Итого:
> Все проблемы многопоточности — это нарушение **атомарности** или **видимости**.

---
## Happens-before
* If effects of operator A a visible to operator B then A HB (happens-before) B.
* HB is transitive: if A HB B and B HB C then A HB C.
* `Syncrhonized` and `volatile` creates HB between treads, due to transitivity it prevents instruction reordering,
for example, all actions in thread A before volatile write HB to all actions in B after volatile read.
## 2. synchronized и volatile

### 2.1 synchronized
Создаёт правило **happens-before**:

```
unlock(lock)  HB→  lock(lock)
```

Все действия потока **до** `monitorexit` гарантированно видны потоку, который входит через `monitorenter`.
Only one thread can perform actions inside synchronized block.

### 2.2 volatile
- volatile-write = release
- volatile-read = acquire
- гарантирует видимость
- запрещает reorder вокруг себя

Создаёт HB:
```
volatile write  HB→  volatile read (если читатель видит значение)
```

---

## 3. CAS и атомики

### 3.1 CAS (Compare-And-Swap)
Атомарная CPU-инструкция:

```
if (*address == expected)
    *address = newValue
```

### 3.2 AtomicInteger/AtomicLong — CAS loop

```
for (;;) {
    old = get()
    new = old + 1
    if (CAS(old → new)) break
}
```

CAS быстрый, но при конкуренции возможны многочисленные retry.

---

## 4. ReentrantLock, очереди и CAS

ReentrantLock построен на AQS:

- `state` — счётчик (reentrant), увеличивается при lock() и уменьшается при unlock(), state==0 - лок свободен
- `exclusiveOwnerThread` — владелец
- очередь ожидания
- CAS(state, 0→1) — попытка захвата

### Unfair lock:
```
if (state == 0 && CAS(0→1)) success
```

### Fair lock:
```
if (first in queue) CAS
else fail
```
| Method                              | Description                                                   | Returns   | Exceptions                   |
|-------------------------------------|---------------------------------------------------------------|-----------|------------------------------|
| `lock()`                            | Acquires the lock, blocks until available.                    | void      | —                            |
| `lockInterruptibly()`               | Acquires the lock unless interrupted. Responds to interrupts. | void      | InterruptedException         |
| `tryLock()`                         | Attempts to acquire the lock immediately without waiting.     | boolean   | —                            |
| `tryLock(long time, TimeUnit unit)` | Tries to acquire the lock within the given timeout.           | boolean   | InterruptedException         |
| `unlock()`                          | Releases the lock. Decrements reentrant hold count.           | void      | IllegalMonitorStateException |

---

## 5. Основные многопоточные проблемы

### Deadlock
Круговая зависимость ресурсов.  
**Решения:** порядок захвата, tryLock(timeout), отказ от вложенных локов.
```
ex:
T1: locks A waits B
T2: locks B waits A
```
### Livelock
Потоки активны, но не продвигаются, «уступают» друг другу.  
**Решения:** random backoff, асимметрия.

```java
//потоки все время берут и сбрасывают блокировки, но ничего не делаюь
import java.util.concurrent.locks.ReentrantLock;

public class LivelockExample {

    private static final ReentrantLock lock1 = new ReentrantLock();
    private static final ReentrantLock lock2 = new ReentrantLock();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> work(lock1, lock2), "T1");
        Thread t2 = new Thread(() -> work(lock2, lock1), "T2");

        t1.start();
        t2.start();
    }

    static void work(ReentrantLock first, ReentrantLock second) {
        while (true) {
            try {
                if (first.tryLock()) {
                    Thread.sleep(300);
                    // имитация "вежливости": если второй лок занят — уступаем
                    if (!second.tryLock()) {
                        first.unlock();
                        Thread.yield(); // уступили: yeld tells the scheduler that current thread doesn't do any 
                        continue;       // пробуем снова
                    }
                    // если второй лок тоже взяли — успех!
                    //do some actions
                    second.unlock();
                    first.unlock();
                    break;
                }
            } catch (Exception ignored) {}
        }
    }
}

```

### Starvation
Поток никогда не получает ресурс.  
**Причины:** unfair lock, yield-паттерны.  
**Решения:** fair lock, отказ от yield.

### Indefinite Blocking
blocking операция внутри synchronized.  
**Решение:** не блокировать внутри монитора.
```
T1: locks A waits signal on B
T2: tries to lock A and send signal on B
```

## Simple Signaler
```java
public class Signaler{
    public synchronized get(){
        this.wait();
    }
    public synchronized send(){
        this.notify();
    }
    //T1: signaler.get();
    //T2: signaler.send();
}

```
### Missed Signals
notify до wait → сигнал потерян  
**Решение: add state**
```java
public class Signaler{
    boolean sent = false;
    public synchronized get(){
        if(!sent) this.wait();
    }
    public synchronized send(){
        sent = true;
        this.notify();
    }
}
```
### Spurious Wakeups
wait просыпается без сигнала.  
**Решение: spin lock** while (!condition) wait()
```java
public class Signaler{
    boolean sent = false;
    public synchronized get(){
        while(!sent){ 
            this.wait();
        }
    }
    public synchronized send(){
        sent = true;
        this.notify();
    }
}
```

### Sliding Conditions
Проверка и действие вне одного synchronized-блока.  
**Решение:** объединить в один монитор.
```java
synchronized{
    if(condition)
}
synchronized{
    //some actions
}
//rewrite to:
synchronized{
    if(condition) //some actions
}
```
---

