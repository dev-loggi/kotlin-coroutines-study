package org.example.chapter7.code7

import kotlinx.coroutines.*

fun main() = runBlocking<Unit> {
    val parentJob = launch(Dispatchers.IO) { // 부모 코루틴 생성
        val dbResultsDeferred: List<Deferred<String>> = listOf("db1", "db2", "db3").map {
            async { // 자식 코루틴 생성
                delay(1000L)
                println("${it}으로부터 데이터를 가져오는데 성공했습니다")
                return@async "[${it}]data"
            }
        }
        val dbResults: List<String> = dbResultsDeferred.awaitAll() // 모든 코루틴이 완료될 때까지 대기

        println(dbResults)
    }
    parentJob.cancel() // 부모 코루틴 취소
}