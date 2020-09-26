package com.example.weatherstation;

import androidx.appcompat.app.AppCompatActivity;

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

import static java.lang.Double.isNaN;

public class Chart1 extends AppCompatActivity {

    /* BEGIN config data */
    private String ipAddress = COMMON.CONFIG_IP_ADDRESS;
    private int sampleTime = COMMON.CONFIG_SAMPLE_TIME;

    boolean Temperature_intent;
    boolean Pressure_intent;
    boolean Humidity_intent;
    /* END config data */

    /* BEGIN widgets */
    private TextView textViewIP;
    private TextView textViewSampleTime;
    private TextView textViewError;

    /* Temperature */
    private final double temperatureGraphMaxX = 10.0d;
    private final double temperatureGraphMinX = 0.0d;
    private final double temperatureGraphMaxY = 110.0d;
    private final double temperatureGraphMinY = -30.0d;

    /* Pressure */
    private final double pressureGraphMaxX = 10.0d;
    private final double pressureGraphMinX = 0.0d;
    private final double pressureGraphMaxY = 1260.0d;
    private final double pressureGraphMinY = 260.0d;

    /* Humidity */
    private final double humidityGraphMaxX = 10.0d;
    private final double humidityGraphMinX = 0.0d;
    private final double humidityGraphMaxY = 100.0d;
    private final double humidityGraphMinY = 0.0d;
    /* END widgets */

    private GraphView Graphview;
    private LineGraphSeries<DataPoint> Series;
    private int GraphMaxDataPointsNumber = 1000;
    private double GraphMaxX = 10.0d;
    private double GraphMinX = 0.0d;
    private double GraphMaxY = 110.0d;
    private double GraphMinY = -30.0d;
    private AlertDialog.Builder configAlterDialog;

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

        /* BEGIN get data from inetnt */
        Temperature_intent = getIntent().getBooleanExtra("Temperature", true);
        Pressure_intent = getIntent().getBooleanExtra("Pressure", true);
        Humidity_intent = getIntent().getBooleanExtra("Humidity", true);
        /* BEGIN get data from inetnt */

        setContentView(R.layout.chart_1);

        /* BEGIN initialize graphs */
        TextView Label1 = (TextView)findViewById(R.id.Label1);
        if (Temperature_intent) {
            Label1.setText("Temperature");
            GraphMaxX = temperatureGraphMaxX;
            GraphMinX = temperatureGraphMinX;
            GraphMaxY = temperatureGraphMaxY;
            GraphMinY = temperatureGraphMinY;
        }
        else if (Pressure_intent) {
            Label1.setText("Pressure (hPa)");
            GraphMaxX = pressureGraphMaxX;
            GraphMinX = pressureGraphMinX;
            GraphMaxY = pressureGraphMaxY;
            GraphMinY = pressureGraphMinY;
        }
        else {
            Label1.setText("Humidity (%)");
            GraphMaxX = humidityGraphMaxX;
            GraphMinX = humidityGraphMinX;
            GraphMaxY = humidityGraphMaxY;
            GraphMinY = humidityGraphMinY;
        }

        // https://github.com/jjoe64/GraphView/wiki
        final TextView textView = (TextView) findViewById(R.id.pressureLabel);

        Graphview = (GraphView)findViewById(R.id.Graph1);
        Series = new LineGraphSeries<>(new DataPoint[]{});
        Graphview.addSeries(Series);
        Graphview.getViewport().setXAxisBoundsManual(true);
        Graphview.getViewport().setMinX(GraphMinX);
        Graphview.getViewport().setMaxX(GraphMaxX);
        Graphview.getViewport().setYAxisBoundsManual(true);
        Graphview.getViewport().setMinY(GraphMinY);
        Graphview.getViewport().setMaxY(GraphMaxY);
        /* END initialize graphs */

        /* BEGIN initialize widgets */
        /* BEGIN initialize TextViews */
        textViewIP = findViewById(R.id.textViewIP);
        textViewIP.setText(getIpAddressDisplayText(ipAddress));

        textViewSampleTime = findViewById(R.id.textViewSampleTime);
        textViewSampleTime.setText(getSampleTimeDisplayText(Integer.toString(sampleTime)));

        textViewError = findViewById(R.id.textViewErrorMsg);
        textViewError.setText("");
        /* END initialize TextViews */

        /* BEGIN config alter dialog */
        configAlterDialog = new AlertDialog.Builder(Chart1.this);
        configAlterDialog.setTitle("This will STOP data acquisition. Proceed?");
        configAlterDialog.setIcon(android.R.drawable.ic_dialog_alert);
        configAlterDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                stopRequestTimerTask();
                openConfig();
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
        queue = Volley.newRequestQueue(Chart1.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        //if ((requestCode == COMMON.REQUEST_CODE_CONFIG) && (resultCode == RESULT_OK)) {

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
            case R.id.configBtn: {
                if(requestTimer != null)
                    configAlterDialog.show();
                else
                    openConfig();
                break;
            }
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
    public void onBackPressed()
    {
        startActivity(new Intent(Chart1.this, MainActivity.class));
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

    /**
     * @brief Called when the user taps the 'Config' button.
     * */
    private void openConfig() {
        Intent intent = new Intent(getBaseContext(), ChartConfig.class);
        intent.putExtra("Temperature", Temperature_intent);
        intent.putExtra("Pressure", Pressure_intent);
        intent.putExtra("Humidity", Humidity_intent);
        startActivity(intent);
    }

    private double getTemperatureFromResponse(String response) {
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
            JSONObject data = jObject.getJSONObject("data").getJSONObject("TPH");
            reading = (double)data.get("temperature");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reading;
    }

    private double getPressureFromResponse(String response) {
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
            JSONObject data = jObject.getJSONObject("data").getJSONObject("TPH");
            reading = (double)data.get("pressure");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reading;
    }

    private double getHumidityFromResponse(String response) {
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
            JSONObject data = jObject.getJSONObject("data").getJSONObject("TPH");
            reading = (double)data.get("humidity");
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
            double data;

            // get needed data
            if(Temperature_intent) {
                data = getTemperatureFromResponse(response);
            }
            else if(Pressure_intent) {
                data = getPressureFromResponse(response);
            }
            else {
                data = getHumidityFromResponse(response);
            }

            // update chart
            if (isNaN(data)) {
                errorHandling(COMMON.ERROR_NAN_DATA);

            } else {

                // update plot series
                double timeStamp = requestTimerTimeStamp / 1000.0; // [sec]
                boolean scrollGraph = (timeStamp > GraphMaxX);

                Series.appendData(new DataPoint(timeStamp, data), scrollGraph, GraphMaxDataPointsNumber);

                // refresh chart
                Graphview.onDataChanged(true, true);
            }

            // remember previous time stamp
            requestTimerPreviousTime = requestTimerCurrentTime;
        }
    }
}
