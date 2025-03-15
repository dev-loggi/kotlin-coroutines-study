package org.example.chapter7.code9

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.example.util.ElapsedTimer
import org.example.util.printStates

fun main() = runBlocking<Unit> {
    val timer = ElapsedTimer().start()
    val parentJob = launch { // 부모 코루틴 실행
        launch { // 자식 코루틴 실행
            delay(1000L)
            println("${timer.elapsedTime()} 자식 코루틴 실행 완료")
        }
        println("${timer.elapsedTime()} 부모 코루틴이 실행하는 마지막 코드")
    }
    parentJob.invokeOnCompletion { // 부모 코루틴이 종료될 시 호출되는 콜백 등록
        println("${timer.elapsedTime()} 부모 코루틴 실행 완료")
    }
    delay(500L)
    parentJob.printStates()
}