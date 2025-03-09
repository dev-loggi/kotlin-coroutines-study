package org.example.chapter5.code1

import kotlinx.coroutines.*

fun main() = runBlocking<Unit> {
    val networkDeferred: Deferred<String> = async(Dispatchers.IO) {
        delay(1000L) // 네트워크 요청
        return@async "Dummy Response" // 결괏값 반환
    }
    val result = networkDeferred.await() // networkDeferred로부터 결괎값이 반환될 때까지 runBlocking 일시 중단
    println(result) // "Dummy Response" 출력
}