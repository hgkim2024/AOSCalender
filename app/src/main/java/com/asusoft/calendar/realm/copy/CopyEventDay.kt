package com.asusoft.calendar.realm.copy

import com.asusoft.calendar.realm.RealmEventDay

class CopyEventDay(
        val key: Long,
        var name: String,
        var startTime: Long,
        var endTime: Long,
        var isComplete: Boolean = false,
        var visitList: ArrayList<CopyVisitPerson>,
        var memo: String,
        var color: Int,
        var order: Double
) {

    fun updateIsCompete(isComplete: Boolean) {
        this.isComplete = isComplete
        val item = RealmEventDay.select(key)
        item?.update(
                name,
                startTime,
                endTime,
                isComplete
        )
    }

    fun updateName(name: String) {
        this.name = name
        val item = RealmEventDay.select(key)
        item?.update(
                name,
                startTime,
                endTime,
                isComplete
        )
    }

    fun updateOrder(order: Double) {
        this.order = order
        val item = RealmEventDay.select(key)
        item?.updateOrder(order)
    }

    fun delete() {
        val item = RealmEventDay.select(key)
        item?.delete()
    }
}