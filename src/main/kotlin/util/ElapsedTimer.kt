package org.example.util

internal class ElapsedTimer {

    private var startTime: Long = 0L
    private var endTime: Long = 0L

    fun start() = apply {
        startTime = System.currentTimeMillis()
    }

    fun elapsedTime(): String {
        return "[지난 시간: ${System.currentTimeMillis() - startTime}ms]"
    }
}