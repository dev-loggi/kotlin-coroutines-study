# 7장. 구조화된 동시성


## 7.1. 실행 환경 상속

### 7.1.1. 부모 코루틴의 실행 환경 상속

- 부모 코루틴은 자식 코루틴에게 실행 환경(CoroutineContext)을 상속한다
- 코루틴 빌더 함수에서 CoroutineContext를 파라미터로 전달하지 않으면, 부모 코루틴의 CoroutineContext의 모든 구성 요소를 그대로 상속 받는다.

```kotlin
fun main() = runBlocking<Unit> {
    val coroutineContext = newSingleThreadContext("MyThread") + CoroutineName("CoroutineA")

    launch(coroutineContext) { // 부모 코루틴 생성
        println("${Thread.currentThread().name} 부모 코루틴 실행")
        
        launch {  // 자식 코루틴 생성
            println("${Thread.currentThread().name} 자식 코루틴 실행")
        }
    }
}
```
```text
// 실행 결과:
[MyThread @CoroutineA#2] 부모 코루틴 실행
[MyThread @CoroutineA#3] 자식 코루틴 실행
```

[그림 7-2]

### 7.1.2. 실행 환경 덮어씌우기

- 부모 코루틴의 모든 실행 환경이 항상 자식 코루틴에게 상속되지는 않는다.
- 자식 코루틴을 생성하는 코루틴 빌더 함수로 새로운 CoroutineContext 객체가 전달되면, 부모 코루틴에게서 전달 받은 CoroutineContext 구성 요소들은 자식 코루틴 빌더 함수로 전달된 CoroutineContext 객체의 구성 요소들로 덮어씌워진다
- 상속되는 CoroutineContext 구성 요소들 중 Job 객체는 상속되지 않는다.

```kotlin
fun main() = runBlocking<Unit> {
    val coroutineContext = newSingleThreadContext("MyThread") + CoroutineName("ParentCoroutine")

    launch(coroutineContext) { // 부모 코루틴 생성
        println("${Thread.currentThread().name} 부모 코루틴 실행")

        launch(CoroutineName("ChildCoroutine")) {  // 자식 코루틴 생성
            println("${Thread.currentThread().name} 자식 코루틴 실행")
        }
    }
}
```
```text
// 실행 결과:
[MyThread @ParentCoroutine#2] 부모 코루틴 실행
[MyThread @ChildCoroutine#3] 자식 코루틴 실행
```
[그림 7-3]

### 7.1.3. 상속되지 않는 Job

- `launch`나 `async`를 포함한 모든 코루틴 빌더 함수는 호출 때마다 코루틴 추상체인 `Job` 객체를 새롭게 생성한다
- 그 이유는, 코루틴을 제어 하는 `Job` 객체를 상속 받게 되면 각각의 개별 코루틴들을 제어할 수 없게 되기 때문이다

```kotlin
fun main() = runBlocking<Unit> {
    val runBlockingJob = coroutineContext[Job] // 부모 코루틴의 CoroutineContext로부터 부모 코루틴의 Job 추출

    launch {
        val launchJob = coroutineContext[Job] // 자식 코루틴의 CoroutineContext로부터 자식 코루틴의 Job 추출

        println("runBlockingJob === launchJob :: ${runBlockingJob === launchJob}")
    }
}
```
```text
// 실행 결과:
runBlockingJob === launchJob :: false
```

### 7.1.4. 구조화에 사용되는 Job

- 부모 코루틴과 자식 코루틴은 서로 다른 `Job` 객체를 가진다.
- 부모 코루틴과 자식 코루틴의 `Job`은 코루틴의 구조화를 위해 `[그림 7-4]`와 같이 서로 양방향 참조를 가진다.
  - `Job.children: Sequence<Job>`: 부모 코루틴에서 자식 코루틴들의 `Job`에 대한 시퀀스
  - `Job.parent: Job?`: 자식 코루틴에서 부모 코루틴의 `Job`에 대한 참조
- `Job.parent: Job?`가 `nullable` 타입인 이유는, 루트 코루틴인 경우 부모 코루틴이 없기 때문이다.
- 이처럼, `Job`은 코루틴의 구조화에 핵심적인 역할을 한다.

