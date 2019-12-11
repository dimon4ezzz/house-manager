package com.dvor.my.mydvor

import java.util.*

class MyEvent(source: Any, type: Type) : EventObject(source) {

    var type = Type.UpdateAddressID

    init {
        this.type = type
    }
}