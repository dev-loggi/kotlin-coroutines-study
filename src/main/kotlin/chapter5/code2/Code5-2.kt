package org.example.chapter5.code2

import kotlinx.coroutines.*
import org.example.util.printStates

fun main() = runBlocking<Unit> {
    val networkDeferred: Deferred<String> = async(Dispatchers.IO) {
        delay(1000L) // 네트워크 요청
        return@async "Dummy Response" // 결괏값 반환
    }
    networkDeferred.join()
    networkDeferred.printStates()
    println(networkDeferred)
}