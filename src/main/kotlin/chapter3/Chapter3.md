# Chapter 3. CoroutineDispatcher

## 3.1. CoroutineDispatcher란 무엇인가?

- CoroutineDispatcher는 코루틴을 스레드로 보내주는 주체이다.
- CoroutineDispatcher는 스레드풀과 여러 개의 스레드를 가진다.
- 코루틴 라이브러리는 여러 가지 CoroutineDispatcher를 제공한다.

### 3.1.1. CoroutineDispatcher의 동작 살펴보기

<사진>

- CoroutineDispatcher 객체는 자신에게 실행 요청된 코루틴을 우선 작업 대기열에 적재한 후, 사용할 수 있는 스레드가 생기면 보내는 방식으로 동작한다.

### 3.1.2. CoroutineDispatcher의 역할

- CoroutineDispatcher는 코루틴의 실행을 관리하는 주체
- 실행 요청된 코루틴을 작업 대기열에 적재
- 스레드로 코루틴을 보내 실행될 수 있게 만드는 역할

> - 코루틴의 실행 옵션에 따라 작업 대기열에 적재되지 않고 즉시 실행될 수도 있음
> - 작업 대기열이 없는 CoroutineDispatcher 구현체도 존재함 (11.2. CoroutineStart의 다양한 옵션들 살펴보기, "11.3. 무제한 디스패처")

## 3.2. 제한된 디스패처와 무제한 디스패처

CoroutineDispatcher는 제한된 디스패처와 무제한 디스패처로 나뉜다.

- **제한된 디스패처(Confined Dispatcher)**
  - 사용할 수 있는 스레드나 스레드풀이 제한된 디스패처
  - 일반적으로, 어떤 작업을 처리할지 미리 역할을 부여하고 역할에 맞춰 실행을 요청하는 것이 효율적
  - 대부분의 코루틴 작업은 제한된 디스패처를 사용함
  - ex) Dispatchers.IO, Dispatchers.Default


- **무제한 디스패처(Unconfined Dispatcher)**
  - 사용할 수 있는 스레드나 스레드풀이 제한되지 않은 디스패처
  - 이전 코드가 실행되던 스레드에서 실행됨
  - 스레드가 매번 달라질 수 있음
  - ex) Dispatchers.Unconfined


## 3.3. 제한된 디스패처 생성하기

- 제한된 디스패처는 코루틴을 실행시킬 때, 보낼 수 있는 스레드가 제한된 CoroutineDispatcher 객체이다.
- 코루틴 라이브러리는 제한된 디스패처를 생성하는 여러 가지 함수를 제공한다.

### 3.3.1. 단일 스레드 디스패처 만들기

- 사용할 수 있는 스레드가 하나인 CoroutineDispatcher 객체
- 코루틴 라이브러리에서 제공하는 `newSingleThreadContext()` 함수를 사용해 생성
- `newSingleThreadContext()` 함수는 CoroutineDispatcher 객체를 반환하며, 스레드의 이름을 지정할 수 있다.
- 작업을 적재하기 위한 작업 대기열이 있고, 스레도 하나로 구성된 스레드풀이 존재한다.

```kotlin
val dispatcher: CoroutineDispatcher = newSingleThreadContext(name = "SingleThread")
```

### 3.3.2. 멀티 스레드 디스패처 만들기

- 2개 이상의 스레드를 사용할 수 있는 CoroutineDispatcher 객체
- 코루틴 라이브러리에서 제공하는 `newFixedThreadPoolContext()` 함수를 사용해 생성
- `newFixedThreadPoolContext()` 함수는 CoroutineDispatcher 객체를 반환하며, 스레드의 개수(nThreads)와 이름(name)을 지정할 수 있다.
- 작업을 적재하기 위한 작업 대기열이 있고, nThreads 개수의 스레드로 구성된 스레드풀이 존재한다.

```kotlin
val dispatcher: CoroutineDispatcher = newFixedThreadPoolContext(nThreads = 2, name = "MultiThread")
```

