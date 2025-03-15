# 5장. async와 Deferred

> **5장에서 다루는 내용**
> - async-await를 사용해 코루틴으로부터 결괏값 수신하기
> - awaitAll 함수를 사용해 복수의 코루틴으로부터 결괏값 수신하기
> - withContext를 사용해 실행 중인 코루틴의 CoroutineContext 변경하기

## 5.1. async 사용해 결괏값 수신하기

### 5.1.1. async 사용해 Deferred 만들기

- `launch`와 `async`는 모두 코루틴을 생성하는 **코루틴 빌더 함수**이다.
- `launch` 코루틴 빌더와 `async` 코루틴 빌더는 매우 비슷하다.

```kotlin
// launch 코루틴 빌더 함수
fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job
```
```kotlin
// async 코루틴 빌더 함수
fun <T> CoroutineScope.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T>
```

- `launch`와 `async`의 차이점은, `launch`는 `Job`객체를 반환하고 **`async`는 `Deferred`객체를 반환**한다.
- `Deferred`는 `Job`의 하위 인터페이스이다.
- `Job`과 `Deferred`는 모두 코루틴을 추상화한 객체이다.
- **즉, `Deferred`는 `Job`의 기능을 모두 가지고 있으며, 추가로 생성된 결괏값을 감싸는 기능을 가지고 있다.**

`async` 함수를 사용하는 예시 코드
```kotlin
val networkDeferred: Deferred<String> = async(Dispatchers.IO) {
    delay(1000L) // 네트워크 요청
    return@async "Dummy Response" // 결괏값 반환
}
```

### 5.1.2. await를 사용한 결괏값 수신

**Deferred**

- `Deferred` 객체는 미래의 어느 시점에 결괏값(T)이 반환될 수 있음을 표현하는 코루틴 객체이다.
- `Deferred` 객체는 결괏값 수신의 대기를 위해 `await` 함수를 제공한다.

**Deferred.await()**

- `await` 함수를 호출하면 `Deferred` 코루틴의 실행이 완료될 때까지 `await` 함수를 호출한 코루틴이 일시 중단된다.
- `Deferred` 코루틴의 실행이 완료되면 결괏값을 반환하고, 호출부의 코루틴을 재개한다.
- 호출부의 코루틴을 일시 중단한다는 점에서 `Job` 객체의 `join` 함수와 매우 유사하게 동작한다.

```kotlin
fun main() = runBlocking<Unit> {
    val networkDeferred: Deferred<String> = async(Dispatchers.IO) {
        delay(1000L) // 네트워크 요청
        return@async "Dummy Response" // 결괏값 반환
    }
    val result = networkDeferred.await() // networkDeferred로부터 결괎값이 반환될 때까지 runBlocking 일시 중단
    println(result) // "Dummy Response" 출력
}
```


## 5.2 Deferred는 특수한 형태의 Job이다

- 모든 코루틴 빌더는 `Job` 객체를 생성한다.
- `async` 코루틴 빌더가 생성하는 `Deferred` 객체는 `Job` 인터페이스의 서브타입으로 선언된 인터페이스이다.
- `Deferred` 객체는 코루틴으로부터 결괏값 수신을 위해 `Job` 객체에서 몇 가지 기능이 추가됐을 뿐, 여전히 `Job` 객체의 일종이다.
- 즉, `Deferred` 객체는 `Job` 객체의 모든 기능을 가지고 있기 때문에, `Job`의 `join`, `cancel` 등의 함수와 `isActive`, `isCompleted` 등의 프로퍼티를 모두 사용할 수 있다.

```kotlin
interface Deferred<out T> : Job {
    suspend fun await(): T
    // ...
}
```

## 5.3 복수의 코루틴으로부터 결괏값 수신하기

여러 비동기 작업으로부터 만들어진 결괏값들을 병합하는 방법에 대해 알아본다.

### 5.3.1. await를 사용해 복수의 코루틴으로부터 결괏값 수신하기

콘서트 관람객을 2개의 플랫폼에서 모집하는 상황을 가정한다.

이러한 경우 각 플랫폼에 등록된 관람객을 조회한 후 결과를 병합하여 출력해야 한다.

#### 방법 1

