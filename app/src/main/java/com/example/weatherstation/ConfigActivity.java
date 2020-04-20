package com.example.weatherstation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class ConfigActivity extends AppCompatActivity {

    EditText ipEditText;
    EditText sampleTimeEditText;
    /* END config textboxes */

    int count_views = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // get the Intent that started this Activity
        Intent intent = getIntent();

        // get the Bundle that stores the data of this Activity
        Bundle configBundle = intent.getExtras();

        ipEditText = findViewById(R.id.ipEditTextConfig);
        String ip = configBundle.getString(COMMON.CONFIG_IP_ADDRESS, COMMON.DEFAULT_IP_ADDRESS);
        ipEditText.setText(ip);

        sampleTimeEditText = findViewById(R.id.sampleTimeEditTextConfig);
        int st = configBundle.getInt(COMMON.CONFIG_SAMPLE_TIME, COMMON.DEFAULT_SAMPLE_TIME);
        sampleTimeEditText.setText(Integer.toString(st));



    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(COMMON.CONFIG_IP_ADDRESS, ipEditText.getText().toString());
        intent.putExtra(COMMON.CONFIG_SAMPLE_TIME, sampleTimeEditText.getText().toString());
        setResult(RESULT_OK, intent);
//        if(count_views == 1) {
//            startActivity(new Intent(MainActivity1.this, MainActivity1.class));
//        }
//        else if(count_views == 2) {
//            startActivity(new Intent(MainActivity2.this, MainActivity2.class));
//        }

        finish();
    }
}
