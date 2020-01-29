package com.example.barfinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compass extends AppCompatActivity implements SensorEventListener, LocationListener {

    Button settingsButton, barListButton;
    SensorManager sensorManager;
    TextView nearestBar;

    LatiLongi myLocation;
    HashMap<String,Double> distances = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        nearestBar = findViewById(R.id.NameOfNearestBar);

        settingsButton = findViewById(R.id.SettingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Compass.this, Settings.class);
                startActivity(intent);
            }
        });
        barListButton = findViewById(R.id.BarListButton);
        barListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Compass.this, BarList.class);
                startActivity(intent);
            }
        });
        myLocation = new LatiLongi(56.148932, 10.168355);

        findNearestBar();
    }

    private void findNearestBar() {
        myDistance(new DistanceCallback() {
            @Override
            public void onCallback(HashMap<String, Double> values) {
                String name = "Max";
                double distance = -1;
                for (Map.Entry<String, Double> entry : values.entrySet()){
                    if(distance == -1 || entry.getValue() < distance){
                        distance = entry.getValue();
                        name = entry.getKey();
                    }
                }
                nearestBar.setText(name);
            }
        });
    }

    public void myDistance(final DistanceCallback distanceCallback){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                distances.clear();
                final int R = 6371; // Radius of the earth

                for(DataSnapshot d : dataSnapshot.getChildren()){

                    double latDistance = Math.toRadians(d.child("Latitude").getValue(Double.class) - myLocation.getLatitude());
                    double lonDistance = Math.toRadians(d.child("Longitude").getValue(Double.class) - myLocation.getLongitude());
                    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                            + Math.cos(Math.toRadians(myLocation.getLatitude())) * Math.cos(Math.toRadians(d.child("Latitude").getValue(Double.class)))
                            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                    double distance = R * c * 1000; // convert to meters
                    distance = Math.pow(distance, 2);

                    distances.put(d.child("Name").getValue(String.class), distance);
                }
                distanceCallback.onCallback(distances);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

//    public static double distance(double lat1, double lat2, double lon1, double lon2) {
//
//        final int R = 6371; // Radius of the earth
//
//        double latDistance = Math.toRadians(lat2 - lat1);
//        double lonDistance = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        double distance = R * c * 1000; // convert to meters
//
//        distance = Math.pow(distance, 2);
//
//        return Math.sqrt(distance);
//    }

    //SensorEventListener
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //LocationListener
    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            myLocation = new LatiLongi(location.getLatitude(), location.getLongitude());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
