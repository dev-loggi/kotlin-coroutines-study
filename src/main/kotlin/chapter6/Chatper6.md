# 6장. CoroutineContext

CoroutineContext는 코루틴을 실행하는 실행 환경을 설정하고 관리하는 인터페이스이다.

> **6장에서 다루는 내용**
> - CoroutineContext의 구성 요소
> - CoroutineContext 구성 방법
> - CoroutineContext 구성 요소에 접근하기
> - CoroutineContext 구성 요소 제거하는 방법

## 6.1. CoroutineContext의 구성 요소

CoroutineContext 객체가 가지는 네 가지 주요 구성 요소는 다음과 같다.

1. **CoroutineName**: 코루틴의 이름을 설정한다
2. **CoroutineDispatcher**: 코루틴을 스레드에 할당해 실행한다
3. **Job**: 코루틴의 추상체로 코루틴을 조작하는 데 사용된다
4. **CoroutineExceptionHandler**: 코루틴에서 발생한 예외를 처리한다

_(이외에도 다양한 구성 요소가 있지만, 이 중에서 가장 많이 사용되는 구성 요소들이다.)_


## 6.2. CoroutineContext 구성하기

### 6.2.1. CoroutineContext가 구성 요소를 관리하는 방법

<div align="center">
  <img height="300px" src="https://github.com/user-attachments/assets/d62ca526-ea42-4231-ba17-ea8016dcf463">
</div>

- CoroutineContext 객체는 키-값 쌍으로 구성된다.
- 각 구성 요소는 고유한 키를 가지며, 키에 대해 중복된 값은 허용되지 않는다.
- 즉, 하나의 키당 하나의 값만 가질 수 있다.

### 6.2.2. CoroutineContext 구성

CoroutineContext의 구성 요소는 객체 간의 `+` 연산자를 사용해 CoroutineContext 객체를 구성한다.

```kotlin
fun main() = runBlocking<Unit> {
    val coroutineContext: CoroutineContext = newSingleThreadContext("MyThread") + CoroutineName("MyCoroutine")

    launch(context = coroutineContext) {
        println("[${Thread.currentThread().name}] launch 실행")
    }
}
// 결과:
// [MyThread @MyCoroutine#2] launch 실행
```

위 코드에서 만들어진 `coroutineContext` 객체는 다음 표와 같이 구성된다.

<div align="center">
  <img height="300px" src="https://github.com/user-attachments/assets/5a0c4563-08a9-4016-bc29-071ce1345e65">
</div>

> 구성 요소가 없는 CoroutineContext는 EmptyCoroutineContext를 통해 만들 수 있다.

### 6.2.3. CoroutineContext 구성 요소 덮어 씌우기

CoroutineContext 객체를 구성할 때, 동일한 키를 가진 구성 요소가 이미 존재할 경우 경우, **마지막에 추가된 구성 요소**가 이전 구성 요소를 **덮어씌운다**.

```kotlin
fun main() = runBlocking<Unit> {
    val coroutineContext: CoroutineContext = newSingleThreadContext("MyThread") + CoroutineName("MyCoroutine")
    val newCoroutineContext: CoroutineContext = coroutineContext + CoroutineName("NewCoroutine")

    launch(context = coroutineContext) {
        println("[${Thread.currentThread().name}] launch 실행")
    }
}
// 결과:
// [MyThread @NewCoroutine#2] launch 실행
```

### 6.2.4. 여러 구성 요소로 이뤄진 CoroutineContext 합치기

여러 개의 구성 요소로 이뤄진 CoroutineContext 객체를 합치려면, `+` 연산자를 사용해 각 구성 요소를 추가하면 된다.

```kotlin
fun main() = runBlocking<Unit> {
    val coroutineContext1 = newSingleThreadContext("MyThread1") + CoroutineName("MyCoroutine1")
    val coroutineContext2 = newSingleThreadContext("MyThread2") + CoroutineName("MyCoroutine2")
    val combinedCoroutineContext = coroutineContext1 + coroutineContext2
    
    launch(context = combinedCoroutineContext) {
        println("[${Thread.currentThread().name}] launch 실행")
    }
}
// 결과:
// [MyThread2 @MyCoroutine2#2] launch 실행
```

