package org.example.chapter3.code1

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext

/**
 * 3.3.1. 단일 스레드 디스패처 만들기 (page. 92)
 */
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
val dispatcher: CoroutineDispatcher = newSingleThreadContext(
    name = "SingleThread",
)