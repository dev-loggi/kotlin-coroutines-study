package org.example.chapter6.code7

import kotlinx.coroutines.*

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