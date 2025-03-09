package org.example.chapter3.code3

import kotlinx.coroutines.*

/**
 * 3.4.1. launch의 파라미터로 CoroutineDispatcher 사용하기 (page. 96)
 */
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
fun main() = runBlocking<Unit> {
    val dispatcher = newSingleThreadContext(name = "SingleThread")

    launch(context = dispatcher) {
        println("[${Thread.currentThread().name}] 실행")
    }
}