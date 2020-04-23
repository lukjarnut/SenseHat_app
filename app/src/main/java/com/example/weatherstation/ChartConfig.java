package com.example.weatherstation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class ChartConfig extends AppCompatActivity {

    EditText sampleTimeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_config);

        sampleTimeEditText = findViewById(R.id.sampleTimeEditTextConfig);
        int st = COMMON.CONFIG_SAMPLE_TIME;
        sampleTimeEditText.setText(Integer.toString(st));
    }

    @Override
    public void onBackPressed() {
        COMMON.CONFIG_SAMPLE_TIME = Integer.parseInt(sampleTimeEditText.getText().toString());
        finish();
    }
}