```kotlin
fun main() = runBlocking<Unit> {
    val parentJob = coroutineContext[Job]

    launch {
        val childJob = coroutineContext[Job]

        println("1. 부모 코루틴과 자식 코루틴의 Job은 같은가? ${parentJob === childJob}")
        println("2. 자식 코루틴의 Job이 가지고 있는 parent는 부모 코루틴의 Job인가? ${childJob?.parent === parentJob}")
        println("3. 부모 코루틴의 Job은 자식 코루틴의 Job에 대한 참조를 가지는가? ${parentJob?.children?.contains(childJob)}")
    }
}
```
```text
// 실행 결과:
1. 부모 코루틴과 자식 코루틴의 Job은 같은가? false
2. 자식 코루틴의 Job이 가지고 있는 parent는 부모 코루틴의 Job인가? true
3. 부모 코루틴의 Job은 자식 코루틴의 Job에 대한 참조를 가지는가? true
```

## 7.2. 코루틴의 구조화와 작업 제어

코루틴의 구조화는 하나의 큰 비동기 작업을 작은 비동기 작업으로 나눌 때 일어난다.  
다음은 3개의 서버로부터 데이터를 다운로드하고, 그 후에 합쳐진 데이터를 변환하는 비동기 작업을 구조화된 코루틴으로 나누는 예시이다:

[그림 7-6]

- 코루틴을 구조화하는 가장 중요한 이유는 **코루틴을 안전하게 관리하고 제어**하기 위함이다

구조화된 코루틴은 안전하게 제어하기 위해 다음 두 가지 특성을 가진다: 

1. 코루틴으로 취소가 요청되면, 자식 코루틴으로 전파된다.
2. 부모 코루틴은 모든 자식 코루틴의 실행이 완료되어야, 완료될 수 있다.

### 7.2.1. 취소의 전파

- 코루틴은 자식 코루틴으로 취소를 전파하는 특성을 갖기 때문에, 특정 코루틴이 취소되면 하위의 모든 코루틴이 취소된다
- 그 이유는 자식 코루틴이 부모 코루틴 작업의 일부이기 때문이다

[그림 7-8] [그림 7-9]
 
다음 코드는, 3개의 데이터베이스로부터 데이터를 가져와 합치는 작업에서 코루틴의 취소를 전파하는 예시이다:

```kotlin
fun main() = runBlocking<Unit> {
    val parentJob = launch(Dispatchers.IO) { // 부모 코루틴 생성
        val dbResultsDeferred: List<Deferred<String>> = listOf("db1", "db2", "db3").map {
            async { // 자식 코루틴 생성
                delay(1000L)
                println("${it}으로부터 데이터를 가져오는데 성공했습니다")
                return@async "[${it}]data"
            }
        }
        val dbResults: List<String> = dbResultsDeferred.awaitAll() // 모든 코루틴이 완료될 때까지 대기

        println(dbResults)
    }
    // parentJob.cancel() // 부모 코루틴 취소
}
```
```text
// 실행 결과:
db2으로부터 데이터를 가져오는데 성공했습니다
db3으로부터 데이터를 가져오는데 성공했습니다
db1으로부터 데이터를 가져오는데 성공했습니다
[[db1]data, [db2]data, [db3]data]
```

> 위 코드에서 부모 코루틴을 취소하는 부분(`// parentJob.cancel()`)의 주석을 해제하면, 부모 코루틴으로부터 모든 자식 코루틴 작업이 곧바로 취소되기 때문에 아무것도 출력되지 않고 프로세스가 종료된다.

### 7.2.2. 부모 코루틴의 자식 코루틴에 대한 완료 의존성

부모 코루틴은 모든 자식 코루틴이 실행 완료되어야 완료될 수 있다.  
이를 **부모 코루틴이 자식 코루틴에 대해 완료 의존성을 가진다**고 한다.

