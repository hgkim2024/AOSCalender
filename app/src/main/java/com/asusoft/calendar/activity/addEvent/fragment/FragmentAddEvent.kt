package com.asusoft.calendar.activity.addEvent.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddEvent
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddPerson
import com.asusoft.calendar.activity.addEvent.dialog.DialogFragmentDaySelectCalendar
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.realm.copy.CopyVisitPerson
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.objects.AdUtil
import com.asusoft.calendar.util.objects.AlertUtil
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerItemClickListener
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter.Companion.CLICK_DELAY
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.complete.CompleteItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext.EditTextHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext.EditTitleItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.memo.MemoItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday.StartDayItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.visite.VisitItem
import com.asusoft.calendar.util.startOfDay
import com.asusoft.calendar.util.toStringDay
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.jakewharton.rxbinding4.view.clicks
import com.orhanobut.logger.Logger
import dev.sasikanth.colorsheet.ColorSheet
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


// TODO: - 주소 입력 추가 - 앱 출시 후 가능 할듯
// TODO: - 전화번호부 가져오는 기능 개편
class FragmentAddEvent: Fragment() {

    companion object {
        fun newInstance(
                key: Long,
                startDate: Long,
                endDate: Long
        ): FragmentAddEvent {
            val f = FragmentAddEvent()

            val args = Bundle()
            args.putLong("key", key)
            args.putLong("startDate", startDate)
            args.putLong("endDate", endDate)
            f.arguments = args
            return f
        }
    }

    lateinit var adapter: RecyclerViewAdapter
    lateinit var recyclerView: RecyclerView

    var adView: AdView? = null
    var isEdit: Boolean = false
    var key = -1L
    lateinit var startDate: Date
    lateinit var endDate: Date

    var visitList: ArrayList<CopyVisitPerson>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        key = args.getLong("key")
        startDate = Date(args.getLong("startDate", 0L))
        endDate = Date(args.getLong("endDate", 0L))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.requireContext()
        val view = inflater.inflate(R.layout.fragment_add_event, container, false)

        GlobalBus.register(this)
        var event: CopyEventDay? = null

        var title: String? = null
        var isComplete = false
        var visitCount = 0
        var memo: String? = null
        var color = ThemeUtil.instance.colorAccent

        if (key != -1L) {
            isEdit = true

            val copyItem = RealmEventDay.selectOne(key)?.getCopy(isVisitList = true)
            if (copyItem == null) {
                activity?.finish()
                return null
            }

            event = copyItem

            title = event.name
            startDate = Date(event.startTime)
            endDate = Date(event.endTime)
            isComplete = event.isComplete
            visitCount = event.visitList.size
            memo = event.memo
            color = event.color
        }


        val list = ArrayList<Any>()
        list.add(
                EditTitleItem(
                        title ?: "",
                        "제목",
                        color
                )
        )

        list.add(
                StartDayItem(
                        startDate,
                        "시작 날짜"
                )
        )

        list.add(
                StartDayItem(
                        endDate,
                        "종료 날짜"
                )
        )

        list.add(CompleteItem(isComplete))

        list.add(
                VisitItem(
                        visitCount,
                        "초대받을 사람"
                )
        )

        list.add(
                MemoItem(
                        "메모",
                        memo ?: "",
                        ""
                )
        )

        if (isEdit) {
            list.add(
                    DeleteItem(key)
            )
        }

