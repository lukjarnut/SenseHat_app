package com.example.weatherstation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class MainConfig extends AppCompatActivity {

    /* BEGIN config textboxes */
    EditText ipEditText;
    EditText sampleTimeEditText;
    /* END config textboxes */

    int count_views = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_config);

        ipEditText = findViewById(R.id.ipEditTextConfig);
        String ip = COMMON.CONFIG_IP_ADDRESS;
        ipEditText.setText(ip);

        sampleTimeEditText = findViewById(R.id.sampleTimeEditTextConfig);
        int st = COMMON.CONFIG_SAMPLE_TIME;
        sampleTimeEditText.setText(Integer.toString(st));

    }
    @Override
    public void onBackPressed() {
        COMMON.CONFIG_IP_ADDRESS = ipEditText.getText().toString();
        COMMON.CONFIG_SAMPLE_TIME = Integer.parseInt(sampleTimeEditText.getText().toString());

        finish();
    }
}
