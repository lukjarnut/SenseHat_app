package com.example.weatherstation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {

    private String ipAddress = COMMON.DEFAULT_IP_ADDRESS;
    private int sampleTime = COMMON.DEFAULT_SAMPLE_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_menu);
    }

    public void btns_onClick(View v) {
        switch (v.getId()) {
            case R.id.chart_button: {
                openCharts();
                break;
            }
//            case R.id.ledbtn: {
//
//                break;
//            }
            case R.id.settings_button: {
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
        configBundle.putString(COMMON.CONFIG_IP_ADDRESS, ipAddress);
        configBundle.putInt(COMMON.CONFIG_SAMPLE_TIME, sampleTime);
        openConfigIntent.putExtras(configBundle);
        startActivityForResult(openConfigIntent, COMMON.REQUEST_CODE_CONFIG);
    }

    private void openCharts() {
        Button button_data = (Button) findViewById(R.id.chart_button);
        Intent intent = new Intent();
        intent.putExtra(COMMON.IP_ADDRESS);
        intent.putExtra(COMMON.CONFIG_SAMPLE_TIME);
        setResult(RESULT_OK, intent);
        button_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Chart3.class));
            }
        });
    }
}
