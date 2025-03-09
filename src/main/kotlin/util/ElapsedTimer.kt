package org.example.util

internal class ElapsedTimer {

    private var startTime: Long = 0L
    private var endTime: Long = 0L

    fun start() {
        startTime = System.currentTimeMillis()
    }

    fun elapsedTime(): Long {
        return System.currentTimeMillis() - startTime
    }

    fun elapsedTimeToString(): String {
        return "[지난 시간: ${elapsedTime()}ms]"
    }
}