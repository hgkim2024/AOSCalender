package com.asusoft.calendar.activity.addEvent.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.objects.CalculatorUtil
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil.SELECT_DAY_HEIGHT
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil.WEEK
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.calendar.selectday.SelectDayHolder
import com.asusoft.calendar.util.recyclerview.holder.calendar.selectday.SelectDayItem
import com.asusoft.calendar.util.recyclerview.helper.StartSnapHelper
import com.jakewharton.rxbinding4.view.clicks
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class DialogFragmentDaySelectCalendar: DialogFragment() {

    companion object {
        const val DEFAULT_MONTH_COUNT = 20

        fun newInstance(
                selectedStartDate: Date? = null,
                selectedEndDate: Date? = null,
                isStart: Boolean = true
        ): DialogFragmentDaySelectCalendar {
            val f = DialogFragmentDaySelectCalendar()

            val args = Bundle()
            if (selectedStartDate != null) {
                args.putLong("selectedStartDate", selectedStartDate.time)
            }

            if (selectedEndDate != null) {
                args.putLong("selectedEndDate", selectedEndDate.time)
            }

            args.putBoolean("isStart", isStart)

            f.arguments = args
            return f
        }
    }

    private lateinit var adapter: RecyclerViewAdapter

    var selectedStartDate: Date? = null
    var selectedEndDate: Date? = null
    private var selectedIsStart: Boolean = true
    private var isStart: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        val selectedStartTime = args.getLong("selectedStartDate") as Long
        if (selectedStartTime != 0L) {
            selectedStartDate = Date(selectedStartTime)
        }

        val selectedEndTime = args.getLong("selectedEndDate") as Long
        if (selectedEndTime != 0L) {
            selectedEndDate = Date(selectedEndTime)
        }

        isStart = args.getBoolean("isStart")
    }

    override fun onStart() {
        super.onStart()

        GlobalBus.register(this)
    }

    override fun onStop() {
        super.onStop()

        GlobalBus.unregister(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()

        val view = inflater.inflate(R.layout.dialog_select_day, container, false)

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        val rootLayout = view.findViewById<ConstraintLayout>(R.id.root_layout)

        rootLayout.apply {
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            clipToOutline= true
        }

        val backgroundLayout = view.findViewById<ConstraintLayout>(R.id.background_layout)
        backgroundLayout.setBackgroundColor(ThemeUtil.instance.background)

        val headerLayout = view.findViewById<ConstraintLayout>(R.id.header_layout)
        headerLayout.setBackgroundColor(ThemeUtil.instance.colorAccent)

        val tvHeader = view.findViewById<TextView>(R.id.tv_header)
        tvHeader.setTextColor(ThemeUtil.instance.invertFont)

        val weekHeader = view.findViewById<ConstraintLayout>(R.id.week_header)
        weekHeader.addView(MonthCalendarUiUtil.getWeekHeader(context, true))

        val today =
                if (selectedStartDate != null) {
                    selectedStartDate!!.startOfMonth
                } else {
                    Date().getToday().startOfMonth
                }

        val list = ArrayList<SelectDayItem>()

        val weight = DEFAULT_MONTH_COUNT / 2
        for (index in 0 until DEFAULT_MONTH_COUNT) {
            val item = SelectDayItem(today.getNextMonth(index - weight))
            list.add(item)
        }

        adapter = RecyclerViewAdapter(this, list as ArrayList<Any>)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val snapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.scrollToPosition(weight)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = (recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstCompletelyVisibleItemPosition()

//                Logger.d("onScrolled position: $position")

                if (-1 < position && position < 2) {
                    val list = getList(adapter.list.first() as SelectDayItem, true)
                    for (item in list) {
                        adapter.list.add(0, item)
                    }
                    adapter.notifyItemRangeInserted(0, list.size - 1)
                    recyclerView.scrollToPosition(list.size)

                } else if (position >= adapter.list.size - 2) {
                    val list = getList(adapter.list.last() as SelectDayItem, false)
                    adapter.list.addAll(list)
                    adapter.notifyDataSetChanged()
                }
            }
        })

        val confirmBtn = view.findViewById<TextView>(R.id.confirm_button)

        confirmBtn.setBackgroundColor(ThemeUtil.instance.colorAccent)
        confirmBtn.setTextColor(ThemeUtil.instance.invertFont)

        confirmBtn.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val event = HashMapEvent(HashMap())
                event.map[DialogFragmentDaySelectCalendar.toString()] = DialogFragmentDaySelectCalendar.toString()

                if (isStart) {
                    if (selectedStartDate != null) {
                        event.map["selectedStartDate"] = selectedStartDate!!
                    }

                    if (selectedEndDate != null) {
                        event.map["selectedEndDate"] = selectedEndDate!!
                    }
                } else {
                    if (selectedStartDate != null) {
                        event.map["selectedEndDate"] = selectedStartDate!!
                    }

                    if (selectedEndDate != null) {
                        event.map["selectedStartDate"] = selectedEndDate!!
                    }
                }

                GlobalBus.post(event)
                dismiss()
            }

        val cancelBtn = view.findViewById<TextView>(R.id.cancel_button)

        cancelBtn.setBackgroundColor(ThemeUtil.instance.background)
        cancelBtn.setTextColor(ThemeUtil.instance.colorAccent)

        cancelBtn.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                dismiss()
            }

        val topSeparator = view.findViewById<View>(R.id.top_separator)
        topSeparator.setBackgroundColor(ThemeUtil.instance.colorAccent)

        return view
    }

    fun getList(dayItem: SelectDayItem, isUp: Boolean): ArrayList<SelectDayItem> {
        val list = ArrayList<SelectDayItem>()

        for (index in 0 until DEFAULT_MONTH_COUNT) {
            val weight = if (isUp) -(index + 1) else index + 1
            val item = SelectDayItem(dayItem.date.getNextMonth(weight))
            list.add(item)
        }

        return list
    }

    override fun onResume() {
        super.onResume()

        val size = CalendarApplication.getSize(requireActivity())
        val params: WindowManager.LayoutParams = dialog?.window?.attributes ?: return

        val maxWidth = CalculatorUtil.dpToPx((SELECT_DAY_HEIGHT * WEEK) + 4.0F + 16.0F)
        params.width = (size.width * 0.9).toInt()
        if (params.width > maxWidth) {
            params.width = maxWidth
        }

        val maxHeight = CalculatorUtil.dpToPx(500.0F)
        params.height = (size.height * 0.9).toInt()
        if (params.height > maxHeight) {
            params.height = maxHeight
        }

        dialog?.window?.attributes = params
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val selectDayHolder = event.map[SelectDayHolder.toString()]
        if (selectDayHolder != null) {
            val date = event.map["date"] as Date

//            Logger.d("selectDayHolder received date: ${date.toStringDay()}, isStart: ${isStart}")

            if (selectedIsStart) {
                selectedStartDate = null
                selectedEndDate = null

                selectedStartDate = date
                selectedIsStart = false
            } else {
                if (date == selectedStartDate) return
                selectedEndDate = date

                selectedIsStart = true
            }

            Logger.d("start date: ${selectedStartDate?.toStringDay()}")
            Logger.d("end date: ${selectedEndDate?.toStringDay()}")
            adapter.notifyDataSetChanged()
        }
    }
}