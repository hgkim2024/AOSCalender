package com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddPerson
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.ExtendedEditText
import com.asusoft.calendar.util.objects.CalendarUtil
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.HashMap
import java.util.concurrent.TimeUnit

class EditTextHolder(
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        if (adapter.list[position] is EditTitleItem) {
            val item = adapter.list[position] as EditTitleItem

            val tvEdit = view.findViewById<ExtendedEditText>(R.id.tv_edit)
            tvEdit.hint = item.hint
            tvEdit.clearTextChangedListeners()
            tvEdit.setTextColor(ThemeUtil.instance.font)

            val colorLayout = view.findViewById<ConstraintLayout>(R.id.color_layout)

            colorLayout.clicks()
                    .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val event = HashMapEvent(HashMap())
                        event.map[EditTextHolder.toString()] = EditTextHolder.toString()
                        GlobalBus.post(event)
                    }

            if (item.color == 0) {
                CalendarUtil.setCornerRadiusDrawable(colorLayout, ThemeUtil.instance.colorAccent, 200.0F)
            } else {
                CalendarUtil.setCornerRadiusDrawable(colorLayout, item.color, 200.0F)
            }

            tvEdit.setText(item.context)
            tvEdit.addTextChangedListener(item.textWatcher)
        }
    }

    companion object {
        override fun toString(): String {
            return "EditTextHolder"
        }
    }
}