```kotlin
fun main() = runBlocking<Unit> {
    val timer = ElapsedTimer().apply { start() } // 1. 시간 측정 시작

    val participantDeferred1: Deferred<List<String>> = async(Dispatchers.IO) {
        // 2. 플랫폼1에서 등록한 관람객 목록을 가져오는 코루틴
        delay(1000L)
        return@async listOf("James", "Jason")
    }
    val participant1 = participantDeferred1.await() // 3. 결과가 수신될 떄까지 대기

    val participantDeferred2: Deferred<List<String>> = async(Dispatchers.IO) {
        // 4. 플랫폼2에서 등록한 관람객 목록을 가져오는 코루틴
        delay(1000L)
        return@async listOf("Jenny")
    }
    val participant2 = participantDeferred2.await() // 5. 결과가 수신될 떄까지 대기

    println("${timer.elapsedTime()} 참여자 목록: ${participant1 + participant2}") // 6. 지난 시간 표시 및 참여자 목록을 병합하여 출력
}
```

- 위 코드는 두 개의 코루틴이 **순차적으로 실행**되와, 결과적으로 **총 2초**의 시간이 소요된다.
- 그 이유는, `deferred1.await()` 함수를 호출하는 부분에서 `deferred` 코루틴이 모두 완료될 때까지 `runBlocking` 코루틴이 일시 중단되기 때문이다.

#### 방법 2

```kotlin
fun main() = runBlocking<Unit> {
    val timer = ElapsedTimer().apply { start() } // 1. 시간 측정 시작
    val participantDeferred1: Deferred<List<String>> = async(Dispatchers.IO) {
        // 2. 플랫폼1에서 등록한 관람객 목록을 가져오는 코루틴
        delay(1000L)
        return@async listOf("James", "Jason")
    }
    val participantDeferred2: Deferred<List<String>> = async(Dispatchers.IO) {
        // 3. 플랫폼2에서 등록한 관람객 목록을 가져오는 코루틴
        delay(1000L)
        return@async listOf("Jenny")
    }
    val participant1 = participantDeferred1.await() // 4. 결과가 수신될 떄까지 대기
    val participant2 = participantDeferred2.await() // 5. 결과가 수신될 떄까지 대기

    println("${timer.elapsedTime()} 참여자 목록: ${participant1 + participant2}") // 6. 지난 시간 표시 및 참여자 목록을 병합하여 출력
}
```

- 위 코드는 두 개의 코루틴이 **병렬적으로 실행**되어, 결과적으로 **총 1초**의 시간이 소요된다.
- [방법 1]과 다른 점은, `deferred1`과 `deferred2`가 모두 실횡된 이후에 `await` 함수를 호출하였기 때문에 두 개의 코루틴이 **병렬적으로 실행**이 가능한 것이다.

### 5.3.2. awaitAll, Collection.awaitAll 사용하기

앞의 예시에서 처럼 여러 개의 `Deferred.await` 함수를 호출할 때, `awaitAll` 함수를 사용하면 코드를 더 간결하게 작성할 수 있다.

```kotlin
suspend fun <T> awaitAll(vararg deferreds: Deferred<T>): List<T>
```

`awaitAll()` 함수를 사용하는 코드 예시는 다음과 같다.

```kotlin
fun main() = runBlocking<Unit> {
    // 상기 코드와 동일
    
    val results: List<List<String>> =
        awaitAll(participantDeferred1, participantDeferred2) // 모든 요청이 끝날 때까지 대기

    println("${timer.elapsedTime()} 참여자 목록: ${results.flatten()}")
}
```

코루틴 라이브러리는 `awaitAll` 함수를 `Collection` 인터페이스에 대한 확장 함수로도 제공한다.

```kotlin
suspend fun <T> Collection<Deferred<T>>.awaitAll(): List<T>
```

`awaitAll()` 함수를 사용하는 코드 예시는 다음과 같다.

```kotlin
fun main() = runBlocking<Unit> {
    // 상기 코드와 동일

    val results: List<List<String>> = listOf(participantDeferred1, participantDeferred2)
        .awaitAll() // 모든 요청이 끝날 때까지 대기

    println("${timer.elapsedTime()} 참여자 목록: ${results.flatten()}")
}
```


## 5.4. withContext

### 5.4.1. withContext로 async-await 대체하기

```kotlin
suspend fun <T> withContext(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T
```

코루틴 라이브러리에서 제공되는 `withContext` 함수를 사용하면, `async`와 `await` 함수를 사용하지 않고도 코루틴을 생성하고 결괏값을 수신할 수 있다.

```kotlin
// async-await를 사용한 코드
fun main() = runBlocking<Unit> {
    val deferred: Deferred<String> = async(Dispatchers.IO) {
        delay(1000L)
        return@async "Dummy Response"
    }
    val result = deferred.await()
    println(result)
}
```
```kotlin
// withContext를 사용한 코드
fun main() = runBlocking<Unit> {
    val result: String = withContext(Dispatchers.IO) {
        delay(1000L)
        return@withContext "Dummy Response"
    }
    println(result)
}
```

