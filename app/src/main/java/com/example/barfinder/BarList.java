package com.example.barfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BarList extends AppCompatActivity {

    RecyclerView listOfBars;
    RecyclerView.LayoutManager barsLayoutManager;

    List<ModelBar> bars;

    BarListAdapter barListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_list);

        bars = new ArrayList<>();

        listOfBars = findViewById(R.id.RecyclerView);

        barsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        listOfBars.setLayoutManager(barsLayoutManager);
        listOfBars.setHasFixedSize(true);

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d: dataSnapshot.getChildren()){
                    String address, name;
                    Double latitude, longitude;
                    address = d.child("Address").getValue(String.class);
                    latitude = d.child("Latitude").getValue(Double.class);
                    longitude = d.child("Longitude").getValue(Double.class);
                    name = d.child("Name").getValue(String.class);
                    bars.add(new ModelBar(address, latitude, longitude, name));
                }
                barListAdapter = new BarListAdapter(bars, BarList.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}
