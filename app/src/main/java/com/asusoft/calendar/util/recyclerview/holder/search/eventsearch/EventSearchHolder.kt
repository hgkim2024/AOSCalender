package com.asusoft.calendar.util.recyclerview.holder.search.eventsearch

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.toStringDay
import java.util.*

class EventSearchHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as? CopyEventDay ?: return

        val edge = view.findViewById<View>(R.id.edge)
        if (item.isComplete) {
            edge.alpha = MonthCalendarUiUtil.ALPHA
        } else {
            edge.alpha = 1.0F
        }

        val title = view.findViewById<TextView>(R.id.title)
        title.text = item.name
        title.setTextColor(ThemeUtil.instance.font)

        val tvTime = view.findViewById<TextView>(R.id.tv_time)
        tvTime.setTextColor(ThemeUtil.instance.font)
        if (item.startTime == item.endTime) {
            tvTime.text = Date(item.startTime).toStringDay()
        } else {
            tvTime.text = "${Date(item.startTime).toStringDay()} ~ ${Date(item.endTime).toStringDay()}"
        }

        val visitLayout = view.findViewById<ConstraintLayout>(R.id.visit_layout)
        if (item.visitList.isEmpty()) {
            visitLayout.visibility = View.GONE
        } else {
            visitLayout.visibility = View.VISIBLE
            val tvVisit = view.findViewById<TextView>(R.id.tv_visit)
            val tvSubVisit = view.findViewById<TextView>(R.id.tv_sub_visit)
            val ivPerson = view.findViewById<ImageView>(R.id.iv_person)

            tvVisit.setTextColor(ThemeUtil.instance.font)
            tvSubVisit.setTextColor(ThemeUtil.instance.font)
            ivPerson.colorFilter = PorterDuffColorFilter(ThemeUtil.instance.font, PorterDuff.Mode.SRC_IN)

            tvVisit.text = "초대받을 사람"
            tvSubVisit.text = item.visitList.size.toString()
            ivPerson.visibility = View.VISIBLE
        }

        val tvMemo = view.findViewById<TextView>(R.id.tv_memo)
        tvMemo.setTextColor(ThemeUtil.instance.font)
        if (item.memo == "") {
            tvMemo.visibility = View.GONE
        } else {
            tvMemo.visibility = View.VISIBLE
            tvMemo.text = item.memo
        }
    }

}