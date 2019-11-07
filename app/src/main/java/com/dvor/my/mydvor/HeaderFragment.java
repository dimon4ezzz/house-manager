package com.dvor.my.mydvor;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HeaderFragment extends Fragment {

    private FirebaseAuth mAuth;
    private TextView userName;
    private TextView userAddress;
    String userStreetId;
    String userBuildingId;
    String userApartment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.nav_header_menu, container, false);
        userName = view.findViewById(R.id.user_name);
        userAddress = view.findViewById(R.id.user_address);

        mAuth = FirebaseAuth.getInstance();

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    userName.setText(dataSnapshot.child("name").getValue().toString() + " " + dataSnapshot.child("surname").getValue().toString());
                    userStreetId = dataSnapshot.child("street_id").getValue().toString();
                    userBuildingId = dataSnapshot.child("building_id").getValue().toString();
                    userApartment = dataSnapshot.child("apartment").getValue().toString();

                    DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId);

                    myRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null) {
                                String address = dataSnapshot.child("street").getValue().toString() + ", д. " + dataSnapshot.child("buildings").child(userBuildingId).child("number").getValue().toString() + ", кв. " + userApartment;
                                userAddress.setText(address);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
}