위 두 코드는 동일한 결과를 출력하며, 동작 방식 또한 매우 유사하다.

### 5.4.2. withContext의 동작 방식

위 예시에서 처럼 `withContext` 함수와 `async-await` 함수는 겉보기에는 매우 유사하게 동작하는 것처럼 보이지만, 내부적으로는 다르게 동작한다.

```kotlin
fun main() = runBlocking<Unit> {
    println("[${Thread.currentThread().name}] runBlocking 블록 실행")
    withContext(Dispatchers.IO) {
        println("[${Thread.currentThread().name}] withContext 블록 실행")
    }
}
// 결과:
// [main @coroutine#1] runBlocking 블록 실행
// [DefaultDispatcher-worker-1 @coroutine#1] withContext 블록 실행
```



- 위 코드의 실행 결과를 보면, 코루틴이 실행되는 **스레드(CoroutineContext)는 변경**되었지만, 코루틴은 여전히 **동일한 코루틴**이라는 것을 알 수 있다.
- `withContext` 함수는 기존의 코루틴에서 `CoroutineContext` 객체만 바꿔서 실행한다.
- 다시 말해, `withContext` 함수가 호출되면, 실행 중인 코루틴의 실행 환경이 `withContext` 함수의 인자로 전달된 `context` 값으로 변경돼 실행되며, 이를 컨텍스트 스위칭(Context Switching)이라고 한다.

<div align="center" style="display:flex; align-items: center; justify-content: center; gap: 20px;">
<img height="200px" src="https://github.com/user-attachments/assets/6f015511-5152-4111-af0d-12ae9517271e">
<img height="200px" src="https://github.com/user-attachments/assets/f0ea0196-7796-4a3e-98c3-47f0245830a8">
</div>

```kotlin
fun main() = runBlocking<Unit> {
    println("[${Thread.currentThread().name}] runBlocking 블록 실행")
    async(Dispatchers.IO) {
        println("[${Thread.currentThread().name}] async 블록 실행")
    }.await()
}
// 결과:
// [main @coroutine#1] runBlocking 블록 실행
// [DefaultDispatcher-worker-1 @coroutine#2] async 블록 실행
```

위 코드의 실행 결괏값을 보면, `async` 함수를 사용한 경우에는 **코루틴이 새로 생성**되어 실행되는 것을 알 수 있다.

### 5.4.3. withContext 사용 시 주의점

- `withContext` 함수는 새로운 코루틴을 만들지 않기 때문에, 하나의 코루틴에서 `withContext` 함수가 여러 번 호출되더라도 순차적으로 실행된다.
- 즉, 다수의 작업을 병렬로 실행하고 싶다면 `async-await` 함수를 사용해야 한다.


## 5.5. 요약

1. `async` 함수를 사용해 코루틴을 실행하면 코루틴의 결과를 감싸는 `Deferred` 객체를 반한받는다.
2. `Deferred`는 `Job`의 서브타입으로 `Job` 객체에 결괏값을 감싸는 기능이 추가된 객체이다.
3. `Deferred` 객체에 대해 `await` 함수를 호출하면 결괏값을 반환받을 수 있다.  
   `await` 함수를 호출한 코루틴은 `Deferred` 객체가 결괏값을 반환할 때까지 일시 중단 후 대기한다.
4. `awaitAll` 함수를 사용해 복수의 `Deferred` 코루틴이 결괏값을 반환할 때까지 대기할 수 있다. 
5. `awaitAll` 함수는 컬렉션에 대한 확장 함수로도 제공된다.
6. `withContext` 함수를 사용해 `async-await` 쌍을 대체할 수 있다.
7. `withContext` 함수는 코루틴을 새로 생성하지 않는다. 코루틴의 실행 환경을 담는 `CoroutineContext`만 변경해 코루틴을 실행하므로 이를 활용해 코루틴이 실행되는 스레드를 변경할 수 있다.
8. `withContext` 함수는 코루틴을 새로 생성하지 않으므로 병렬로 실행돼야 하는 북수의 작업을 `withContext`로 감싸 실행하면 순차적으로 실행된다. 이럴 때는 `withContext` 대신 `async`를 사용해 작업이 병렬로 실행될 수 있도록 해야 한다.
9. `withContext`로 인해 실행 환경이 변경돼 실행되는 코투틴은 `withContext`의 작업을 모두 실행하면 다시 이전의 실행 환경으로 돌아온다. 
