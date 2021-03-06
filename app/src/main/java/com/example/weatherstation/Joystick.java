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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class Joystick extends AppCompatActivity {

    /* BEGIN config data */
    private String ipAddress = COMMON.CONFIG_IP_ADDRESS;
    private int sampleTime = COMMON.CONFIG_SAMPLE_TIME;
    /* END config data */

    /* BEGIN widgets */
    private TextView textViewIP;
    private TextView textViewSampleTime;
    private TextView textViewError;

    /* END widgets */

    /* X Axis Graph */
    private GraphView XAxisGraphview;
    private LineGraphSeries<DataPoint> XAxisSeries;
    private final int XAxisGraphMaxDataPointsNumber = 1000;
    private double XAxisGraphMaxX = 10.0d;
    private double XAxisGraphMinX = 0.0d;
    private double XAxisGraphMaxY = 110.0d;
    private double XAxisGraphMinY = -30.0d;
    private AlertDialog.Builder configAlterDialog;

    /* Y Axis Graph */
    private GraphView YAxisGraphwiev;
    private LineGraphSeries<DataPoint> YAxisSeries;
    private final int YAxisGraphMaxDataPointsNumber = 1000;
    private double YAxisGraphMaxX = 10.0d;
    private double YAxisGraphMinX = 0.0d;
    private double YAxisGraphMaxY = 1260.0d;
    private double YAxisGraphMinY = 260.0d;
    private AlertDialog.Builder configAlterDialog2;

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

        setContentView(R.layout.joystick);

        /* BEGIN initialize widgets */
        /* BEGIN initialize TextViews */
        textViewIP = findViewById(R.id.textViewIP);
        textViewIP.setText(getIpAddressDisplayText(ipAddress));

        textViewSampleTime = findViewById(R.id.textViewSampleTime);
        textViewSampleTime.setText(getSampleTimeDisplayText(Integer.toString(sampleTime)));

        textViewError = findViewById(R.id.textViewErrorMsg);
        textViewError.setText("");
        /* END initialize TextViews */

        /* BEGIN initialize Graphs */
        // https://github.com/jjoe64/GraphView/wiki
        XAxisGraphview = (GraphView)findViewById(R.id.X_graph_joy);
        XAxisSeries = new LineGraphSeries<>(new DataPoint[]{});
        XAxisGraphview.addSeries(XAxisSeries);
        XAxisGraphview.getViewport().setXAxisBoundsManual(true);
        XAxisGraphview.getViewport().setMinX(XAxisGraphMinX);
        XAxisGraphview.getViewport().setMaxX(XAxisGraphMaxX);
        XAxisGraphview.getViewport().setYAxisBoundsManual(true);
        XAxisGraphview.getViewport().setMinY(XAxisGraphMinY);
        XAxisGraphview.getViewport().setMaxY(XAxisGraphMaxY);

        YAxisGraphwiev = (GraphView)findViewById(R.id.Y_graph_joy);
        YAxisSeries = new LineGraphSeries<>(new DataPoint[]{});
        YAxisGraphwiev.addSeries(YAxisSeries);
        YAxisGraphwiev.getViewport().setXAxisBoundsManual(true);
        YAxisGraphwiev.getViewport().setMinX(YAxisGraphMinX);
        YAxisGraphwiev.getViewport().setMaxX(YAxisGraphMaxX);
        YAxisGraphwiev.getViewport().setYAxisBoundsManual(true);
        YAxisGraphwiev.getViewport().setMinY(YAxisGraphMinY);
        YAxisGraphwiev.getViewport().setMaxY(YAxisGraphMaxY);


        /* END initialize Graphs */

        /* BEGIN config alter dialog */
        configAlterDialog = new AlertDialog.Builder(Joystick.this);
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
        queue = Volley.newRequestQueue(Joystick.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        // IoT server IP address
        textViewIP.setText(getIpAddressDisplayText(ipAddress));

        // Sample time (ms)
        textViewSampleTime.setText(getSampleTimeDisplayText(Integer.toString(sampleTime)));
    }

    /**
     * @brief Main activity button onClick procedure - common for all upper main_menu buttons
     * @param v the View (Button) that was clicked
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Joystick.this, MainActivity.class));
        finish();
    }

    /**
     * @brief Create display text for IoT server IP address
     * @param ip IP address (string)
     * @retval Display text for textViewIP widget
     */
    private String getIpAddressDisplayText(String ip) {
        return ("IP: " + ip);
    }

    /**
     * @brief Create display text for requests sample time
     * @param st Sample time in ms (string)
     * @retval Display text for textViewSampleTime widget
     */
    private String getSampleTimeDisplayText(String st) {
        return ("Sample time: " + st + " ms");
    }

    /**
     * @brief Create JSON file URL from IoT server IP.
     * @param ip IP address (string)
     * @retval GET request URL
     */
    private String getURL(String ip) {
        return ("http://" + ip + "/" + COMMON.FILE_NAME);
    }

    /**
     * @brief Handles application errors. Logs an error and passes error code to GUI.
     * @param errorCode local error codes, see: COMMON
     */
    private void errorHandling(int errorCode) {
        switch(errorCode) {
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

    private double getYFromResponse(String response) {
        JSONObject jObject;
        double reading = Float.NaN;

        // Create generic JSON object form string
        try {
            jObject = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return reading;
        }

        // Read chart data form JSON object
        try {
            JSONObject data = jObject.getJSONObject("data").getJSONObject("Joystick");
            reading = (double)data.get("y");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reading;
    }

    private double getXFromResponse(String response) {
        JSONObject jObject;
        double reading = Float.NaN;

        // Create generic JSON object form string
        try {
            jObject = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return reading;
        }

        // Read chart data form JSON object
        try {
            JSONObject data = jObject.getJSONObject("data").getJSONObject("Joystick");
            reading = (double)data.get("x");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reading;
    }
    /**
     * @brief Starts new 'Timer' (if currently not exist) and schedules periodic task.
     */
    private void startRequestTimer() {
        if(requestTimer == null) {
            // set a new Timer
            requestTimer = new Timer();

            // initialize the TimerTask's job
            initializeRequestTimerTask();
            requestTimer.schedule(requestTimerTask, 0, COMMON.DEFAULT_SAMPLE_TIME);

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
                    public void run() { sendGetRequest(); }
                });
            }
        };
    }

    /**
     * @brief Sending GET request to IoT server using 'Volley'.
     */
    private void sendGetRequest() {
        // Instantiate the RequestQueue with Volley
        // https://javadoc.io/doc/com.android.volley/volley/1.1.0-rc2/index.html
        String url = getURL(ipAddress);
        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { responseHandling(response); }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { errorHandling(COMMON.ERROR_RESPONSE); }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /**
     * @brief Validation of client-side time stamp based on 'SystemClock'.
     */
    private long getValidTimeStampIncrease(long currentTime) {
        // Right after start remember current time and return 0
        if(requestTimerFirstRequest)
        {
            requestTimerPreviousTime = currentTime;
            requestTimerFirstRequest = false;
            return 0;
        }

        // After each stop return value not greater than sample time
        // to avoid "holes" in the plot
        if(requestTimerFirstRequestAfterStop)
        {
            if((currentTime - requestTimerPreviousTime) > COMMON.DEFAULT_SAMPLE_TIME)
                requestTimerPreviousTime = currentTime - COMMON.DEFAULT_SAMPLE_TIME;

            requestTimerFirstRequestAfterStop = false;
        }

        // If time difference is equal zero after start
        // return sample time
        if((currentTime - requestTimerPreviousTime) == 0)
            return COMMON.DEFAULT_SAMPLE_TIME;

        // Return time difference between current and previous request
        return (currentTime - requestTimerPreviousTime);
    }

    /**
     * @brief GET response handling - chart data series updated with IoT server data.
     */
    private void responseHandling(String response) {
        if (requestTimer != null) {
            // get time stamp with SystemClock
            long requestTimerCurrentTime = SystemClock.uptimeMillis(); // current time
            requestTimerTimeStamp += getValidTimeStampIncrease(requestTimerCurrentTime);

            // get raw data from JSON response
            double y = getYFromResponse(response);
            double x = getXFromResponse(response);

            // update chart

                // update plot series
                double timeStamp = requestTimerTimeStamp / 1000.0; // [sec]
                boolean scrollGraph = (timeStamp > XAxisGraphMaxX);

                //add new data to chart
                XAxisSeries.appendData(new DataPoint(timeStamp, y), scrollGraph, YAxisGraphMaxDataPointsNumber);
                YAxisSeries.appendData(new DataPoint(timeStamp, x), scrollGraph, YAxisGraphMaxDataPointsNumber);

                // refresh chart
                XAxisGraphview.onDataChanged(true, true);
                YAxisGraphwiev.onDataChanged(true, true);

            // remember previous time stamp
            requestTimerPreviousTime = requestTimerCurrentTime;
        }
    }
}
