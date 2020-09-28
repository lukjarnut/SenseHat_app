package com.example.weatherstation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.SeekBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class LED_Matrix extends AppCompatActivity {

    /* BEGIN widgets*/
    SeekBar redSeekBar, greenSeekBar, blueSeekBar;
    View colorView;
    EditText urlText;
    /* END widgets*/

    /* BEGIN colors */
    int a, r, g, b, ARGB;
    int ledOffColor;
    Vector ledOffColorVec;
    Integer[][][] ledColors = new Integer[8][8][3];
    /* BEGIN colors */

    /* BEGIN request */
    String url = "https://192.168.1.15/led_display.php";
    private RequestQueue queue;
    Map<String, String> paramsClear = new HashMap<String, String>();
    /* END request */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.led_matrix);

        /* BEGIN color data init */
        ledOffColor = ResourcesCompat.getColor(getResources(), R.color.ledIndBachground, null);
        ledOffColorVec = intToRgb(ledOffColor);

        ARGB = ledOffColor;

        a = 0xff;
        r = 0x00;
        g = 0x00;
        b = 0x00;

        clearLedArray();
        /* END color data init */

        /* BEGIN widgets init*/
        redSeekBar = (SeekBar)findViewById(R.id.seekBarR);
        redSeekBar.setMax(255);
        redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar){/**/}
            public void onStopTrackingTouch(SeekBar seekBar) {
                ARGB = seekBarUpdate('R', progressChangedValue);
                colorView.setBackgroundColor(ARGB);
            }
        });

        greenSeekBar = (SeekBar)findViewById(R.id.seekBarG);
        greenSeekBar.setMax(255);
        greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {
                ARGB = seekBarUpdate('G', progressChangedValue);
                colorView.setBackgroundColor(ARGB);
            }
        });

        blueSeekBar = (SeekBar)findViewById(R.id.seekBarB);
        blueSeekBar.setMax(255);
        blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {
                ARGB = seekBarUpdate('B', progressChangedValue);
                colorView.setBackgroundColor(ARGB);
            }
        });

        colorView = findViewById(R.id.colorView);

        urlText = findViewById(R.id.urlText);
        urlText.setText(url);
        /*END widgets init */

        /* BEGIN volley request queue */
        queue = Volley.newRequestQueue(this);

        for(int i =0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                String data = "["+Integer.toString(i)+","+Integer.toString(j)+",0,0,0]";
                paramsClear.put(ledIndexToTag(i, j), data);
            }
        }
        /* END volley request queue */
    }

    int seekBarUpdate(char color, int value) {
        switch(color) {
            case 'R': r = value; break;
            case 'G': g = value; break;
            case 'B': b = value; break;
            default: /* Do nothing */ break;
        }
        a = (r+g+b)/3;
        return argbToInt(a, r, g, b);
    }

    public int argbToInt(int _a, int _r, int _g, int _b){
        return (_a & 0xff) << 24 | (_r & 0xff) << 16 | (_g & 0xff) << 8 | (_b & 0xff);
    }

    public Vector intToRgb(int argb) {
        int _r = (argb >> 16) & 0xff;
        int _g = (argb >> 8) & 0xff;
        int _b = argb & 0xff;
        Vector rgb = new Vector(3);
        rgb.add(0, _r);
        rgb.add(1, _g);
        rgb.add(2, _b);
        return rgb;
    }

    public void changeLedIndicatorColor(View v) {
        v.setBackgroundColor(ARGB);
        String tag = (String)v.getTag();
        Vector index = ledTagToIndex(tag);
        int x = (int)index.get(0);
        int y = (int)index.get(1);
        ledColors[x][y][0] = r;
        ledColors[x][y][1] = g;
        ledColors[x][y][2] = b;
    }

    Vector ledTagToIndex(String tag) {
        Vector vec = new Vector(2);
        vec.add(0, Character.getNumericValue(tag.charAt(3)));
        vec.add(1, Character.getNumericValue(tag.charAt(4)));
        return vec;
    }

    String ledIndexToTag(int x, int y) {
        return "LED" + Integer.toString(x) + Integer.toString(y);
    }

    String ledIndexToJsonColor(int x, int y) {
        String _x = Integer.toString(x);
        String _y = Integer.toString(y);
        String _r = Integer.toString(ledColors[x][y][0]);
        String _g = Integer.toString(ledColors[x][y][1]);
        String _b = Integer.toString(ledColors[x][y][2]);
        return "["+_x+","+_y+","+_r+","+_g+","+_b+"]";
    }

    boolean ledColorNotNull(int x, int y) {
        return !((ledColors[x][y][0]==null)||(ledColors[x][y][1]==null)||(ledColors[x][y][2]==null));
    }

    public void clearLedArray() {
        for(int i = 0; i< 8; i++) {
            for(int j = 0; j < 8; j++) {
                ledColors[i][j][0] = null;
                ledColors[i][j][1] = null;
                ledColors[i][j][2] = null;
            }
        }
    }

    public void clearAllLed(View v) {
        TableLayout tb = (TableLayout)findViewById(R.id.LedTable);
        View ledInd;
        for(int i = 0; i< 8; i++) {
            for (int j = 0; j < 8; j++) {
                ledInd = tb.findViewWithTag(ledIndexToTag(i, j));
                ledInd.setBackgroundColor(ledOffColor);
            }
        }
        clearLedArray();
        sendClearRequest();
    }

    public Map<String, String> getLedDisplayParams() {
        String led;
        String color;
        Map<String, String> params = new HashMap<String, String>();
        for(int i = 0; i< 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(ledColorNotNull(i, j)) {
                    led = ledIndexToTag(i, j);
                    color = ledIndexToJsonColor(i, j);
                    params.put(led, color);
                }
            }
        }
        return params;
    }

    public void sendControlRequest(View v)
    {
        //url = urlText.getText().toString();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if(msg != null)
                            Log.d("Error.Response", msg);
                        else {

                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams(){
                return getLedDisplayParams();
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(postRequest);
    }

    void sendClearRequest()
    {
        //url = urlText.getText().toString();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if(msg != null)
                            Log.d("Error.Response", msg);
                        else {

                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return paramsClear;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(postRequest);
    }


}
