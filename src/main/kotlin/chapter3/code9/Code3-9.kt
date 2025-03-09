package org.example.chapter3.code9

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 3.5.3. limitedParallelism 사용해 Dispatchers.Default 스레드 사용 제한하기 (page. 105)
 *
 * - 무겁고 오래 걸리는 연산을 수행하는 코루틴을 Dispatchers.Default의 모든 스레드가 사용될 수 있다.
 * - 이런 경우, Dispatchers.Default의 스레드 사용을 제한할 수 있는 방법이 있다.
 * - Dispatchers.Default.limitedParallelism 함수를 사용하면 Dispatchers.Default의 스레드 사용을 제한할 수 있다.
 */
fun main() = runBlocking<Unit> {
    launch(Dispatchers.Default.limitedParallelism(2)) {
        repeat(10) {
            launch {
                println("[${Thread.currentThread().name}] 코루틴 실행")
                //delay(10)
            }
        }
    }
}