package com.ghijoon.klinikdayamedika;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public void btnCariRuteOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);

        startActivity(intent);
    }

    public void btnTentangOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), AboutActivity.class);

        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