```kotlin
fun main() = runBlocking<Unit> {
    val timer = ElapsedTimer().start()
    val parentJob = launch { // 부모 코루틴 실행
        launch { // 자식 코루틴 실행
            delay(1000L)
            println("${timer.elapsedTime()} 자식 코루틴 실행 완료")
        }
        println("${timer.elapsedTime()} 부모 코루틴이 실행하는 마지막 코드")
    }
    parentJob.invokeOnCompletion { // 부모 코루틴이 종료될 시 호출되는 콜백 등록
        println("${timer.elapsedTime()} 부모 코루틴 실행 완료")
    }
}
```
```text
// 실행 결과:
[지난 시간: 5ms] 부모 코루틴이 실행하는 마지막 코드
[지난 시간: 1028ms] 자식 코루틴 실행 완료
[지난 시간: 1029ms] 부모 코루틴 실행 완료
```

위 코드와 같이 부모 코루틴의 코드는 `5ms`만에 실행이 완료되었지만, 자식 코루틴의 실행이 완료된 후에 `Completion` 콜백이 호출되는 것을 확인할 수 있다.

#### 7.2.2.1 실행 완료 중 상태

[그림 7-13]

- `실행 완료 중` 상태란, 부모 코루틴의 모든 코드가 실행됐지만 자식 코루틴이 실행 중인 경우 부모 코루틴이 갖는 상태이다
- 자식 코루틴들이 모두 실행이 완료되면 자동으로 `실행 완료` 상태로 바뀐다

#### 7.2.2.2 실행 완료 중 상태의 Job의 상태 값

그렇다면, `실행 완료 중`인 코루틴 `Job` 객체의 실제 상태 값은 무엇인지 확인해보자

```kotlin
fun main() = runBlocking<Unit> {
    val timer = ElapsedTimer().start()
    val parentJob = launch { // 부모 코루틴 실행
        launch { // 자식 코루틴 실행
            delay(1000L)
            println("${timer.elapsedTime()} 자식 코루틴 실행 완료")
        }
        println("${timer.elapsedTime()} 부모 코루틴이 실행하는 마지막 코드")
    }
    parentJob.invokeOnCompletion { // 부모 코루틴이 종료될 시 호출되는 콜백 등록
        println("${timer.elapsedTime()} 부모 코루틴 실행 완료")
    }
    delay(500L)
    parentJob.printStates() // parentJob의 상태 출력
}
```
```text
// 실행 결과:
[지난 시간: 6ms] 부모 코루틴이 실행하는 마지막 코드
isActive    >> true
isCompleted >> false
isCancelled >> false
[지난 시간: 1025ms] 자식 코루틴 실행 완료
[지난 시간: 1027ms] 부모 코루틴 실행 완료
```

위 실행 결과와 같이 `parentJob`은 현재 아직 실행 중이므로 `isActive`는 `true`이고, `isCompleted`는 `false`이다.

[표 7-2]

위 상태표와 같이 `실행 완료 중` 상태는 `실행 중` 상태와 같은 결과를 보여준다.  
즉, `Job` 객체의 상태 값으로는 `실행 중`과 `실행 완료 중` 상태를 구분할 수는 없지만, 구조화된 코루틴의 실행 흐름을 이해하기 위해서는 **자식 코루틴이 실행 완료되지 않으면 부모 코루틴도 실행 완료될 수 없다는 점을 이해하는 것이 중요**하다.

## 7.3. CoroutineScope 사용해 코루틴 관리하기

CoroutineScope 객체는 자신의 범위 내에서 생성된 코루틴들에게 실행 환경을 제공하고, 이들의 실행 범위를 관리하는 역할을 한다.

### 7.3.1. CoroutineScope 생성하기

#### 1) CoroutineScope 인터페이스 구현을 통한 생성

```kotlin
class CustomCoroutineScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + newSingleThreadContext("CustomScopeThread")
}

fun main() {
    CustomCoroutineScope().launch {
        delay(100L)
        println("[${Thread.currentThread().name}] 코루틴 실행 완료")
    }
    Thread.sleep(1000L) // 코드 종료 방지
}
```
```text
// 실행 결과:
[CustomScopeThread @coroutine#1] 코루틴 실행 완료
```

#### 2) CoroutineScope 함수를 사용해 생성

```kotlin
public fun CoroutineScope(context: CoroutineContext): CoroutineScope =
    ContextScope(if (context[Job] != null) context else context + Job())
```

- `CoroutineScope` 함수는 `CoroutineScope` 인터페이스를 구현한 `ContextScope` 객체를 통해 생성한다

