package com.example.weatherstation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class ChartRPY_Config extends AppCompatActivity {

    EditText sampleTimeEditText;
    String in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_rpy_config);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            in = extras.getString("Chart");
        } else {
            in = (String) savedInstanceState.getSerializable("Chart");
        }

        sampleTimeEditText = findViewById(R.id.sampleTimeEditTextConfig);
        int st = COMMON.CONFIG_SAMPLE_TIME;
        sampleTimeEditText.setText(Integer.toString(st));
    }


    @Override
    public void onBackPressed() {
        COMMON.CONFIG_SAMPLE_TIME = Integer.parseInt(sampleTimeEditText.getText().toString());
        if ( in == "ChartRPY" ) {
            startActivity(new Intent(this, ChartRPY.class));
            finish();
        }
        if( in == "ChartJoy"){
            startActivity(new Intent(this, Table.class));
            finish();
        }
        if( in == "Table") {
            startActivity(new Intent(this, Table.class));
            finish();
        }
        else{
            finish();
        }
    }

}

