package org.example.chapter7.code2

import kotlinx.coroutines.*
import org.example.util.printThreadName

fun main() = runBlocking<Unit> {
    val coroutineContext = newSingleThreadContext("MyThread") + CoroutineName("CoroutineA")

    launch(coroutineContext) { // 부모 코루틴 생성
        println("${Thread.currentThread().name} 부모 코루틴 실행")
        println("currentCoroutineContext()=${currentCoroutineContext()}")

        launch {  // 자식 코루틴 생성
            println("${Thread.currentThread().name} 자식 코루틴 실행")
            println("currentCoroutineContext()=${currentCoroutineContext()}")
            println(coroutineContext[CoroutineName] === currentCoroutineContext()[CoroutineName])
            println(coroutineContext[CoroutineName] == currentCoroutineContext()[CoroutineName])
        }
    }
}