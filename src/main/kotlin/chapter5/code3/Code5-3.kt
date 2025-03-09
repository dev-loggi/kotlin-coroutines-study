package org.example.chapter5.code3

import kotlinx.coroutines.*
import org.example.util.ElapsedTimer

fun main() = runBlocking<Unit> {
    val timer = ElapsedTimer().apply { start() } // 1. 시간 측정 시작

    val participantDeferred1: Deferred<List<String>> = async(Dispatchers.IO) {
        // 2. 플랫폼1에서 등록한 관람객 목록을 가져오는 코루틴
        delay(1000L)
        return@async listOf("James", "Jason")
    }
    val participant1 = participantDeferred1.await() // 3. 결과가 수신될 떄까지 대기

    val participantDeferred2: Deferred<List<String>> = async(Dispatchers.IO) {
        // 4. 플랫폼2에서 등록한 관람객 목록을 가져오는 코루틴
        delay(1000L)
        return@async listOf("Jenny")
    }
    val participant2 = participantDeferred2.await() // 5. 결과가 수신될 떄까지 대기

    println("${timer.elapsedTimeToString()} 참여자 목록: ${participant1 + participant2}") // 6. 지난 시간 표시 및 참여자 목록을 병합하여 출력
}