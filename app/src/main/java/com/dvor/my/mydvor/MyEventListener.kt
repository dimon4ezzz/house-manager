package com.dvor.my.mydvor

import java.util.EventListener

interface MyEventListener : EventListener {
    fun processEvent(event: MyEvent)
}