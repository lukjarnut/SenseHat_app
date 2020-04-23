package com.example.weatherstation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class MainConfig extends AppCompatActivity {

    EditText ipEditText;
    /* END config textboxes */

    int count_views = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_config);

        ipEditText = findViewById(R.id.ipEditTextConfig);
        String ip = COMMON.CONFIG_IP_ADDRESS;
        ipEditText.setText(ip);

    }
    @Override
    public void onBackPressed() {
        COMMON.CONFIG_IP_ADDRESS = ipEditText.getText().toString();

        finish();
    }
}
