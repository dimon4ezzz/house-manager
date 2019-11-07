package com.dvor.my.mydvor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import java.util.LinkedList;
import java.util.List;

public class MyNotifications extends Service {
    NotificationManager notificationManager;
    int count = 2;

    private static List<MyEventListener> eventListeners;

    public void addEventListener(MyEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public static void notifyEventListeners(MyEvent event) {
        for (MyEventListener eventListener: eventListeners) {
            eventListener.processEvent(event);
        }
    }

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    String userStreetId;
    String organizationId;
    String userBuildingId;
    DatabaseReference myRef2;
    DatabaseReference myRef3;
    DatabaseReference myRef4;
    DatabaseReference myRef5;
    DataSnapshot messageSnapshot;
    ValueEventListener listenerBuilding;
    ValueEventListener listenerMessages;
    DataSnapshot notificationsSnapshot;
    DataSnapshot notificationsRead;
    ValueEventListener listenerNotifications;
    ValueEventListener listenerRead;

    ListView messageList;
    Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("0", "My channel",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("My channel description");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);

            NotificationChannel channel_1 = new NotificationChannel("1", "Сообщения от УК",
                    NotificationManager.IMPORTANCE_HIGH);
            channel_1.setDescription("My channel description");
            channel_1.enableLights(true);
            channel_1.setLightColor(Color.RED);
            channel_1.enableVibration(true);
            notificationManager.createNotificationChannel(channel_1);

            NotificationChannel channel_2 = new NotificationChannel("2", "Уведомления для вашего дома",
                    NotificationManager.IMPORTANCE_HIGH);
            channel_2.setDescription("My channel description");
            channel_2.enableLights(true);
            channel_2.setLightColor(Color.RED);
            channel_2.enableVibration(true);
            notificationManager.createNotificationChannel(channel_2);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "0")
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setContentTitle("Здравствуйте, уважаемый пользователь")
                    .setContentText("Вы будете получать push-уведомления от MyDvor");

            startForeground(1, notification.build());
            deleteNotification();
        }

        if(eventListeners == null) {
            eventListeners = new LinkedList<>();
        }
        else {
            eventListeners.clear();
        }

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user == null) {
                    notificationManager.cancelAll();
                    stopSelf();
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    userStreetId = dataSnapshot.child("street_id").getValue().toString();
                    userBuildingId = dataSnapshot.child("building_id").getValue().toString();

                    notifyEventListeners(new MyEvent(this, Type.UpdateAddressID));
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
                                .child("buildings").child(userBuildingId).child("organization_id");

                        listenerBuilding = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                organizationId = dataSnapshot.getValue().toString();

                                notifyEventListeners(new MyEvent(this, Type.UpdateOrganizationId));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef2.addValueEventListener(listenerBuilding);


                        if(myRef4 != null) {
                            myRef4.removeEventListener(listenerNotifications);
                        }

                        myRef4 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId)
                                .child("buildings").child(userBuildingId).child("notifications");

                        listenerNotifications = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                notificationsSnapshot = dataSnapshot;

                                notifyEventListeners(new MyEvent(this, Type.UpdateNotifications));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef4.addValueEventListener(listenerNotifications);
                    } break;

                    case UpdateOrganizationId: {

                        if(myRef3 != null) {
                            myRef3.removeEventListener(listenerMessages);
                        }

                        myRef3 = FirebaseDatabase.getInstance().getReference("organization")
                                .child(organizationId).child("messages").child(mAuth.getUid());

                        listenerMessages = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                messageSnapshot = dataSnapshot;

                                notifyEventListeners(new MyEvent(this, Type.UpdateUI));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef3.addValueEventListener(listenerMessages);
                    } break;

                    case UpdateNotifications: {
                        if(myRef5 != null) {
                            myRef5.removeEventListener(listenerRead);
                        }

                        myRef5 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId)
                                .child("buildings").child(userBuildingId).child("notificationsRead").child(mAuth.getUid());

                        listenerRead = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                notificationsRead = dataSnapshot;

                                notifyEventListeners(new MyEvent(this, Type.UpdateUI_2));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef5.addValueEventListener(listenerRead);
                    } break;

                    case UpdateUI: {
                        updateUI();
                        System.gc();
                    } break;

                    case UpdateUI_2: {
                        updateUI_2();
                        System.gc();
                    } break;
                }
            }
        });
    }

    private void updateUI(){
        deleteMessages();

        if(messageSnapshot != null) {

            Intent notificationIntent = new Intent(this, MessageFakeActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            for (DataSnapshot n : messageSnapshot.getChildren()) {
                if ((Integer.parseInt(n.child("income").getValue().toString())==1) && (Integer.parseInt(n.child("read").getValue().toString())==0)) {
                    sendNotif("Сообщение от УК:", n.child("Text").getValue().toString(), "1", contentIntent, true);
                }
            }
        }
    }

    private void updateUI_2(){
        deleteNotifications();

        if(notificationsSnapshot != null) {
            Intent notificationIntent = new Intent(this, NotificationsFakeActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            for (DataSnapshot n : notificationsSnapshot.getChildren()) {
                if(notificationsRead.child(n.getKey()).getValue() == null) {
                    sendNotif("Уведомление для вашего дома:", n.child("text").getValue().toString(), "2", contentIntent, false);
                }
            }
        }
    }

    private void deleteMessages() {
        NotificationChannel channel = notificationManager.getNotificationChannel("1");
        notificationManager.deleteNotificationChannel("1");
        notificationManager.createNotificationChannel(channel);
        count = notificationManager.getActiveNotifications().length;
    }

    private void deleteNotifications() {
        NotificationChannel channel = notificationManager.getNotificationChannel("2");
        notificationManager.deleteNotificationChannel("2");
        notificationManager.createNotificationChannel(channel);
        count = notificationManager.getActiveNotifications().length;
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    void sendNotif(String title, String message, String channelId, PendingIntent contentIntent, boolean mess) {
        NotificationCompat.Builder builder;
        if (mess) {
            builder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_menu_message)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true);
        }
        else {
            builder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_menu_notif)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true);
        }

        notificationManager.notify(count, builder.build());
        count++;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

        eventListeners.clear();
        if(myRef2 != null) {
            myRef2.removeEventListener(listenerBuilding);
        }
        if(myRef3 != null) {
            myRef3.removeEventListener(listenerMessages);
        }
        if(myRef4 != null) {
            myRef4.removeEventListener(listenerNotifications);
        }
        if(myRef5 != null) {
            myRef5.removeEventListener(listenerRead);
        }
        listenerBuilding=null;
        listenerMessages=null;
        listenerNotifications=null;
        listenerRead=null;
    }

    private void deleteNotification(){
        Thread r =  new Thread(new deleteNot());
        r.start();
    }

    private class deleteNot implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            notificationManager.deleteNotificationChannel("0");
        }
    }
}