### 6.2.5. CoroutineContext에 Job 생성해 추가하기

`Job` 객체는 기본적으로 `launch`나 runBlocking 같은 코루틴 빌더 함수를 통해 자동으로 생성되지만, `Job()`을 호출해 직접 생성할 수도 있다.

```kotlin
val myJob = Job()
val coroutineContext = Dispatchers.IO + myJob
```

> Job 객체를 직접 생성하여 추가하면 코루틴의 구조화가 깨지기 때문에 주의할 필요가 있다.  
> 이와 관련된 내용은 "7장. 구조화된 동시성"에서 다룬다.


## 6.3. CoroutineContext 구성 요소에 접근하기

CoroutineContext 객체의 구성 요소에 접근하기 위해서는, 각 구성 요소의 고유한 키를 통해 접근한다.

### 6.3.1. CoroutineContext 구성 요소의 키

CoroutineContext 구성 요소의 키는 CoroutineContext.Key 인터페이스를 구현해 만들 수 있는데, 일반적으로 CoroutineContext 구성 요소는 자신의 내부에 키를 싱글톤 객체로 구현한다.

```kotlin
public data class CoroutineName(
    val name: String
) : AbstractCoroutineContextElement(CoroutineName) {
    public companion object Key : CoroutineContext.Key<CoroutineName>
    // ...
}
```
```kotlin
public interface Job : CoroutineContext.Element {
    public companion object Key : CoroutineContext.Key<Job>
    // ...
}
```
위와 같이 CoroutineName과 Job 클래스는 각각 클래스 내부에 CoroutineContext.Key 인터페이스를 구현하는 동반 객체(companion object)를 가지고 있다.

### 6.3.2. 키를 사용해 CoroutineContext 구성 요소에 접근하기

이번에는 해당 키를 사용해 각 CoroutineContext 구성 요소에 직접 코드로 접근해본다.

```kotlin
@OptIn(ExperimentalStdlibApi::class)
fun main() = runBlocking<Unit> {

    val coroutineName = CoroutineName("MyCoroutine")
    val coroutineDispatcher = Dispatchers.IO
    val coroutineContext = coroutineName + coroutineDispatcher

    // Key를 통해 CoroutineName 가져오기
    println("coroutineContextName=${coroutineContext[CoroutineName.Key]}")
    println("coroutineContextName=${coroutineContext[CoroutineName]}")
    println("coroutineContextName=${coroutineContext[coroutineName.key]}")

    // Key를 통해 CoroutineDispatcher 가져오기
    println("coroutineContextDispatcher=${coroutineContext[CoroutineDispatcher.Key]}")
    println("coroutineContextDispatcher=${coroutineContext[CoroutineDispatcher]}")
    println("coroutineContextDispatcher=${coroutineContext[Dispatchers.IO.key]}")
    println("coroutineContextDispatcher=${coroutineContext[coroutineDispatcher.key]}")
}
// 결과:
// coroutineContextName=CoroutineName(MyCoroutine)
// coroutineContextName=CoroutineName(MyCoroutine)
// coroutineContextName=CoroutineName(MyCoroutine)
// coroutineContextDispatcher=Dispatchers.IO
// coroutineContextDispatcher=Dispatchers.IO
// coroutineContextDispatcher=Dispatchers.IO
// coroutineContextDispatcher=Dispatchers.IO
```

- CoroutineContext 객체의 `get()` 함수의 인자로 키를 전달하면 해당 키에 해당하는 구성 요소를 반환한다.
- CoroutineContext 객체의 `key` 프로퍼티를 사용해 해당 구성 요소의 키를 가져올 수 있다.


## 6.4. CoroutineContext 구성 요소 제거하기

CoroutineContext 객체는 구성 요소를 제거하기 위한 `minusKey()` 함수를 제공한다.

