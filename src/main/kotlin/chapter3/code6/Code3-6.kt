package org.example.chapter3.code6

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking

/**
 * 3.4.2. 부모 코루틴의 CoroutineDispatcher 사용해 자식 코루틴 실행하기 (page. 100)
 */
@OptIn(DelicateCoroutinesApi::class)
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