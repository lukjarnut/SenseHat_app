package com.example.weatherstation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    private String ipAddress = COMMON.DEFAULT_IP_ADDRESS;
    private int sampleTime = COMMON.DEFAULT_SAMPLE_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_menu);
    }

    public void btns_onClick(View v) {
        switch (v.getId()) {
            case R.id.joy__button: {
                openJoy();
                break;
            }
            case R.id.rpy__button: {
                openRPY();
                break;
            }
            case R.id.chart_button: {
                openCharts();
                break;
            }
            case R.id.led_button: {
                openLED();
                break;
            }
            case R.id.settings_button: {
                openConfig();
                break;
            }
            case R.id.table_button: {
                openTable();
                break;
            }
            default: {
                // do nothing
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        finish();
        moveTaskToBack(true);
    }

    private void openRPY() { startActivity(new Intent(MainActivity.this, ChartRPY.class)); }

    private void openJoy() { startActivity(new Intent(MainActivity.this, Joystick.class)); }

    private void openConfig() {
        startActivity(new Intent(MainActivity.this, MainConfig.class));
    }

    private void openLED() { startActivity(new Intent (MainActivity.this, LED_Matrix.class)); }

    private void openCharts() {
        switch (COMMON.NO_CHARTS) {
            case 1:
                startActivity(new Intent(MainActivity.this, Chart1.class));
                break;
            case 2:
                startActivity(new Intent(MainActivity.this, Chart2.class));
                break;
            case 3:
                startActivity(new Intent(MainActivity.this, Chart3.class));
                break;
        }
    }
    private void openTable() { startActivity(new Intent (MainActivity.this, Table.class)); }
}
