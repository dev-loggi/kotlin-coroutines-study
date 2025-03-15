package org.example.chapter7.code13

import kotlinx.coroutines.*

@OptIn(ExperimentalStdlibApi::class)
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