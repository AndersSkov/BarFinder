package com.example.barfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class Settings extends AppCompatActivity {

    CheckBox showNearestBar;
    TextView nameOfNearestBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        showNearestBar = findViewById(R.id.ShowNameOfBar);
        nameOfNearestBar = findViewById(R.id.NameOfNearestBar);
    }

    public void showNameOfNearestBar(View view) {
        if(showNearestBar.isChecked()){
            //nameOfNearestBar.setVisibility(View.INVISIBLE);
        } else {
            //nameOfNearestBar.setVisibility(View.VISIBLE);
        }
    }
}
