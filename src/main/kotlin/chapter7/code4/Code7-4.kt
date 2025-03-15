package org.example.chapter7.code4

import kotlinx.coroutines.*

fun main() = runBlocking<Unit> {
    val runBlockingJob = coroutineContext[Job] // 부모 코루틴의 CoroutineContext로부터 부모 코루틴의 Job 추출

    launch {
        val launchJob = coroutineContext[Job] // 자식 코루틴의 CoroutineContext로부터 자식 코루틴의 Job 추출

        println("runBlockingJob === launchJob :: ${runBlockingJob === launchJob}")
    }
}

fun main2() = runBlocking<Unit> {
    val parentJob = Job()
    val parentContext = newSingleThreadContext("ParentThread") + parentJob
    println("11")

    launch(parentContext) {
        val childContext = parentJob
        println("22")

        launch(childContext) {
            println("33")
            val childJob = coroutineContext[Job]
            println("parentJob === childJob :: ${parentJob === childJob}")
        }
        delay(1000)
    }
}