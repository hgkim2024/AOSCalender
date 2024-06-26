package com.asusoft.calendar.activity.addEvent.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.realm.copy.CopyVisitPerson
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.removeActionBarShadow
import com.asusoft.calendar.util.extension.setOrientation
import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.helper.ItemTouchHelperCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.HashMap
import java.util.concurrent.TimeUnit

class ActivityAddPerson : AppCompatActivity() {

    companion object {
        var REQUEST_CONTACTS = 0

        override fun toString(): String {
            return "ActivityAddPerson"
        }
    }

    lateinit var adapter: RecyclerViewAdapter
    lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOrientation()
        setContentView(R.layout.activity_add_persion)

        val key = intent.getLongExtra("key", -1L)
        val visitList =
                if (key != -1L) {
                    val item = RealmEventDay.selectOne(key)

                    if (item == null) {
                        finish()
                        return
                    }

                    item.getCopyVisitList()
                } else {
                    ArrayList<CopyVisitPerson>()
                }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setBackgroundColor(ThemeUtil.instance.background)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        removeActionBarShadow()

        val floatingButton = findViewById<FloatingActionButton>(R.id.floating_button)
        floatingButton.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val intent = Intent(Intent.ACTION_PICK)
                intent.data = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                startActivityForResult(intent,
                        REQUEST_CONTACTS
                )
            }

        adapter = RecyclerViewAdapter(this, visitList as ArrayList<Any>)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(baseContext)

        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(recyclerView)

        val title = findViewById<TextView>(R.id.action_bar_title)
        title.textSize = PreferenceManager.getFloat(PreferenceKey.CALENDAR_HEADER_FONT_SIZE, PreferenceKey.CALENDAR_HEADER_DEFAULT_FONT_SIZE)
        title.setTextColor(ThemeUtil.instance.font)
        tvEmpty = findViewById<TextView>(R.id.tv_empty)
        isEmpty()

    }

    override fun onResume() {
        super.onResume()
        setOrientation()
    }

    private fun isEmpty() {
        tvEmpty.setTextColor(ThemeUtil.instance.lightFont)
        if (adapter.list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "우측 하단 버튼 클릭으로 추가할 수 있습니다."
        } else {
            tvEmpty.visibility = View.GONE
        }
    }


    override fun finish() {
        postEventBus()
        super.finish()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {

        when(menuItem.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onBackPressed() {
        postEventBus()
        super.onBackPressed()
    }

    private fun postEventBus() {
        val event = HashMapEvent(HashMap())
        event.map[ActivityAddPerson.toString()] = ActivityAddPerson.toString()
        event.map["list"] = adapter.list
        GlobalBus.post(event)
    }

    @SuppressLint("ShowToast")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CONTACTS) {
            val cursor = contentResolver.query(
                    data!!.data!!,
                    arrayOf(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.Contacts.PHOTO_ID,
                            ContactsContract.Contacts._ID
                    ),
                    null,
                    null,
                    null
            )

            cursor!!.moveToFirst()

            val receiveName = cursor.getString(0)
            val receivePhone = cursor.getString(1)
            cursor.close()

            val item = CopyVisitPerson(receiveName, receivePhone)
            adapter.list.add(item)
            adapter.notifyDataSetChanged()
            isEmpty()
        }

    }
}