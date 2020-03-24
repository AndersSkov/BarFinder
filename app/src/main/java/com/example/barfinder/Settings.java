package com.example.barfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

public class Settings extends AppCompatActivity {

    CheckBox showNearestBar;
    boolean isShowNearestBarChecked;
    ImageButton backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        showNearestBar = findViewById(R.id.ShowNameOfBar);
        isShowNearestBarChecked = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("showNameOfBar", false);
        showNearestBar.setChecked(isShowNearestBarChecked);

    }

    public void showNameOfNearestBar(View view) {
        switch(view.getId()) {
            case R.id.ShowNameOfBar:
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean("showNameOfBar", showNearestBar.isChecked()).apply();
                break;
        }
        if(showNearestBar.isChecked()){
            //nameOfNearestBar.setVisibility(View.INVISIBLE);
        } else {
            //nameOfNearestBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        Intent intent = new Intent();
        intent.putExtra("showNameOfBar", isShowNearestBarChecked);
        setResult(Activity.RESULT_OK, intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("showNameOfBar", showNearestBar.isChecked());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isShowNearestBarChecked = savedInstanceState.getBoolean("showNameOfBar");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
