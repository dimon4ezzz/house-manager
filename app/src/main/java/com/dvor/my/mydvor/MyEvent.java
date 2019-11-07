package com.dvor.my.mydvor;

import java.util.EventObject;

public class MyEvent extends EventObject {

    private Type type = Type.UpdateAddressID;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public MyEvent(Object source, Type type) {
        super(source);
        this.type = type;
    }
}