package com.example.weatherstation;

public class COMMON {
    // activities request codes
    public final static int REQUEST_CODE_CONFIG = 1;

    // configuration info: names and default values
    public static String CONFIG_IP_ADDRESS = "192.168.1.34";
    public final static String DEFAULT_IP_ADDRESS = "192.168.1.70";

    public static int CONFIG_SAMPLE_TIME = 500;
    public final static int DEFAULT_SAMPLE_TIME = 500;
    public static int NO_CHARTS = 3;

    // error codes
    public final static int ERROR_TIME_STAMP = -1;
    public final static int ERROR_NAN_DATA = -2;
    public final static int ERROR_RESPONSE = -3;

    // IoT server data
    public final static String FILE_NAME = "chartdata_2.json"; //"/sense_hat/sense_joy.php?t=c&h=%&p=hpa";
}
