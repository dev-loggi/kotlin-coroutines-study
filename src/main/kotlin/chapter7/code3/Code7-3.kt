package org.example.chapter7.code3

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

fun main() = runBlocking<Unit> {
    val coroutineContext = newSingleThreadContext("MyThread") + CoroutineName("ParentCoroutine")

    launch(coroutineContext) { // 부모 코루틴 생성
        println("${Thread.currentThread().name} 부모 코루틴 실행")
        println("currentCoroutineContext()=${currentCoroutineContext()}")
        currentCoroutineContext().toString()

        launch(CoroutineName("ChildCoroutine")) {  // 자식 코루틴 생성
            println("${Thread.currentThread().name} 자식 코루틴 실행")
            println("currentCoroutineContext()=${currentCoroutineContext()}")

            println(coroutineContext[CoroutineName] === currentCoroutineContext()[CoroutineName])
            println(coroutineContext[CoroutineName] == currentCoroutineContext()[CoroutineName])
        }
    }
}