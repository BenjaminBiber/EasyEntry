package com.easyentry.app.worker

import java.util.Calendar

fun computeDelayMillis(
    hourOfDay: Int,
    minuteOfHour: Int,
    dayOfWeekBitmask: Int,
    isRecurring: Boolean,
    delayMinutes: Int
): Long {
    if (!isRecurring) return delayMinutes * 60_000L

    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, minuteOfHour)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    repeat(8) {
        // Calendar.DAY_OF_WEEK: Sun=1, Mon=2...Sat=7 → convert to Mon=0…Sun=6
        val calDay = target.get(Calendar.DAY_OF_WEEK)
        val dayIdx = (calDay + 5) % 7
        if (dayOfWeekBitmask and (1 shl dayIdx) != 0 && target.timeInMillis > now.timeInMillis) {
            return@repeat
        }
        target.add(Calendar.DAY_OF_YEAR, 1)
    }

    return maxOf(target.timeInMillis - now.timeInMillis, 1_000L)
}
