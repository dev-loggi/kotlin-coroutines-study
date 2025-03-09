package org.example.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

internal fun CoroutineScope.printThreadName(message: String = "") {
    println("[${Thread.currentThread().name}]" + if (message.isNotEmpty()) " $message" else "")
}

internal fun Job.printStates() {
    println("""
        isActive    >> $isActive
        isCompleted >> $isCompleted
        isCancelled >> $isCancelled
    """.trimIndent())
}