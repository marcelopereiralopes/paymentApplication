package com.example.paymentapplication.infrastructure

import java.util.HashMap

object AppStore {

    private val session = HashMap<Any, Any>()

    operator fun get(id: Any): Any? {
        return session[id]
    }

    operator fun set(id: Any, value: Any) {
        session[id] = value
    }

    fun remove(id: Any) {
        session.remove(id)
    }

    fun recycle() {
        session.clear()
    }
}