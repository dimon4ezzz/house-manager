package com.dvor.my.mydvor

import java.util.*

interface MyEventListener : EventListener {
    fun processEvent(event: MyEvent)
}