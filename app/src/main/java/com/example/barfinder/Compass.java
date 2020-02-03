package com.example.barfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Compass extends AppCompatActivity implements SensorEventListener, LocationListener {

    Button settingsButton, barListButton;
    SensorManager sensorManager;
    TextView nearestBar;

    ImageView arrow;

    HashMap<String, Double> distances = new HashMap<>();
    private Location locationObj, destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        nearestBar = findViewById(R.id.NameOfNearestBar);
        arrow = findViewById(R.id.arrow);

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
        destination = new Location("destination");
        locationObj = new Location("currentLocation");
        locationObj.setLatitude(56.148932);
        locationObj.setLongitude(10.168355);

        findNearestBar();
    }

    private void findNearestBar() {
        myDistance(new DistanceCallback() {
            @Override
            public void onCallback(HashMap<String, Double> values) {
                String name = "Max";
                double distance = -1;
                for (Map.Entry<String, Double> entry : values.entrySet()) {
                    if (distance == -1 || entry.getValue() < distance) {
                        distance = entry.getValue();
                        name = entry.getKey();
                    }
                }
                nearestBar.setText(name);
            }
        }, new LatiLongiCallback() {
            @Override
            public void onCallback(double lati, double longi) {
                destination.setLatitude(lati);
                destination.setLongitude(longi);
            }
        });
    }

    public void myDistance(final DistanceCallback distanceCallback, final LatiLongiCallback latiLongiCallback){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                distances.clear();
                final int R = 6371; // Radius of the earth

                for(DataSnapshot d : dataSnapshot.getChildren()){

                    double latDistance = Math.toRadians(d.child("Latitude").getValue(Double.class) - destination.getLatitude());
                    double lonDistance = Math.toRadians(d.child("Longitude").getValue(Double.class) - destination.getLongitude());
                    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                            + Math.cos(Math.toRadians(destination.getLatitude())) * Math.cos(Math.toRadians(d.child("Latitude").getValue(Double.class)))
                            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                    double distance = R * c * 1000; // convert to meters
                    distance = Math.pow(distance, 2);

                    distances.put(d.child("Name").getValue(String.class), distance);
                }
                distanceCallback.onCallback(distances);
                latiLongiCallback.onCallback(56, 10);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void rotateImageView(ImageView imageView, int drawable, float rotate ) {

        // Decode the drawable into a bitmap
        Bitmap bitmapOrg = BitmapFactory.decodeResource( getResources(), drawable);

        // Get the width/height of the drawable
        DisplayMetrics dm = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = bitmapOrg.getWidth(), height = bitmapOrg.getHeight();

        // Initialize a new Matrix
        Matrix matrix = new Matrix();

        // Decide on how much to rotate
        rotate = rotate % 360;

        // Actually rotate the image
        matrix.postRotate( rotate, width, height );

        // recreate the new Bitmap via a couple conditions
        Bitmap rotatedBitmap = Bitmap.createBitmap( bitmapOrg, 0, 0, width, height, matrix, true );
        //BitmapDrawable bmd = new BitmapDrawable( rotatedBitmap );

        //imageView.setImageBitmap( rotatedBitmap );
        imageView.setImageDrawable(new BitmapDrawable(getResources(), rotatedBitmap));
        imageView.setScaleType( ImageView.ScaleType.CENTER );
    }

    //SensorEventListener
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // If we don't have a Location, we break out
        if ( locationObj == null ) return;

        float azimuth = sensorEvent.values[0];

        GeomagneticField geoField = new GeomagneticField( Double
                .valueOf( locationObj.getLatitude() ).floatValue(), Double
                .valueOf( locationObj.getLongitude() ).floatValue(),
                Double.valueOf( locationObj.getAltitude() ).floatValue(),
                System.currentTimeMillis() );

        azimuth -= geoField.getDeclination(); // converts magnetic north into true north

        // Store the bearingTo in the bearTo variable
        float bearTo = locationObj.bearingTo(destination);

        // If the bearTo is smaller than 0, add 360 to get the rotation clockwise.
        if (bearTo < 0) {
            bearTo = bearTo + 360;
        }

        //This is where we choose to point it
        float direction = bearTo - azimuth;

        // If the direction is smaller than 0, add 360 to get the rotation clockwise.
        if (direction < 0) {
            direction = direction + 360;
        }

        rotateImageView( arrow, R.drawable.beer_arrow, direction );
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //LocationListener
    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            locationObj = location;
        }
        findNearestBar();
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
