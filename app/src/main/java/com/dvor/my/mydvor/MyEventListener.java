package com.dvor.my.mydvor;

import java.util.EventListener;

public interface MyEventListener extends EventListener {
    void processEvent(MyEvent event);
}