```kotlin
fun main() {
    CoroutineScope(Dispatchers.IO).launch {
        delay(100L)
        println("[${Thread.currentThread().name}] 코루틴 실행 완료")
    }
    Thread.sleep(1000L) // 코드 종료 방지
}
```
```text
// 실행 결과:
[DefaultDispatcher-worker-2 @coroutine#1] 코루틴 실행 완료
```

### 7.3.2. 코루틴에게 실행 환경을 제공하는 CoroutineScope

```kotlin
public fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    // ...
}
```

`launch` 코루틴 빌더 함수는 `CoroutineScope`의 확장 함수로 선언되어 있으며, `launch` 함수가 호출되면 다음 과정을 통해 `CoroutineScope` 객체로부터 실행 환경을 제공받아 코루틴의 실행 환경을 설정한다.

1) 수신 객체인 `CoroutineScope`로부터 `CoroutineConext` 객체를 제공받는다
2) 제공받은 `CoroutineContext` 객체에 `launch` 함수의 `context 인자로 넘어온 `CoroutineContext`를 더한다
3) 생성된 `CoroutineContext` 객체에 `launch` 함수의 `context` 인자로 넘어온 `CoroutineContext`를 더한다

위 과정을 코드로 살펴보자.

```kotlin
fun main() {
    val newScope = CoroutineScope(CoroutineName("MyCoroutine") + Dispatchers.IO)

    newScope.launch(CoroutineName("LaunchCoroutine")) {
        println(coroutineContext[CoroutineName])
        println(coroutineContext[CoroutineDispatcher])
        val launchJob = coroutineContext[Job]
        val newScopeJob = newScope.coroutineContext[Job]
        println("launchJob?.parent === newScopeJob >> ${launchJob?.parent === newScopeJob}")
    }
    Thread.sleep(1000L)
}
```
```text
// 실행 결과:
CoroutineName(LaunchCoroutine)
Dispatchers.IO
launchJob?.parent === newScopeJob >> true
```

### 7.3.3. CoroutineScope에 속한 코루틴의 범위

#### 1) CoroutineScope에 속한 코루틴의 범위

[그림 7-19] [그림 7-20]

코루틴 빌더 람다식에서 수신 객체로 제공되는 `CoroutineScope` 객체는, 코루틴 빌더로 생성되는 코루틴과 람다식 내에서 `CoroutineScope` 객체를 사용해 실행되는 모든 코루틴을 포함한다.

#### 2) CoroutineScope를 새로 생성해 기존 CoroutineScope 범위에서 벗어나기

특정 코루틴만 기존에 존재하던 `CoroutineScope` 객체의 범위에서 벗어나게 만들려면, 새로운 `CoroutineScope` 객체를 생성해 사용하면 된다.

```kotlin
fun main() = runBlocking<Unit> {
    launch(CoroutineName("Coroutine1")) {
        launch(CoroutineName("Coroutine3")) {
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        CoroutineScope(Dispatchers.IO).launch(CoroutineName("Coroutine4")) {
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        launch(CoroutineName("Coroutine2")) {
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        println("[${Thread.currentThread().name}] 코루틴 실행")
    }
}
```
```text
// 실행 결과:
[DefaultDispatcher-worker-1 @Coroutine4#4] 코루틴 실행
[main @Coroutine1#2] 코루틴 실행
[main @Coroutine3#3] 코루틴 실행
[main @Coroutine2#5] 코루틴 실행
```

위 코드는 아래 그림과 같은 코루틴 계층 구조를 가진다.

[그림 7-22]

- 이와 같이 `CoroutineScope` 객체에 의해 관리되는 코루틴의 범위와 범위를 만드는 것은 `Job` 객체이다.

> 코루틴의 구조화를 깨는 것은 비동기 작업을 안전하지 않게 만들기 때문에 **최대한 지양**해야 한다.

### 7.3.4. CoroutineScope 취소하기

- `CoroutineScope` 인터페이스는 확장 함수로 `cancel` 함수를 지원한다
- `calcel` 함수는 `CoroutineScope` 객체의 범위에 속한 모든 코루틴을 취소하는 함수이다

```kotlin
fun main() = runBlocking<Unit> {
    launch(CoroutineName("Coroutine1")) {
        launch(CoroutineName("Coroutine3")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        launch(CoroutineName("Coroutine4")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        this.cancel() // Coroutine1의 CoroutineScope에 취소 요청
    }
    launch(CoroutineName("Coroutine2")) {
        delay(100L)
        println("[${Thread.currentThread().name}] 코루틴 실행")
    }
}
```
```text
// 실행 결과:
[main @Coroutine2#3] 코루틴 실행
```

- `CoroutineScope.cancel()` 함수는 자신의 `CoroutineContext`에서 `Job` 객체를 찾아 `Job.cancel()` 함수를 호출한다
- 즉, `CoroutineScope.cancel()` 함수는 `Job.cancel()` 함수를 호출하는 것과 같으며, 취소를 전파하는 원리도 같다

```kotlin
public fun CoroutineScope.cancel(cause: CancellationException? = null) {
    val job = coroutineContext[Job] ?: error("Scope cannot be cancelled because it does not have a job: $this")
    job.cancel(cause)
}
```

### 7.3.5. CoroutineScope 활성화 상태 확인하기

```kotlin
public val CoroutineScope.isActive: Boolean
    get() = coroutineContext[Job]?.isActive ?: true
```

- `CoroutineScope` 객체는 현재 활성화 되어 있는지 여부를 확인하는 `isActive` 확장 프로퍼티를 제공한다.
- 이도 마찬가지로 `Job.isActive` 프로퍼티를 호출하는 것과 같다

## 7.4. 구조화와 Job

### 7.4.1. runBlocking과 루트 Job

```kotlin
fun main() = runBlocking<Unit> {
    launch(CoroutineName("Coroutine1")) {
        launch(CoroutineName("Coroutine3")) { }
        launch(CoroutineName("Coroutine4")) { }
    }
    launch(CoroutineName("Coroutine2")) {
        launch(CoroutineName("Coroutine5")) { }
    }
    delay(1000L)
}
```

- 위 코드에서 루트 코루틴은 `runBlocking` 함수로 생성된 코루틴이다
- `runBlocking` 코루틴의 `Job` 객체는 루트 코루틴이기 때문에 `parent`가 `null`이다

위 코드에서 생성되는 코루틴은 아래 그림과 같은 계층 구조를 가진다.

[그림 7-25]

### 7.4.2. Job 구조화 깨기

#### 1) CoroutineScope 사용해 구조화 깨기

```kotlin
fun main() = runBlocking<Unit> { // 루트 Job 생성
    val newScope = CoroutineScope(Dispatchers.IO) // 새로운 루트 Job 생성

    newScope.launch(CoroutineName("Coroutine1")) {
        launch(CoroutineName("Coroutine3")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        launch(CoroutineName("Coroutine4")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
    }
    newScope.launch(CoroutineName("Coroutine2")) {
        launch(CoroutineName("Coroutine5")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
    }
    // delay(1000L) // 1초간 대기
}
```

- 위 코드에서 `runBlocking` 함수를 통해 루트 `Job`이 생성되지만, `CoroutineScope` 함수를 통해 새로운 루트 `Job`을 가진 `newScope`가 생성된다

[그림 7-26]

- 위 그림과 같이 구조화가 깨졌기 때문에 `// delay(1000L)` 코드를 주석 처리하면, `newScope`에 있는 코루틴들의 실행이 완료되지 못한 채 프로세스가 종료된다

#### 7.4.4.2 Job 사용해 구조화 깨기 

```kotlin
fun main() = runBlocking<Unit> { // 루트 Job 생성
    val newRootJob = Job() // 새로운 루트 Job 생성

    launch(CoroutineName("Coroutine1") + newRootJob) {
        launch(CoroutineName("Coroutine3")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        launch(CoroutineName("Coroutine4")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
    }
    launch(CoroutineName("Coroutine2") + newRootJob) {
        launch(CoroutineName("Coroutine5")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
    }
    // delay(1000L) // 1초간 대기
}
```

- 이번에는 `Job` 객체를 직접 생성하여 코루틴의 구조화를 깨뜨렸으며, 원리는 `CoroutineScope`를 사용한 경우와 동일하다

### 7.4.3. Job 사용해 일부 코루틴만 취소되지 않게 만들기

- 새로 `Job` 객체를 생성해 계층 구조를 끊음으로써 일부 코루틴만 취소되지 않도록 설정할 수 있다

```kotlin
fun main() = runBlocking<Unit> { // 루트 Job 생성
    val newRootJob = Job() // 새로운 루트 Job 생성

    launch(CoroutineName("Coroutine1") + newRootJob) {
        launch(CoroutineName("Coroutine3")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        launch(CoroutineName("Coroutine4")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
    }
    launch(CoroutineName("Coroutine2") + newRootJob) {
        launch(CoroutineName("Coroutine5") + Job()) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
    }
    delay(50L) // 모든 코루틴이 생성될 때까지 대기
    newRootJob.cancel() // 새로운 루트 Job 취소
    delay(1000L) // 1초간 대기
}
```
```text
// 실행 결과:
[main @Coroutine5#6] 코루틴 실행
```

이 경우 `Coroutine5`는 `newRootJob`과 계층 구조가 끊어지기 때문에 `newRootJob.cancel()`이 호출되어도 정상적으로 실행되는 것을 볼 수 있다.

[그림 7-30]

### 7.4.4. 생성된 Job의 부모를 명시적으로 설정하기

```kotlin
public fun Job(parent: Job? = null): CompletableJob = JobImpl(parent)
```

- `Job` 객체를 생성할 때 사용되는 `Job()` 함수에는 부모 `Job` 객체를 명시적으로 설정할 수 있는 `parent` 인자가 있다
- 즉, `parent` 인자로 부모 `Job` 객체를 전달하면, 구조화를 깨지 않을 수 있다

```kotlin
fun main() = runBlocking<Unit> {
    launch(CoroutineName("Coroutine1")) {
        val coroutine1Job = this.coroutineContext[Job] // Coroutine1의 Job
        val newJob = Job(parent = coroutine1Job)
        launch(CoroutineName("Coroutine2") + newJob) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
    }
}
```
```text
// 실행 결과:
[main @Coroutine2#3] 코루틴 실행
// 프로세스 종료 로그가 출력되지 않는다...
```

[그림 7-33]

### 7.4.5. 생성된 Job은 자동으로 실행 완료되지 않는다

- `launch` 함수를 통해 생성된 `Job` 객체는 더 이상 실행할 코드가 없고, 모든 자식 코루틴들이 실행 완료되면 자동으로 실행 완료된다.
- 하지만, `Job()` 생성 함수를 통해 직접 생성된 `Job` 객체는 자식 코루틴들이 모두 실행 완료되더라도 자동으로 실행 완료되지 않으며, 명시적으로 완료 함수인 `complete`를 호출해야 완료된다.

```kotlin
fun main() = runBlocking<Unit> {
    launch(CoroutineName("Coroutine1")) {
        val coroutine1Job = this.coroutineContext[Job] // Coroutine1의 Job
        val newJob = Job(parent = coroutine1Job)
        launch(CoroutineName("Coroutine2") + newJob) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        newJob.complete() // 명시적으로 완료 호출
    }
}
```
```text
// 실행 결과:
[main @Coroutine2#3] 코루틴 실행

Process finished with exit code 0
```

> #### 💡 runBlocking과 launch의 차이
> 
> - `runBlocking` 함수와 `launch` 함수는 모두 코루틴 빌더 함수이지만 호출부의 스레드를 사용하는 방법에 차이가 있다
> - 그러나, `runBlocking` 함수의 차단과 스레드 블로킹(Thread Blocking)에서의 차단과 다르다.
> - 스레드 블로킹은 해당 스레드를 어떤 작업에도 사용할 수 없도록 차단되는 것을 의미하고, `runBlocking` 함수의 차단은 `runBlocking` 코루틴과 그 자식 코루틴을 제외한 다른 작업이 스레드를 사용할 수 없음을 의미한다.
> - 즉, `runBlocking` 코루틴에 의해 호출부의 스레드가 배타적으로 사용된다는 것은 `runBlocking` 코루틴 하위에 생성된 코루틴도 그 호출부의 스레드를 사용할 수 있다는 의미이다.
