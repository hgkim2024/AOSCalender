package com.asusoft.calendar.util.recyclerview.holder.calendar.selectday

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.activity.addEvent.dialog.DialogFragmentDaySelectCalendar
import com.asusoft.calendar.activity.calendar.fragment.month.enums.WeekOfDayType
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.objects.CalendarUtil.getEventOrderList
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil.WEEK
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.HashMap
import java.util.concurrent.TimeUnit

class SelectDayHolder(
        private val typeObject: Any,
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as? SelectDayItem ?: return

//        Logger.d("SelectDayHolder date: ${item.date.toStringMonth()}")

        val rootLayout = view.findViewById<ConstraintLayout>(R.id.root_layout)
        rootLayout.setBackgroundColor(ThemeUtil.instance.background)

        val header = view.findViewById<TextView>(R.id.tv_header)
        header.text = item.date.toStringMonth()
        header.setTextColor(ThemeUtil.instance.lightFont)

        val separator = view.findViewById<View>(R.id.separator)
        separator.setBackgroundColor(ThemeUtil.instance.separator)

        val monthDate = item.date.startOfMonth
        val startDate = monthDate.startOfWeek
        val row = MonthCalendarUiUtil.getMonthRow(monthDate)
        val dayViewList = ArrayList<TextView>()

        val bodyLayout = view.findViewById<LinearLayout>(R.id.body_layout)
        if (bodyLayout.childCount == 0) {
            val monthItem = MonthCalendarUiUtil.getMonthUI(context, item.date.startOfMonth, true)
            bodyLayout.addView(monthItem.monthView)
        } else {
            if (row == 6) {
                val lastDayCount = row * WEEK
                val dayView = bodyLayout.findViewWithTag<TextView?>(lastDayCount)
                if (dayView == null) {
                    bodyLayout.removeAllViews()
                    val monthItem = MonthCalendarUiUtil.getMonthUI(context, item.date.startOfMonth, true)
                    bodyLayout.addView(monthItem.monthView)
                }
            } else if (row == 5) {
                val lastDayCount = row * WEEK
                val dayView = bodyLayout.findViewWithTag<TextView?>(lastDayCount)
                if (dayView != null) {
                    bodyLayout.removeAllViews()
                    val monthItem = MonthCalendarUiUtil.getMonthUI(context, item.date.startOfMonth, true)
                    bodyLayout.addView(monthItem.monthView)
                }
            }
        }

        for (idx in 0 until row) {
            val weekDate = startDate.getNextDay(idx * WEEK)
            val orderMap = getEventOrderList(weekDate)
            val holidayMap = orderMap.filter { it.key <= 1231 }

            for (index in 0 until WEEK) {
                val i = (idx * WEEK) + index
                val date = startDate.getNextDay(i)
                val dayView = bodyLayout.findViewWithTag<TextView?>(i) ?: return
                dayViewList.add(dayView)

                dayView.text = date.calendarDay.toString()
                dayView.setTextColor(WeekOfDayType.fromInt(date.weekOfDay).getFontColor())
                dayView.setBackgroundColor(ThemeUtil.instance.background)

                if (date.calendarMonth == monthDate.calendarMonth) {
                    dayView.alpha = 1.0F
                } else {
                    dayView.alpha = MonthCalendarUiUtil.ALPHA
                }

                dayView.clicks()
                    .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (dayView.alpha != MonthCalendarUiUtil.ALPHA) {
                            val event = HashMapEvent(HashMap())
                            event.map[SelectDayHolder.toString()] = SelectDayHolder.toString()
                            event.map["date"] = date
                            GlobalBus.post(event)
                        }
                    }

                if (holidayMap.isNotEmpty()) {
                    val dateString = String.format("%02d", date.calendarMonth) + String.format("%02d", date.calendarDay)
                    val key = dateString.toLong()
                    if (holidayMap[key] != null) {
                        dayView.setTextColor(ThemeUtil.instance.holiday)
                    }
                }
            }
        }


        val fragment = typeObject as DialogFragmentDaySelectCalendar
        MonthCalendarUiUtil.setSelectedDay(
                fragment.selectedStartDate,
                fragment.selectedEndDate,
                monthDate,
                dayViewList
        )

    }

    companion object {
        override fun toString(): String {
            return "SelectDayHolder"
        }
    }

}