```kotlin
@OptIn(ExperimentalStdlibApi::class)
fun main() = runBlocking<Unit> {
    val coroutineContext = CoroutineName("MyCoroutine") + Dispatchers.IO + Job()
    val deletedCoroutineContext = coroutineContext.minusKey(CoroutineName)

    println("""
        [coroutineContext]
        name       >> ${coroutineContext[CoroutineName]}
        dispatcher >> ${coroutineContext[CoroutineDispatcher]}
        job        >> ${coroutineContext[Job]}
        
        [deletedCoroutineContext]
        name       >> ${deletedCoroutineContext[CoroutineName]}
        dispatcher >> ${deletedCoroutineContext[CoroutineDispatcher]}
        job        >> ${deletedCoroutineContext[Job]}
    """.trimIndent())
}
// 결과:
// [coroutineContext]
// name       >> CoroutineName(MyCoroutine)
// dispatcher >> Dispatchers.IO
// job        >> JobImpl{Active}@1c53fd30

// [deletedCoroutineContext]
// name       >> null
// dispatcher >> Dispatchers.IO
// job        >> JobImpl{Active}@1c53fd30
```

- 위 코드와 같이 `minusKey()` 함수의 인자로 키를 넣으면, 해당 구성 요소가 제거되는 모습을 볼 수 있다.
- 단, 여기서 주의할 점은 `minusKey()` 함수는 **새로운 CoroutineContext 객체를 반환**한다는 것이다.
- 따라서, `minusKey()` 함수를 사용해 구성 요소를 제거하면, 기존의 CoroutineContext 객체는 변경되지 않는다.

## 6.5. 요약

1. CoroutineContext 객체는 코루틴의 실행 환경을 설정하고 관리하는 객체로 CoroutineDispatcher, CoroutineName, Job, CoroutineExceptionHandler 등의 객체를 조합해 코루틴 실행 환경을 정의한다.
2. CoroutineContext의 네 가지 주요한 구성 요소는 코루틴의 이름을 설정하는 CoroutineName 객체, 코루틴을 스레드로 보내 실행하는 CoroutineDispatcher 객체, 코루틴을 조작하는 데 사용하는 Job 객체, 코루틴의 예외를 처리하는 CoroutineExceptionHandler 객체이다.
3. CoroutineContext 객체는 키-값 쌍으로 구성 요소를 관리하며, 동일한 키에 대해 중복된 값을 허용하지 않는다. 따라서 각 구성 요소를 한 개씩만 가질 수 있다.
4. 더하기 연산자(`+`)를 사용해 CoroutineContext의 구성 요소를 조합할 수 있다.
5. 동일한 키를 가진 구성 요소가 여러 개 추가될 경우 나중에 추가된 구성 요소가 이전 값을 덮어씌운다. 즉, 마지막에 추가된 구성 요소만 유효하다.
6. 일반적으로 구성 요소의 동반 객체로 선언된 key 프로퍼티를 사용해 키 값에 접근할 수 있다. 예를 들어 CoroutineName의 키 값은 CoroutineName.Key를 통해 접근할 수 있다.
7. 키를 연산자 함수인 get과 함께 사용해 CoroutineContext 객체에 설정된 구성 요소에 접근할 수 있다. 예를 들어 CoroutineContext 객체인 coroutineContext의 CoroutineName 구성 요소에 접근하고 싶다면 `coroutineContext.get(CoroutineName.Key)`와 같이 사용하면 된다.
8. `get` 연산자 함수는 대괄호(`[]`)로 대체할 수 있다. 따라서 앞의 `coroutineContext.get(CoroutineName.Key)`는 `coroutineContext[CoroutineName.Key]`로 대체할 수 있다.
9. CoroutineName, CoroutineDispatcher, Job, CoroutineExceptionHandler는 동반 객체인 Key를 통해 CoroutineContext.Key를 구현하기 때문에 그 자체로 키를 사용할 수 있다. 따라서 `coroutineContext[CoroutineName]`은 `coroutineContext[CoroutineName.Key]`와 같은 연산을 한다.
10. CoroutineContext 객체의 `minusKey` 함수를 사용하면 CoroutineContext 객체에서 특정 구성 요소를 제거한 객체를 반환받을 수 있다.