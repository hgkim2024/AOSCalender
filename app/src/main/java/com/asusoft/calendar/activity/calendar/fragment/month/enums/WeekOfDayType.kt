package com.asusoft.calendar.activity.calendar.fragment.month.enums

import android.content.Context
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.objects.ThemeUtil

enum class WeekOfDayType(val value: Int) {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getShortTitle(): String {
        return when(this) {
            SUNDAY -> "일"
            MONDAY -> "월"
            TUESDAY -> "화"
            WEDNESDAY -> "수"
            THURSDAY -> "목"
            FRIDAY -> "금"
            SATURDAY -> "토"
        }
    }

    fun getTitle(): String {
        return getShortTitle() + "요일"
    }

    fun getFontColor(): Int {
        return when(this) {
            SUNDAY -> ThemeUtil.instance.holiday
            SATURDAY -> ThemeUtil.instance.saturday
            else -> ThemeUtil.instance.font
        }
    }

}