> **newFixedThreadPoolContext 함수가 구현된 방식**
> 
> - 코루틴 라이브러리 1.7.2 버전 기준
> - 함수 내부적으로 `Executors.newScheduledThreadPool()` 함수를 사용해 스레드풀을 생성
> - 모두 데몬 스레드로 생성됨
> - 생성된 `ExecutorService`는 `asCoroutineDispatcher()` 함수를 사용해 CoroutineDispatcher 객체로 변환됨
> 
> **중요한 것은 CoroutineDispatcher 객체가 코루틴을 스레드에 분배한다는 것**


?? 데몬 스레드로 생성이 된다면, 안드로이드 앱 프로세스의 경우 어떤 영향을 미칠까?


## 3.4. CoroutineDispatcher 사용해 코루틴 실행하기

### 3.4.1. launch의 파라미터로 CoroutineDispatcher 사용하기

- `launch()` 함수의 `context` 인자로 CoroutineDispatcher 객체를 지정해 코루틴을 실행할 수 있다.

```kotlin
// 단일 스레드 디스패처 사용해 코루틴 실행하기
fun main() = runBlocking {
    val dispatcher: CoroutineDispatcher = newSingleThreadContext(name = "SingleThread")
    launch(context = dispatcher) {
        println("${Thread.currentThread().name} 실행")
    }
}
// 멀티 스레드 디스패처 사용해 코루틴 실행하기
fun main() = runBlocking<Unit> {
    val multiThreadDispatcher = newFixedThreadPoolContext(
        nThreads = 2,
        name = "MultiThread",
    )
    launch(context = multiThreadDispatcher) {
        println("[${Thread.currentThread().name}] 실행") 
    }
    launch(context = multiThreadDispatcher) {
        println("[${Thread.currentThread().name}] 실행")
    }
}
```

### 3.4.2. 부모 코루틴의 CoroutineDispatcher 사용해 자식 코루틴 실행하기

- 코루틴은 구조화를 통해 코루틴 내부에서 새로운 코루틴을 실행할 수 있다.
- 바깥쪽의 코루틴을 부모 코루틴(Parent Coroutine), 내부에서 생성되는 코루틴을 자식 코루틴(Child Coroutine)이라고 한다.
- 구조화는 코루틴을 계층 관계로 만드는 것뿐만 아니라, Parent Coroutine의 실행 환경을 Child Coroutine에 전달하는 데에도 사용된다.
- 만약 Child Coroutine에 디스패처가 설정되지 않으면, Parent Coroutine의 디스패처를 사용한다.

```kotlin
fun main() = runBlocking<Unit> {
    val multiThreadDispatcher = newFixedThreadPoolContext(
        nThreads = 2,
        name = "MultiThread",
    )
    launch(multiThreadDispatcher) { // 부모 코루틴
        println("[${Thread.currentThread().name}] 부모 코루틴 실행")

        launch { // 자식 코루틴
            println("[${Thread.currentThread().name}] 자식 코루틴 실행")
        }
        launch { // 자식 코루틴
            println("[${Thread.currentThread().name}] 자식 코루틴 실행")
        }
    }
}
```


## 3.5. 미리 정의된 CoroutineDispatcher

앞서 다룬 `newFixedThreadPoolContext()` 함수를 사용하면 다음과 같은 경고가 출력된다.

> 이는 섬세하게 다뤄져야 하는 API이다. 섬세하게 다뤄져야 하는 API는 문서를 모두 읽고 제대로 이해하고 사용돼야 한다.

- 위 함수를 사용하면 비효율적일 가능성이 높다.
- 스레드풀에 속한 스레드의 수가 너무 적거나 많이 생성되어 쓸모없는 자원 낭비가 발생할 수 있다.
- 혹은, 여러 개발자가 개발할 경우, 디스패처 생성을 남발하게 될 수도 있다.

위 문제를 방지하기 위해 코루틴 라이브러리에는 미리 정의된 CoroutineDispatcher가 제공된다.

- Dispatchers.IO: 네트워크 요청이나 파일 입출력 등의 입출력(I/O) 작업을 위한 CoroutineDispatcher
- Dispatchers.Default: CPU 사용량이 많은 연산 작업을 위한 CoroutineDispatcher
- Dispatchers.Main: 메인 스레드를 사용하기 위한 CoroutineDispatcher

### 3.5.1. Dispatchers.IO
 
