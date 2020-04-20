package com.example.weatherstation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void btns_onClick(View v) {
        switch (v.getId()) {
            case R.id.chartBtn: {
                openCharts();
                break;
            }
//            case R.id.ledbtn: {
//
//                break;
//            }
            case R.id.settingsbtn: {
                openConfig();
                break;
            }
            default: {
                // do nothing
            }
        }
    }

    private void openConfig() {
        Intent openConfigIntent = new Intent(this, ConfigActivity.class);
        Bundle configBundle = new Bundle();
        //configBundle.putString(COMMON.CONFIG_IP_ADDRESS, ipAddress);
        //configBundle.putInt(COMMON.CONFIG_SAMPLE_TIME, sampleTime);
        openConfigIntent.putExtras(configBundle);
        startActivityForResult(openConfigIntent, COMMON.REQUEST_CODE_CONFIG);
    }

    private void openCharts() {
        Button button_data = (Button) findViewById(R.id.chartbtn);
        button_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Chart3.class));
            }
        });
    }
}
