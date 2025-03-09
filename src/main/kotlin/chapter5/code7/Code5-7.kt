package org.example.chapter5.code7

import kotlinx.coroutines.*

fun main() = runBlocking<Unit> {

    // async-await 사용
    val deferred: Deferred<String> = async(Dispatchers.IO) {
        delay(1000L)
        return@async "Dummy Response"
    }
    val result1 = deferred.await()
    println(result1)

    // withContext 사용
    val result2: String = withContext(Dispatchers.IO) {
        delay(1000L)
        return@withContext "Dummy Response"
    }
    println(result2)
}