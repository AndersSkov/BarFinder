package com.example.barfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.HashMap;
import java.util.Map;

public class Compass extends AppCompatActivity implements SensorEventListener {

    Button settingsButton, barListButton, rotateButton, showInMapsButton;
    TextView nearestBar;
    ImageView arrow;

    static Compass instance;

    HashMap<String, Pair2> distancesHashMap = new HashMap<String, Pair2>();
    HashMap<String, Pair> latiLongiHashMap = new HashMap<>();
    Location myLocation, destination;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    String address;

    SensorManager mSensorManager;
    Sensor mGyroscope, mAccSensor;

    String nameOfClosestBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_COARSE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                updateLocation();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Toast.makeText(Compass.this, "You must accept", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();

        instance = this;

        nearestBar = findViewById(R.id.NameOfNearestBar);
        arrow = findViewById(R.id.arrow);

        settingsButton = findViewById(R.id.SettingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Compass.this, Settings.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
            }
        });

        barListButton = findViewById(R.id.BarListButton);
        barListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Compass.this, BarList.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            }
        });

        rotateButton = findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateImageView(arrow, R.drawable.beer_arrow, 196);
            }
        });

        showInMapsButton = findViewById(R.id.showInMapsButton);
        showInMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(!address.isEmpty()){
                        address.replace(' ', '+');
                        Uri gmmUri = Uri.parse("geo:0,0?q=" + address); Intent mapSearchIntent = new Intent(
                                android.content.Intent.ACTION_VIEW, gmmUri);
                        mapSearchIntent.setPackage("com.google.android.apps.maps"); startActivity(mapSearchIntent);
                        startActivity(mapSearchIntent);
                    }
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Couldn't locate the location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        destination = new Location("");
        destination.setLatitude(56.148211);
        destination.setLongitude(10.200571);
        myLocation = new Location("");
        myLocation.setLatitude(56.159167);
        myLocation.setLongitude(10.206569);

        findNearestBar();
    }

    public void updateLocation() {
        buildLocationRequest();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }

    public void updateMyLocation(Location value){
        Compass.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myLocation = value;
                findNearestBar();
            }
        });
    }

    public PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MyLocationService.class);
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    public static Compass getInstance() {
        return instance;
    }

    private void findNearestBar() {
        myDistance(new DistanceCallback() {
            @Override
            public void onCallback(HashMap<String, Pair2> values) {
                nameOfClosestBar = "Max";
                double distance = -1;
                for (Map.Entry<String, Pair2> entry : values.entrySet()) {
                    if (distance == -1 || (double) entry.getValue().getDistance() < distance) {
                        distance = (double) entry.getValue().getDistance();
                        nameOfClosestBar = entry.getKey();
                        address = (String) entry.getValue().getAddress();
                    }
                }
                nearestBar.setText(nameOfClosestBar);
            }
        }, new LatiLongiCallback() {
            @Override
            public void onCallback(HashMap<String, Pair> values) {
                for (Map.Entry<String, Pair> entry : values.entrySet()) {
                    if(entry.getKey().equals(nameOfClosestBar)){
                        destination.setLatitude((Double) entry.getValue().getLati());
                        destination.setLongitude((Double) entry.getValue().getLongi());
                    }
                }
            }
        });
    }

    public void myDistance(final DistanceCallback distanceCallback, final LatiLongiCallback latiLongiCallback){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                distancesHashMap.clear();
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

                    distancesHashMap.put(d.child("Name").getValue(String.class), new Pair2(distance, d.child("Address").getValue(String.class)));
                    latiLongiHashMap.put(d.child("Name").getValue(String.class), new Pair(d.child("Latitude").getValue(Double.class), d.child("Longitude").getValue(Double.class)));
                }
                distanceCallback.onCallback(distancesHashMap);
                latiLongiCallback.onCallback(latiLongiHashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void rotateImageView(ImageView imageView, int drawable, float rotate ) {
        Matrix matrix = new Matrix();
        Bitmap source = BitmapFactory.decodeResource(this.getResources(), R.drawable.beer_arrow);
        matrix.postRotate(rotate);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, imageView.getWidth(), imageView.getHeight(), true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(rotatedBitmap);
    }

    //SensorEventListener
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // If we don't have a Location, we break out
        if ( myLocation == null ) return;

        float azimuth = sensorEvent.values[0];

        GeomagneticField geoField = new GeomagneticField( Double
                .valueOf( myLocation.getLatitude() ).floatValue(), Double
                .valueOf( myLocation.getLongitude() ).floatValue(),
                Double.valueOf( myLocation.getAltitude() ).floatValue(),
                System.currentTimeMillis() );

        azimuth -= geoField.getDeclination(); // converts magnetic north into true north

        // Store the bearingTo in the bearTo variable
        float bearTo = myLocation.bearingTo(destination);

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

        rotateImageView(arrow, R.drawable.beer_arrow, direction);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccSensor);
    }
}
