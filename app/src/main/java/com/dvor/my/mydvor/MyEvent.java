package com.dvor.my.mydvor;

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

public class MyEvent extends EventObject {

    enum Type {
        UpdateAddressID, UpdateRetailers, UpdateRetailersStreet, UpdateOrganizationId, UpdateNews, UpdateNotifications, UpdateUI, UpdateUI_2
    }

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