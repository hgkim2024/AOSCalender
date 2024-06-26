package com.asusoft.calendar.realm

import android.graphics.Color
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.copy.CopyTheme
import com.orhanobut.logger.Logger
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmTheme: RealmObject()  {

    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var name: String = "기본 테마"
    var order: Long = System.currentTimeMillis()

    var colorAccent: Int = Color.parseColor("#4179F0")

    var holiday: Int = Color.parseColor("#dc143c")
    var saturday: Int = Color.parseColor("#4179F0")

    var background: Int = Color.parseColor("#FAFAFA")
    var separator: Int = Color.parseColor("#E0E0E0")

    var font: Int = Color.parseColor("#424242")
    var lightFont: Int = Color.parseColor("#BDBDBD")
    var invertFont: Int = Color.parseColor("#FAFAFA")

    var today: Int = Color.parseColor("#00b07b")
    var eventFontColor = Color.parseColor("#FAFAFA")

    companion object {
        fun selectOne(key: Long = 0): RealmTheme? {
            Logger.d("RealmTheme selectOne")
            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val item =  if (key == 0L) {
                realm.where(RealmTheme::class.java)
                    .sort("key")
                    .findFirst()

            } else {
                realm.where(RealmTheme::class.java)
                    .equalTo("key", key)
                    .findFirst()
            }

            realm.commitTransaction()
            return item
        }

        fun selectAllCopy(): List<CopyTheme> {
            val copyItems = ArrayList<CopyTheme>()

            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val items = realm.where(RealmTheme::class.java)
                .sort("order")
                .findAll()

            realm.commitTransaction()

            for (item in items) {
                copyItems.add(item.getCopy())
            }

            return copyItems
        }
    }

    fun getCopy(): CopyTheme {
        return CopyTheme(
            key,
            name,
            order,
            colorAccent,
            holiday,
            saturday,
            background,
            separator,
            font,
            lightFont,
            invertFont,
            today,
            eventFontColor
        )
    }

    fun updateOrder(order: Long) {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())

        realm.beginTransaction()
        this.order = order
        realm.commitTransaction()
        realm.refresh()
    }

    fun insert() {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
        realm.beginTransaction()
        realm.insertOrUpdate(this)
        realm.commitTransaction()
        realm.refresh()
    }

    fun delete() {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
        realm.beginTransaction()
        this.deleteFromRealm()
        realm.commitTransaction()
        realm.refresh()
    }
}