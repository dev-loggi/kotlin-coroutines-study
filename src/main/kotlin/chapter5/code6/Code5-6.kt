package org.example.chapter5.code6

import kotlinx.coroutines.*
import org.example.util.ElapsedTimer
import org.example.util.printThreadName

fun main() = runBlocking<Unit> {
    val timer = ElapsedTimer().apply { start() }

    val participantDeferred1: Deferred<List<String>> = async(Dispatchers.IO) {
        delay(1000L)
        return@async listOf("James", "Jason")
    }
    val participantDeferred2: Deferred<List<String>> = async(Dispatchers.IO) {
        delay(1000L)
        return@async listOf("Jenny")
    }
    val results: List<List<String>> = listOf(participantDeferred1, participantDeferred2)
        .awaitAll() // 모든 요청이 끝날 때까지 대기

    println("${timer.elapsedTimeToString()} 참여자 목록: ${results.flatten()}")
}