package org.example.chapter3.code7

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 3.5.1. Dispatchers.IO (page. 103)
 *
 * - 네트워크 통신, DB 액세스 등의 I/O 작업을 위한 디스패처
 * - 코루틴 라이브러리 1.7.2 버전 기준으로 최대 사용 스레드 수는 JVM에서 사용이 가능한 프로세서의 수와 64 중 큰 값으로 설정
 * - Dispatchers.IO는 싱글톤 인스턴스
 * - 스레드의 이름은 DefaultDispatcher-worker-n 형식 (코루틴 라이브러리에서 제공하는 공유 스레드풀에서 생성된 스레드)
 */
fun main() = runBlocking<Unit> {
    launch(Dispatchers.IO) {
        println("[${Thread.currentThread().name}] 코루틴 실행")
    }
}