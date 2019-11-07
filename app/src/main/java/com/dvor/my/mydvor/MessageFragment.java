package com.dvor.my.mydvor;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;

import java.util.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MessageFragment extends Fragment implements View.OnClickListener{

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
    String organizationId;
    String userBuildingId;
    DatabaseReference myRef2;
    DatabaseReference myRef3;
    DataSnapshot messageSnapshot;
    ValueEventListener listenerBuilding;
    ValueEventListener listenerMessages;

    private List<Message> messages = new ArrayList<Message>();
    private TextView messageText;
    ListView messageList;
    AdapterView.OnItemClickListener itemListener;
    Context context;

    //сохранение текста неотправленного сообщения
    //костыльно но работает
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("message", (messageText.getText().toString()));
        MainActivity.savedMessage=messageText.getText().toString();
    }


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

        View view = inflater.inflate(R.layout.fragment_message, container, false);
        // начальная инициализация списка
        // получаем элемент ListView
        messageList = (ListView) view.findViewById(R.id.messageList);
        // создаем адаптер
        context = view.getContext();

        mAuth = FirebaseAuth.getInstance();

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());

        Button button = (Button) view.findViewById(R.id.send_message);
        button.setOnClickListener(this);

        messageText=(TextView)view.findViewById(R.id.message_text) ;
        messageText.setText(MainActivity.savedMessage);
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
                                .child("buildings").child(userBuildingId).child("organization_id");

                        listenerBuilding = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                organizationId = dataSnapshot.getValue().toString();

                                notifyEventListeners(new MyEvent(this, MyEvent.Type.UpdateOrganizationId));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef2.addValueEventListener(listenerBuilding);
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

                                notifyEventListeners(new MyEvent(this, MyEvent.Type.UpdateNews));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef3.addValueEventListener(listenerMessages);
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



    @Override
    public void onClick(View v) {
        messageText.clearFocus();
        //скрыть клавиатуру
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(messageText.getWindowToken(), 0);

        switch (v.getId()) {
            case R.id.send_message:
                sendMessage();
                break;
        }
    }

    private void sendMessage()
    {
        if (messageText.getText().toString()==null)
            return;
        if (messageText.getText().toString().trim().length() == 0 )
            return;
        MessageBD mes=new MessageBD(0, messageText.getText().toString(), (new Date()).toString());
        int messageId=messages.size()+1;
        messageText.setText("");
        MainActivity.savedMessage="";
        FirebaseDatabase.getInstance().getReference("organization")
             .child(organizationId).child("messages")
                .child(mAuth.getUid()).child(Integer.toString(messageId)).setValue(mes);
    }

    private void updateUI(){
        messages.clear();
        String userName;

        if(messageSnapshot != null) {
            for (DataSnapshot n : messageSnapshot.getChildren()) {
                if (Integer.parseInt(n.child("income").getValue().toString())==0)
                    userName="Вы:";
                else {
                    userName = "Управляющая компания:";
                    FirebaseDatabase.getInstance().getReference("organization")
                            .child(organizationId).child("messages")
                            .child(mAuth.getUid()).child(n.getKey()).child("read").setValue(1);
                }
                messages.add(new Message(userName, n.child("Text").getValue().toString(), n.child("date").getValue().toString()));
            }
        }
        Collections.reverse(messages);
        MessageAdapter messageAdapter = new MessageAdapter(getActivity(), R.layout.list_message, messages);
        // устанавливаем адаптер
        messageList.setAdapter(messageAdapter);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        eventListeners.clear();
        if(myRef2 != null) {
            myRef2.removeEventListener(listenerBuilding);
        }
        if(myRef3 != null) {
            myRef3.removeEventListener(listenerMessages);
        }
        listenerBuilding=null;
        listenerMessages=null;
    }
}