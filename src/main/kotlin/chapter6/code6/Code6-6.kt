package org.example.chapter6.code6

import kotlinx.coroutines.*

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