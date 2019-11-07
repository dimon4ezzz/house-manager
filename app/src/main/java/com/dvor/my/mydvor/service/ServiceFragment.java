package com.dvor.my.mydvor.service;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dvor.my.mydvor.MyEvent;
import com.dvor.my.mydvor.MyEventListener;
import com.dvor.my.mydvor.R;
import com.dvor.my.mydvor.Type;
import com.dvor.my.mydvor.data.Service;
import com.dvor.my.mydvor.service.ServiceAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class ServiceFragment extends Fragment {

    private static List<MyEventListener> eventListeners;
    String[] data = {"Доставка воды", "Продукты", "Сантехник"};

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
    DataSnapshot servicesSnapshot;
    ValueEventListener listenerBuilding;
    ValueEventListener listenerService;


    private List<Service> services = new ArrayList<>();
    ListView servicesList;
    AdapterView.OnItemClickListener itemListener;
    Long serviceType;


    private  void spinner(View view)
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = view.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Title");
        spinner.setSelection(0);
        // обработчик нажатия
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
               serviceType=id;
               takeDataSnapshot();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(eventListeners == null) {
            eventListeners = new LinkedList<>();
        }
        else {
            eventListeners.clear();
        }

        View view = inflater.inflate(R.layout.fragment_service, container, false);
        // начальная инициализация списка
        // получаем элемент ListView
        servicesList = view.findViewById(R.id.newsList);
        // создаем адаптер
        Context context = view.getContext();
        spinner(view);
        takeDataSnapshot();
        return view;
    }

    private void takeDataSnapshot()
    {
        mAuth = FirebaseAuth.getInstance();
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
                    } break;

                    case UpdateOrganizationId: {

                        if(myRef3 != null) {
                            myRef3.removeEventListener(listenerService);
                        }

                        myRef3 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId)
                                .child("services").child(Long.toString(serviceType));

                        listenerService = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                servicesSnapshot = dataSnapshot;

                                notifyEventListeners(new MyEvent(this, Type.UpdateNews));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef3.addValueEventListener(listenerService);
                    } break;

                    case UpdateNews: {
                        updateUI();
                        System.gc();
                    } break;
                }
            }
        });
    }

    private void updateUI(){
        services.clear();


        if(servicesSnapshot != null) {
            for (DataSnapshot n : servicesSnapshot.getChildren()) {
                services.add(new Service(n.child("provider").getValue().toString(), n.child("text").getValue().toString(),
                        n.child("phone").getValue().toString()));

            }
        }

        ServiceAdapter serviceAdapter = new ServiceAdapter(getActivity(), R.layout.list_service, services);
        // устанавливаем адаптер
        servicesList.setAdapter(serviceAdapter);
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
            myRef3.removeEventListener(listenerService);
        }
        listenerBuilding=null;
        listenerService=null;
    }
}