- 애플리케이션에서는 네트워크 통신이나 DB, 파일 입출력 등의 I/O 작업이 여러 개로 동시에 수행되는 경우가 많다.
- 이를 위해 많은 스레드가 필요하다.
- Dispatchers.IO는 이러한 I/O 작업을 위해 사용되는 CoroutineDispatcher 객체이다.
- 스레드풀의 스레드 개수는 JVM에서 사용이 가능한 프로세서의 수와 64 중 큰 값으로 설정된다.
- Dispatchers.IO는 싱글톤 인스턴스이다.

```kotlin
launch(Dispatchers.IO) {
    // ...
}
```

- 스레드의 이름은 DefaultDispatcher-worker-n 형식으로, 
- 코루틴 라이브러리에서 제공하는 **공유 스레드풀**에서 생성된 스레드를 사용한다.

### 3.5.2. Dispatchers.Default

- CPU 바운드 작업이 필요할 때 사용하는 CoroutineDispatcher 객체이다.
- 주로 대용량 데이터 처리, 계산 작업 등에 사용된다.
- Dispatchers.Default는 싱글톤 인스턴스이다.

```kotlin
launch(Dispatchers.Default) {
    // ...
}
```

> **입출력 작업과 CPU 바운드 작업**
> 
> - 입출력 작업과 CPU 바운드 작업의 중요한 차이는: 작업이 실행됐을 때 **스레드를 지속적으로 사용하는지 여부**이다.
> - 입출력 작업은 스레드를 사용하지 않는 시간이 많다. (ex. 네트워크 요청, 파일 입출력)
> - CPU 바운드 작업은 스레드를 지속적으로 사용한다. (ex. 대용량 데이터 처리, 계산 작업)
> - 따라서, 입출력 작업은 스레드 기반과 코루틴 기반의 성능 차이가 크고, CPU 바운드 작업은 성능 차이가 크지 않다.

### 3.5.3. limitedParallelism 사용해 Dispatchers.Default 스레드 사용 제한하기

- Dispatchers.Default를 사용해 무겁고 오래 걸리는 연산을 처리하면 특정 연산을 위해 Dispatchers.Default의 모든 스레드가 사용될 수 있다.
- 이를 방지하기 위해 `limitedParallelism()` 함수를 사용하면 Dispatchers.Default의 스레드 사용 개수를 제한할 수 있다.

```kotlin
// Dispatchers.Default의 스레드 중 2개의 스레드만 사용하게 됨
launch(Dispatchers.Default.limitedParallelism(2)) {
    // ...
}
```

### 3.5.4. 공유 스레드풀을 사용하는 Dispatchers.IO와 Dispatchers.Default

- Dispatchers.IO와 Dispatchers.Default는 코루틴 라이브러리에 의해 관리되는 공유 스레드풀을 사용한다.
- 이 공유 스레드풀에서는 스레드를 무제한으로 생성할 수 있다.
- 물론, Dispatchers.IO와 Dispatchers.Default가 사용하는 스레드 영역은 분리된다.


- `Disaptchers.Default.limitedParallelism()`는 Dispatchers.Default의 스레드 중 일부만 사용하게 된다.
- `newFixedThreadPoolContext()` 함수로 만들어지는 디스패처는 자신만 사용할 수 있는 전용 스레드풀을 생성하는 반면, Dispatchers.IO와 Dispatchers.Default는 공유 스레드풀을 사용한다.

> **Dispatchers.IO의 limitedParallelism**
> 
> - `Dispatchers.IO.limitedParallelism()`는 `Dispatchers.Default.limitedParallelism()`와 달리 새로운 스레드 풀을 만들어낸다.
> - 스레드의 수를 제한 없이 만들어낼 수 있다.
> - `Dispatchers.IO.limitedParallelism()`는 특정한 작업이 다른 작업에 영향을 받지 않아야 해, 별도 스레드 풀에서 실행되는 것이 필요할 때 사용된다.

### 3.5.5. Dispatchers.Main

- Dispatchers.Main은 일반적으로 UI가 있는 애플리케이션에서 메인 스레드의 사용을 위해 사용되는 특별한 CoroutineDispatcher 객체이다.
- 즉, 별도의 라이브러리(`kotlinx-coroutines-android` 등)를 추가해야 이 디스패처를 사용할 수 있다.

## 3.6. 요약