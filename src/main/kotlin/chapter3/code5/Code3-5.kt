package org.example.chapter3.code5

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking

/**
 * 3.4.1. launch의 파라미터로 CoroutineDispatcher 사용하기 (page. 96)
 */
@OptIn(DelicateCoroutinesApi::class)
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