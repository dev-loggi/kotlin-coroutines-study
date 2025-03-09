package org.example.chapter3.code8

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 3.5.2. Dispatchers.Default (page. 104)
 *
 * - CPU 바운드 작업을 위한 디스패처
 * - 대용량 데이터 처리, 계산 등의 CPU 사용량이 많은 작업을 위한 디스패처
 * - 싱글톤 인스턴스
 */
fun main() = runBlocking<Unit> {
    launch(Dispatchers.Default) {
        println("[${Thread.currentThread().name}] 코루틴 실행")
    }
}