package com.example.weatherstation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

        /* BEGIN get data from intent*/
        Temperature_boolean = getIntent().getBooleanExtra("Temperature", true);
        Pressure_boolean = getIntent().getBooleanExtra("Pressure", true);
        Humidity_boolean = getIntent().getBooleanExtra("Humidity", true);
        /* END get data from intent*/

        /* BEGIN initialise switches*/
        Temperature_switch = (Switch)findViewById(R.id.TemperatureSW);
        Pressure_switch = (Switch)findViewById(R.id.PressureSW);
        Humidity_switch = (Switch)findViewById(R.id.HumiditySW);

        if(Temperature_boolean) { Temperature_switch.setChecked(true); }
        else { Temperature_switch.setChecked(false); }

        if(Pressure_boolean) { Pressure_switch.setChecked(true); }
        else { Pressure_switch.setChecked(false); }

        if(Humidity_boolean) { Humidity_switch.setChecked(true); }
        else { Humidity_switch.setChecked(false); }
        /* END initialise switches*/

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
        int count = count();
        switch(count) {
            case 0:
                configAlterDialog.show();
                break;
            case 1:
                Intent intent_1 = new Intent(getBaseContext(), Chart1.class);
                intent_1.putExtra("Temperature", Temperature_boolean);
                intent_1.putExtra("Pressure", Pressure_boolean);
                intent_1.putExtra("Humidity", Humidity_boolean);
                startActivity(intent_1);
                finish();
                break;
            case 2:
                Intent intent_2 = new Intent(getBaseContext(), Chart2.class);
                intent_2.putExtra("Temperature", Temperature_boolean);
                intent_2.putExtra("Pressure", Pressure_boolean);
                intent_2.putExtra("Humidity", Humidity_boolean);
                startActivity(intent_2);
                finish();
                break;
            case 3:
                startActivity(new Intent(ChartConfig.this, Chart3.class));
                finish();
                break;
        }
    }

    /**
     * @brief Check how many charts user selected.
     */
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
}

