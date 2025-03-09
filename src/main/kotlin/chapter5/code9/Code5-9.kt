package org.example.chapter5.code9

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() = runBlocking<Unit> {
    println("[${Thread.currentThread().name}] runBlocking 실행")

    withContext(Dispatchers.IO) {
        println("[${Thread.currentThread().name}] withContext 실행")
    }
}