        adapter = RecyclerViewAdapter(this, list)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                        context,
                        recyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                GlobalScope.async(Dispatchers.Main) {
                                    delay(CLICK_DELAY)
                                    when (val adapterItem = adapter.list[position]) {
                                        is StartDayItem -> {
                                            val selectDayList = ArrayList<StartDayItem>()

                                            for (item in adapter.list) {
                                                if (item is StartDayItem) {
                                                    selectDayList.add(item)
                                                }
                                            }

                                            DialogFragmentDaySelectCalendar
                                                    .newInstance(
                                                            selectDayList[0].date,
                                                            selectDayList[1].date.startOfDay,
                                                            adapterItem.title == "시작 날짜"
                                                    )
                                                    .show(requireActivity().supportFragmentManager, DialogFragmentDaySelectCalendar.toString())
                                        }

                                        is VisitItem -> {
                                            val intent = Intent(context, ActivityAddPerson::class.java)
                                            if (event != null) {
                                                intent.putExtra("key", event.key)
                                            }
                                            startActivity(intent)
                                        }
                                    }
                                }
                            }

                            override fun onItemLongClick(view: View?, position: Int) {}
                        }
                )
        )

        val cancelBtn = view.findViewById<Button>(R.id.cancel_btn)
        cancelBtn.setBackgroundColor(Color.TRANSPARENT)
        cancelBtn.setTextColor(ThemeUtil.instance.colorAccent)
        cancelBtn.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    activity?.finish()
                }

        val confirmBtn = view.findViewById<Button>(R.id.confirm_btn)
        confirmBtn.setBackgroundColor(Color.TRANSPARENT)
        confirmBtn.setTextColor(ThemeUtil.instance.colorAccent)
        confirmBtn.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    (activity as? ActivityAddEvent)?.refreshFlag = true
                    addEventRealm()
                    activity?.finish()
                }

        if (key != -1L) {
            confirmBtn.text = "수정"
        }


        // 광고 추가
        adView = AdView(context)

        adView?.adSize = AdSize.BANNER
        adView?.adUnitId = AdUtil.adMobAddEventTopBannerId

        val layout = view.findViewById<ConstraintLayout>(R.id.ad_layout)
        layout.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as ActivityAddEvent?)!!.setTitle("새로운 이벤트")
    }

    override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView?.resume()
    }

    override fun onDestroy() {
        adView?.destroy()
        GlobalBus.unregister(this)
        super.onDestroy()
    }

    private fun addEventRealm() {
        lateinit var titleItem: EditTitleItem
        lateinit var startDayItem: StartDayItem
        lateinit var endDayItem: StartDayItem
        lateinit var completeItem: CompleteItem
        lateinit var memoItem: MemoItem

        var startDayCount = 0
        for (item in adapter.list) {
            when (item) {
                is EditTitleItem -> titleItem = item

                is StartDayItem -> {
                    if (startDayCount == 0)
                        startDayItem = item
                    else if (startDayCount == 1)
                        endDayItem = item

                    startDayCount++
                }

                is CompleteItem -> completeItem = item
                is MemoItem -> memoItem = item
            }
        }

        if(isEdit) {
            val event = RealmEventDay.selectOne(key)
            event?.update(
                    titleItem.context,
                    startDayItem.date.startOfDay.time,
                    endDayItem.date.startOfDay.time,
                    completeItem.isComplete,
                    visitList,
                    memoItem.context,
                    titleItem.color
            )
        } else {
            val eventMultiDay = RealmEventDay()
            eventMultiDay.update(
                    titleItem.context,
                    startDayItem.date.startOfDay.time,
                    endDayItem.date.startOfDay.time,
                    completeItem.isComplete,
                    visitList,
                    memoItem.context,
                    titleItem.color
            )
            eventMultiDay.insert()
        }
    }

    private fun removeEvent(key: Long) {
        val event = RealmEventDay.selectOne(key)
        if (event != null) {
            event.delete()
            (activity as? ActivityAddEvent)?.refreshFlag = true
            activity?.finish()
            return
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val context = requireContext()
        val deleteHolder = event.map[DeleteHolder.toString()]
        if (deleteHolder != null) {
            val key = event.map["key"] as Long
            AlertUtil.alertOkAndCancel(
                    context,
                    "삭제하시겠습니까?",
                    getString(R.string.ok)
            ) { _, _ ->
                removeEvent(key)
            }
        }

        val dialogFragmentDaySelectCalendar = event.map[DialogFragmentDaySelectCalendar.toString()]
        if (dialogFragmentDaySelectCalendar != null) {
            val selectedStartDate = event.map["selectedStartDate"] as? Date
            val selectedEndDate = event.map["selectedEndDate"] as? Date


            val selectDayList = ArrayList<StartDayItem>()

            for (item in adapter.list) {
                if (item is StartDayItem) {
                    selectDayList.add(item)
                }
            }

            if (selectedStartDate != null) {
                selectDayList[0].date = selectedStartDate

                if (selectDayList[0].date > selectDayList[1].date
                    && selectedEndDate == null
                ) {
                    selectDayList[1].date = selectDayList[0].date
                }
//                Logger.d("selectedStartDate: ${selectedStartDate.toStringDay()}")
            }

            if (selectedEndDate != null) {
                selectDayList[1].date = selectedEndDate

                if (selectDayList[0].date > selectDayList[1].date
                    && selectedStartDate == null
                ) {
                    selectDayList[0].date = selectDayList[1].date
                }
//                Logger.d("selectedEndDate: ${selectedEndDate.toStringDay()}")
            }

            if (selectedStartDate != null
                && selectedEndDate != null
                && selectDayList[0].date > selectDayList[1].date
            ) {
                val date = selectDayList[0].date
                selectDayList[0].date = selectDayList[1].date
                selectDayList[1].date = date
            }

            adapter.notifyDataSetChanged()
        }

        val activityAddPerson = event.map[ActivityAddPerson.toString()]
        if (activityAddPerson != null) {
            val list = event.map["list"] as ArrayList<CopyVisitPerson>
            visitList = list

            for (idx in adapter.list.indices) {
                val item = adapter.list[idx]
                if (item is VisitItem) {
                    item.count = list.size
                    adapter.notifyItemChanged(idx)
                    break
                }
            }

        }

        val editTextHolder = event.map[EditTextHolder.toString()]
        if (editTextHolder != null) {
            val colors = resources.getIntArray(R.array.colors)
            val colorSheet = ColorSheet()

            val tv = colorSheet.view?.findViewById<TextView>(R.id.sheetTitle)
            tv?.text = "선택"

            // TODO: - 타이틀 수정 하는 방법 알아보기 - 없음 - 나중에 커스텀으로 만들 것
            
            colorSheet
                    .cornerRadius(8)
                    .colorPicker(
                            colors = colors,
                            noColorOption = false,
                            selectedColor = ColorSheet.NO_COLOR,
                            listener = { color ->
                                for (idx in adapter.list.indices) {
                                    val item = adapter.list[idx]
                                    if (item is EditTitleItem) {
                                        item.color = color
                                        adapter.notifyItemChanged(idx)
                                        break
                                    }
                                }
                            })
                    .show(requireActivity().supportFragmentManager)
        }
    }

}