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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StockFragment extends Fragment {

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
    DataSnapshot retailersStreet;
    DatabaseReference myRef2;
    DatabaseReference myRef3;
    List<DataSnapshot> retailers = new LinkedList<DataSnapshot>();
    List<DataSnapshot> shopsID = new LinkedList<DataSnapshot>();
    ValueEventListener listenerRetailersStreet;
    ValueEventListener listenerRetailers;
    AdapterView.OnItemClickListener itemListener;

    private List<Stock> stocks = new ArrayList<Stock>();
    ListView stockList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(eventListeners == null) {
            eventListeners = new LinkedList<>();
        }
        else {
            eventListeners.clear();
        }

        View view = inflater.inflate(R.layout.fragment_stock, container, false);
        // начальная инициализация списка
        //setInitialData();
        // получаем элемент ListView
        stockList = (ListView) view.findViewById(R.id.stocksList);
        // создаем адаптер
        Context context = view.getContext();

        mAuth = FirebaseAuth.getInstance();

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    userStreetId = dataSnapshot.child("street_id").getValue().toString();

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
                            myRef2.removeEventListener(listenerRetailersStreet);
                        }

                        myRef2 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId).child("retailers");

                        listenerRetailersStreet = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                retailersStreet = dataSnapshot;

                                notifyEventListeners(new MyEvent(this, MyEvent.Type.UpdateRetailersStreet));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef2.addValueEventListener(listenerRetailersStreet);
                    } break;

                    case UpdateRetailersStreet: {

                        if(myRef3 != null) {
                            myRef3.removeEventListener(listenerRetailers);
                        }

                        myRef3 = FirebaseDatabase.getInstance().getReference("retailers");

                        listenerRetailers = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                retailers.clear();
                                shopsID.clear();

                                if(dataSnapshot.getValue() != null) {
                                    for (DataSnapshot retailerStreet : retailersStreet.getChildren()) {
                                        shopsID.add(retailerStreet.child("shops"));

                                        String retailerID = retailerStreet.child("id").getValue().toString();
                                        retailers.add(dataSnapshot.child(retailerID));

                                        notifyEventListeners(new MyEvent(this, MyEvent.Type.UpdateRetailers));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef3.addValueEventListener(listenerRetailers);
                    } break;

                    case UpdateRetailers: {
                        updateUI();
                        System.gc();
                    } break;
                }
            }
        });
        return view;
    }

    private void updateUI(){
        stocks.clear();

        for (int i = 0; i < retailers.size(); i++) {
          //String retailerName = retailers.get(i).child("name").getValue().toString();

            DataSnapshot sales = retailers.get(i).child("sales");

            for(DataSnapshot sale: sales.getChildren()) {
                stocks.add(new Stock( sale.child("title").getValue().toString(),
                        sale.child("text").getValue().toString(),
                       sale.child("address").getValue().toString(), sale.child("img").getValue().toString()));
            }
        }

        StockAdapter newsAdapter = new StockAdapter(getActivity(), R.layout.list_stocks, stocks);
        // устанавливаем адаптер
        stockList.setAdapter(newsAdapter);

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        eventListeners.clear();
        if(myRef2 != null) {
            myRef2.removeEventListener(listenerRetailersStreet);
        }
        if(myRef3 != null) {
            myRef3.removeEventListener(listenerRetailers);
        }
        listenerRetailersStreet=null;
        listenerRetailers=null;
    }
}