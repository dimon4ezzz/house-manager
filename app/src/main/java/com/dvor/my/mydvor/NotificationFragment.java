package com.dvor.my.mydvor;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


import java.util.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private static List<MyEventListener> eventListeners;

    public void addEventListener(MyEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void notifyEventListeners(MyEvent event) {
        for (MyEventListener eventListener: eventListeners) {
            eventListener.processEvent(event);
        }
    }

    private FirebaseAuth mAuth;
    String userStreetId;
    String userBuildingId;
    DatabaseReference myRef2;
    DataSnapshot notificationSnapshot;
    ValueEventListener listenerBuilding;
    ValueEventListener listenerMessages;

    private List<Notification> notifications = new ArrayList<Notification>();

    ListView notificationList;
    AdapterView.OnItemClickListener itemListener;
    Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(eventListeners == null) {
            eventListeners = new LinkedList<>();
        }
        else {
            eventListeners.clear();
        }

        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        // начальная инициализация списка
        // получаем элемент ListView
        notificationList = (ListView) view.findViewById(R.id.notificationList);
        // создаем адаптер
        context = view.getContext();

        mAuth = FirebaseAuth.getInstance();

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    userStreetId = dataSnapshot.child("street_id").getValue().toString();
                    userBuildingId = dataSnapshot.child("building_id").getValue().toString();

                    notifyEventListeners(new MyEvent(this, MyEvent.Type.UpdateAddressID));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        this.addEventListener(new MyEventListener() {
            @Override
            public void processEvent(MyEvent event) {
                if(event.getSource() == null || event.getType() == null) {
                    return;
                }

                switch (event.getType()) {
                    case UpdateAddressID: {

                        if(myRef2 != null) {
                            myRef2.removeEventListener(listenerBuilding);
                        }

                        myRef2 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId)
                                .child("buildings").child(userBuildingId).child("notifications");

                        listenerBuilding = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                notificationSnapshot = dataSnapshot;

                                notifyEventListeners(new MyEvent(this, MyEvent.Type.UpdateNews));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef2.addValueEventListener(listenerBuilding);
                    } break;

                    case UpdateNews: {
                        updateUI();
                        System.gc();
                    } break;
                }
            }
        });

        return view;
    }





    private void updateUI(){
        notifications.clear();
        String userName;

        if(notificationSnapshot != null) {
            for (DataSnapshot n : notificationSnapshot.getChildren()) {
                notifications.add(new Notification(n.child("text").getValue().toString(), n.child("date").getValue().toString()));
                FirebaseDatabase.getInstance().getReference("streets")
                        .child(userStreetId).child("buildings")
                        .child(userBuildingId).child("notificationsRead").child(mAuth.getUid()).child(n.getKey()).setValue(1);
            }
        }
        Collections.reverse(notifications);
        NotificationAdapter notificationAdapter = new NotificationAdapter(getActivity(), R.layout.list_notification, notifications);
        // устанавливаем адаптер
        notificationList.setAdapter(notificationAdapter);

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        eventListeners.clear();
        if(myRef2 != null) {
            myRef2.removeEventListener(listenerBuilding);
        }
        listenerBuilding=null;
        listenerMessages=null;
    }
}