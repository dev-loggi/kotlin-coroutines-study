package org.example.chapter3.code2

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newFixedThreadPoolContext

/**
 * 3.3.2. 멀티 스레드 디스패처 만들기 (page. 93)
 */
@OptIn(DelicateCoroutinesApi::class)
val multiThreadDispatcher: CoroutineDispatcher = newFixedThreadPoolContext(
    nThreads = 2,
    name = "MultiThread",
)