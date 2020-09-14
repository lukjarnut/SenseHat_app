package com.example.weatherstation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class ChartConfig extends AppCompatActivity {

    EditText sampleTimeEditText;
    Switch Temperature_switch;
    Switch Pressure_switch;
    Switch Humidity_switch;

    Boolean Temperature_boolean;
    Boolean Pressure_boolean;
    Boolean Humidity_boolean;

    private AlertDialog.Builder configAlterDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_config);

        Temperature_switch = (Switch)findViewById(R.id.TemperatureSW);
        Pressure_switch = (Switch)findViewById(R.id.PressureSW);
        Humidity_switch = (Switch)findViewById(R.id.HumiditySW);


        sampleTimeEditText = findViewById(R.id.sampleTimeEditTextConfig);
        int st = COMMON.CONFIG_SAMPLE_TIME;
        sampleTimeEditText.setText(Integer.toString(st));

        /* BEGIN config alter dialog */
        configAlterDialog = new AlertDialog.Builder(ChartConfig.this);
        configAlterDialog.setTitle("No charts selected!");
        configAlterDialog.setIcon(android.R.drawable.ic_dialog_alert);
        configAlterDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        /* END config alter dialog */
    }


    @Override
    public void onBackPressed() {
        COMMON.CONFIG_SAMPLE_TIME = Integer.parseInt(sampleTimeEditText.getText().toString());
        int count = count();
        switch(count) {
            case 0:
                configAlterDialog.show();
                break;
            case 1:
                startActivity(new Intent(ChartConfig.this, Chart1.class));
                finish();
                break;
            case 2:
                startActivity(new Intent(ChartConfig.this, Chart2.class));
                finish();
                break;
            case 3:
                startActivity(new Intent(ChartConfig.this, Chart3.class));
                finish();
                break;
        }
    }

    private int count(){
        int number_of_charts = 0;
        Temperature_boolean = Temperature_switch.isChecked();
        Pressure_boolean = Pressure_switch.isChecked();
        Humidity_boolean = Humidity_switch.isChecked();

        if(Temperature_boolean){
            number_of_charts++;
        }
        if(Pressure_boolean){
            number_of_charts++;
        }
        if(Humidity_boolean){
            number_of_charts++;
        }
        return number_of_charts;
    }

    private void alert()
    {

    }
}

