package com.example.weatherstation;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Double.isNaN;

public class Table extends AppCompatActivity {
    /* BEGIN config data */
    private String ipAddress = COMMON.CONFIG_IP_ADDRESS;
    private int sampleTime = COMMON.CONFIG_SAMPLE_TIME;
    private AlertDialog.Builder configAlterDialog;
    /* END config data */

    /* BEGIN widgets */
    private TextView textViewIP;
    private TextView textViewSampleTime;
    private TextView textViewError;

    private TextView roll_view;
    private TextView pitch_view;
    private TextView yaw_view;
    private TextView temperature_view;
    private TextView pressure_view;
    private TextView humidity_view;
    private TextView joy_x_view;
    private TextView joy_y_view;

    /* END widgets */

    /* BEGIN request timer */
    private RequestQueue queue;
    private Timer requestTimer;
    private long requestTimerTimeStamp = 0;
    private long requestTimerPreviousTime = -1;
    private boolean requestTimerFirstRequest = true;
    private boolean requestTimerFirstRequestAfterStop;
    private TimerTask requestTimerTask;
    private final Handler handler = new Handler();
    /* END request timer */

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table);

        /* BEGIN initialize widgets */
        /* BEGIN initialize TextViews */
        roll_view = (TextView) findViewById(R.id.Tabel_value_1);
        pitch_view = (TextView) findViewById(R.id.Tabel_value_2);
        yaw_view = (TextView) findViewById(R.id.Tabel_value_3);
        temperature_view = (TextView) findViewById(R.id.Tabel_value_4);
        humidity_view = (TextView) findViewById(R.id.Tabel_value_5);
        pressure_view = (TextView) findViewById(R.id.Tabel_value_6);
        joy_x_view = (TextView) findViewById(R.id.Tabel_value_7);
        joy_y_view = (TextView) findViewById(R.id.Tabel_value_8);

        textViewIP = findViewById(R.id.textViewIP);
        textViewIP.setText(getIpAddressDisplayText(ipAddress));

        textViewSampleTime = findViewById(R.id.textViewSampleTime);
        textViewSampleTime.setText(getSampleTimeDisplayText(Integer.toString(sampleTime)));

        textViewError = findViewById(R.id.textViewErrorMsg);
        textViewError.setText("");
        /* END initialize TextViews */


        /* BEGIN config alter dialog */
        configAlterDialog = new AlertDialog.Builder(Table.this);
        configAlterDialog.setTitle("This will STOP data acquisition. Proceed?");
        configAlterDialog.setIcon(android.R.drawable.ic_dialog_alert);
        configAlterDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                stopRequestTimerTask();
            }
        });
        configAlterDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        /* END config alter dialog */
        /* END initialize widgets */

        // Initialize Volley request queue
        queue = Volley.newRequestQueue(Table.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        textViewSampleTime.setText(getSampleTimeDisplayText(Integer.toString(sampleTime)));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Table.this, MainActivity.class));
        finish();
    }

    /**
     * @param ip IP address (string)
     * @brief Create display text for IoT server IP address
     * @retval Display text for textViewIP widget
     */
    private String getIpAddressDisplayText(String ip) {
        return ("IP: " + ip);
    }

    /**
     * @param st Sample time in ms (string)
     * @brief Create display text for requests sample time
     * @retval Display text for textViewSampleTime widget
     */
    private String getSampleTimeDisplayText(String st) {
        return ("Sample time: " + st + " ms");
    }

    /**
     * @param ip IP address (string)
     * @brief Create JSON file URL from IoT server IP.
     * @retval GET request URL
     */
    private String getURL(String ip) {
        return ("http://" + ip + "/" + COMMON.FILE_NAME);
    }


    /**
     * @brief Validation of client-side time stamp based on 'SystemClock'.
     */
    private long getValidTimeStampIncrease(long currentTime) {
        // Right after start remember current time and return 0
        if (requestTimerFirstRequest) {
            requestTimerPreviousTime = currentTime;
            requestTimerFirstRequest = false;
            return 0;
        }

        // After each stop return value not greater than sample time
        // to avoid "holes" in the plot
        if (requestTimerFirstRequestAfterStop) {
            if ((currentTime - requestTimerPreviousTime) > COMMON.CONFIG_SAMPLE_TIME)
                requestTimerPreviousTime = currentTime - COMMON.CONFIG_SAMPLE_TIME;

            requestTimerFirstRequestAfterStop = false;
        }

        // If time difference is equal zero after start
        // return sample time
        if ((currentTime - requestTimerPreviousTime) == 0)
            return COMMON.CONFIG_SAMPLE_TIME;

        // Return time difference between current and previous request
        return (currentTime - requestTimerPreviousTime);
    }

    /**
     * @param errorCode local error codes, see: COMMON
     * @brief Handles application errors. Logs an error and passes error code to GUI.
     */
    private void errorHandling(int errorCode) {
        switch (errorCode) {
            case COMMON.ERROR_TIME_STAMP:
                textViewError.setText("ERR #1");
                Log.d("errorHandling", "Request time stamp error.");
                break;
            case COMMON.ERROR_NAN_DATA:
                textViewError.setText("ERR #2");
                Log.d("errorHandling", "Invalid JSON data.");
                break;
            case COMMON.ERROR_RESPONSE:
                textViewError.setText("ERR #3");
                Log.d("errorHandling", "GET request VolleyError.");
                break;
            default:
                textViewError.setText("ERR ??");
                Log.d("errorHandling", "Unknown error.");
                break;
        }
    }

    /**
     * @brief GET response handling - chart data series updated with IoT server data.
     */
    @SuppressLint("SetTextI18n")
    private void responseHandling(String response) {
        if (requestTimer != null) {
            // get time stamp with SystemClock
            long requestTimerCurrentTime = SystemClock.uptimeMillis(); // current time
            requestTimerTimeStamp += getValidTimeStampIncrease(requestTimerCurrentTime);

            JSONObject jObject = new JSONObject();

            // get raw data from JSON response
            double temperature_value = Float.NaN;
            double humidity_value = Float.NaN;
            double pressure_value = Float.NaN;

            double roll_value = Float.NaN;
            double pitch_value = Float.NaN;
            double yaw_value = Float.NaN;

            double joy_x_value = Float.NaN;
            double joy_y_value = Float.NaN;

            // Create generic JSON object form string
            try {
                jObject = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //read temperature, pressure and humidity from JSON object
            try {
                JSONObject TPH = jObject.getJSONObject("data").getJSONObject("TPH");
                temperature_value = (double) TPH.get("temperature");
                humidity_value = (double)(TPH.get("humidity"));
                pressure_value = (double)(TPH.get("pressure"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject RPY = jObject.getJSONObject("data").getJSONObject("RPY");
                roll_value = (double)(RPY.get("roll"));
                pitch_value = (double)(RPY.get("pitch"));
                yaw_value = (double)(RPY.get("yaw"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject joystick = jObject.getJSONObject("data").getJSONObject("Joystick");
                joy_x_value = (double)(joystick.get("x"));
                joy_y_value = (double)(joystick.get("y"));
            } catch (JSONException e) {
                e.printStackTrace();
            }



            //update values
            temperature_view.setText(Double.toString(temperature_value));
            pressure_view.setText(Double.toString(pressure_value));
            humidity_view.setText(Double.toString(humidity_value));
            roll_view.setText(Double.toString(roll_value));
            pitch_view.setText(Double.toString(pitch_value));
            yaw_view.setText(Double.toString(yaw_value));
            joy_x_view.setText(Double.toString(joy_x_value));
            joy_y_view.setText(Double.toString(joy_y_value));

            // remember previous time stamp
            requestTimerPreviousTime = requestTimerCurrentTime;
        }
    }

    /**
     * @param v the View (Button) that was clicked
     * @brief Main activity button onClick procedure - common for all upper main_menu buttons
     */
    public void btns_onClick(View v) {
        switch (v.getId()) {
            case R.id.startBtn: {
                startRequestTimer();
                break;
            }
            case R.id.stopBtn: {
                stopRequestTimerTask();
                break;
            }
            default: {
                // do nothing
            }
        }
    }

    /**
     * @brief Starts new 'Timer' (if currently not exist) and schedules periodic task.
     */
    private void startRequestTimer() {
        if (requestTimer == null) {
            // set a new Timer
            requestTimer = new Timer();

            // initialize the TimerTask's job
            initializeRequestTimerTask();
            requestTimer.schedule(requestTimerTask, 0, COMMON.CONFIG_SAMPLE_TIME);

            // clear error message
            textViewError.setText("");
        }
    }

    /**
     * @brief Stops request timer (if currently exist)
     * and sets 'requestTimerFirstRequestAfterStop' flag.
     */
    private void stopRequestTimerTask() {
        // stop the timer, if it's not already null
        if (requestTimer != null) {
            requestTimer.cancel();
            requestTimer = null;
            requestTimerFirstRequestAfterStop = true;
        }
    }

    /**
     * @brief Initialize request timer period task with 'Handler' post method as 'sendGetRequest'.
     */
    private void initializeRequestTimerTask() {
        requestTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        sendGetRequest();
                    }
                });
            }
        };
    }

    private void sendGetRequest() {
        // Instantiate the RequestQueue with Volley
        // https://javadoc.io/doc/com.android.volley/volley/1.1.0-rc2/index.html
        String url = getURL(ipAddress);
        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseHandling(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorHandling(COMMON.ERROR_RESPONSE);
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}