package com.asusoft.calendar.fragment.month

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.*
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.ActivityAddEvent
import com.asusoft.calendar.activity.ActivityStart
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class FragmentMonthViewPager: Fragment() {

    private lateinit var adapter: AdapterMonthCalendar
    private lateinit var viewPager: ViewPager2
    private lateinit var todayLayout: TextView

    private val pageCount = 1

    private var selectedDate = Date().getToday()
    private var curPosition = 0
    private var isScroll = false
    private var isMovePage = false

    companion object {
        fun newInstance(): FragmentMonthViewPager {
            return FragmentMonthViewPager()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalBus.getBus().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        GlobalBus.getBus().unregister(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = this.context!!
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)

        adapter = AdapterMonthCalendar(activity!!)
        viewPager = view.findViewById(R.id.month_calendar)

        val floatingBtn = view.findViewById<FloatingActionButton>(R.id.btn_float)
        floatingBtn.setOnClickListener {
            showAddEventActivity()
        }

        val weekHeader = view.findViewById<ConstraintLayout>(R.id.week_header)
        weekHeader.addView(MonthCalendarUIUtil.getWeekHeader(context))

        todayLayout = view.findViewById<TextView>(R.id.tv_today)
        todayLayout.background.alpha = 200
        todayLayout.visibility = View.INVISIBLE

        todayLayout.setOnClickListener {
            isScroll = false
            movePage(Date().getToday())
            todayLayout.visibility = View.INVISIBLE
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewPager.adapter = adapter
        viewPager.setCurrentItem(AdapterMonthCalendar.START_POSITION, false)
        curPosition = AdapterMonthCalendar.START_POSITION
        viewPager.offscreenPageLimit = pageCount

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (!isMovePage) {
                    curPosition = position
                    isScroll = true
                    viewPager.isUserInputEnabled = false
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
//                Log.d("Asu", "position: ${position}, positionOffset: ${positionOffset}, positionOffsetPixels: ${positionOffsetPixels}")

                if (isScroll
                    && positionOffsetPixels == 0
                ) {
                    val diffMonth = viewPager.currentItem - AdapterMonthCalendar.START_POSITION
                    Log.d("Asu", "diffMonth: $diffMonth")
                    loadPage()

                    isScroll = false
                    viewPager.isUserInputEnabled = true
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                when (state) {
                    SCROLL_STATE_DRAGGING -> {
                        if (adapter.initFlag) {
                            adapter.initFlag = false
                        }
                    }

                    SCROLL_STATE_IDLE -> {
                        if (isMovePage) {
                            isMovePage = false
                        }


                        val curPageTime = adapter.getItemId(curPosition)

                        Log.d("Asu", "SCROLL_STATE_IDLE date: ${Date(curPageTime).toStringDay()}")
                        val today = Date().getToday().startOfMonth.time
                        if (today != curPageTime) {
                            if (today < curPageTime) {
                                todayLayout.text = "<  오늘"
                            } else {
                                todayLayout.text = "오늘  >"
                            }
                            todayLayout.visibility = View.VISIBLE
                        } else {
                            todayLayout.visibility = View.INVISIBLE
                        }
                    }

                    SCROLL_STATE_SETTLING -> {}
                }

            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val fragmentMonthPage = event.map.getOrDefault(FragmentMonthPage.toString(), null)
        if (fragmentMonthPage != null) {
            selectedDate = event.map["date"] as Date
            Log.d("Asu", "selected day date: ${selectedDate.toStringDay()}")

            val addFlag = event.map["add"]
            if (addFlag != null) {
                showAddEventActivity()
            }
        }

        val activityStart = event.map.getOrDefault(ActivityStart.toStringActivity(), null)
        if (activityStart != null) {
            val date = event.map["date"] as Date
            movePage(date)
        }
    }

    private fun loadPage() {
        val event = HashMapEvent(HashMap())
        event.map[FragmentMonthViewPager.toString()] = FragmentMonthViewPager.toString()
        GlobalBus.getBus().post(event)
    }

    private fun movePage(date: Date) {
        adapter.initFlag = true
        isMovePage = true

        val curPageDate = Date(adapter.getItemId(curPosition))

        val diffYear = date.calendarYear - curPageDate.calendarYear
        val diffMonth = date.calendarMonth - curPageDate.calendarMonth
        val diff = diffYear * 12 + diffMonth

        viewPager.setCurrentItem(viewPager.currentItem + diff, true)
    }

    private fun showAddEventActivity() {
        val intent = Intent(context, ActivityAddEvent::class.java)
        intent.putExtra("startDate", selectedDate)
        intent.putExtra("endDate", selectedDate)
        startActivity(intent)
    }
}