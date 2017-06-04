package com.example.example4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.example4.utils.ReadFile;
import com.example.example4.utils.WriteFile;

/**
 * Smart Phone Sensing Example 4 - 2017. Wifi received signal strength.
 */
public class MainActivity extends Activity implements OnClickListener, SensorEventListener{

    /**
     * The wifi manager.
     */
    private WifiManager wifiManager;
    private SensorManager sensorManager;
    private static WriteFile f = new WriteFile();

    private static double sigmaCoeff[][] = new double[44][20];
    private static double meuCoeff[][] = new double[44][20];
    private static double comfusionMatrix[][] = new double[20][20];
    private static double belief[] = new double[20];
    private static double[][] fileData = new double[44][10];
    /**
     * The accelerometer.
     */
    private Sensor accelerometer;
    /**
     * The text view.
     */
    private TextView textRssi;
    private long time= System.currentTimeMillis();
    private static int cell = 0;
    /**
     * The button.
     */
    private Button buttonRssi;

    private File appDirectory;
    private File logDirectory;
    private File logFile;
    private  PrintWriter fout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if ( true ) {
            System.out.println(System.getenv());

            appDirectory = Environment.getExternalStorageDirectory();
            String[] list = appDirectory.list();
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }
            System.out.println(appDirectory.setWritable(true));
            logDirectory = new File( appDirectory +"/RSSData" );
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }
            System.out.println(logDirectory.canWrite());




            // create app folder
        }

        // Create items.
        textRssi = (TextView) findViewById(R.id.textRSSI);
        buttonRssi = (Button) findViewById(R.id.buttonRSSI);
        // Set listener for the button.
        buttonRssi.setOnClickListener(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // if the default accelerometer exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // set accelerometer
            accelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, accelerometer,
                    1000);
        } else {
            // No accelerometer!
        }
        if(false) {
            if (sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) != null) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), 1000);
                fout.println("TYPE_GAME_ROTATION_VECTOR registered");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), 1000);
                fout.println("TYPE_GRAVITY");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) != null) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR), 1000);
                fout.println("TYPE_GEOMAGNETIC_ROTATION_VECTOR");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), 5000);
                fout.println("TYPE_ROTATION_VECTOR");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), 100);
                fout.println("TYPE_STEP_COUNTER");
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR), 100);
                fout.println("TYPE_STEP_DETECTOR");
            }
            fout.println("Registration Complete... at" + System.currentTimeMillis());
            fout.flush();
        }

        initGaussianCoeff();



    }

    // onResume() registers the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer,
                //SensorManager.SENSOR_DELAY_NORMAL
                1000);
    }

    // onPause() unregisters the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onClick2(View v) {
        initBelief();
        resetFileData();
        updateBelief();
        cell++;
    }
    public void onClick(View v) {
        // Set text.

        textRssi.setText("\n\tScan all access points:");
        logFile = new File( logDirectory, "RSSData_cell_no" + cell++ +"time_" + time + ".txt" );
        try{
            fout = new PrintWriter( logFile);
        }
        catch(Exception e){}
        // Set wifi manager.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        textRssi.setText("Collecting Cell Data from cell "+ cell);

        // Start a wifi scan.
        for(int i = 0 ; i < 10; i++){
            wifiManager.startScan();
            // Store results in a list.
            List<ScanResult> scanResults = wifiManager.getScanResults();
            // Write results to a
            for (ScanResult scanResult : scanResults) {
                StringBuffer b = new StringBuffer("RSSData iter :"+ i);
                //textRssi.setText(textRssi.getText() + "\n\tSSID = "
                //      + scanResult.SSID + "    RSSI = "
                //    + scanResult.level + "dBm" +"     MAC:" + scanResult.BSSID +"     capabilities:"+ scanResult.capabilities
                //+"\tdescribeContents:"+ scanResult.describeContents()+"\tcenterFreq0:"+ scanResult.centerFreq0 +"\tcenterFreq1:"+
                //scanResult.centerFreq1+"\tchannelWidth:"+scanResult.channelWidth + "\tfrequency:"+ scanResult.frequency);
                b.append(scanResult.level);
                b.append("|");
                b.append(scanResult.BSSID);
                fout.println(b.toString());
            }
            fout.flush();

            try{
                Thread.sleep(1000);
            }
            catch(Exception e){

            }
        }
        textRssi.setText("Completed Collection... in cell "+ cell);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(true) {}
        else{
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                StringBuffer b = new StringBuffer("TYPE_ACCELEROMETER:");
                b.append(
                        event.accuracy+"|"+
                                event.timestamp+"|");
                for(int i = 0 ; i < event.values.length; i++){
                    b.append( event.values[i]+"|");
                }
                fout.println(b.toString());
                fout.flush();
                b = null;
            }
            if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
                StringBuffer b = new StringBuffer("TYPE_STEP_DETECTOR:");
                b.append(
                        event.accuracy+"|"+
                                event.timestamp+"|");
                for(int i = 0 ; i < event.values.length; i++){
                    b.append( event.values[i]+"|");
                }
                fout.println(b.toString());
                fout.flush();
                b = null;
            }
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
                StringBuffer b = new StringBuffer("TYPE_STEP_COUNTER:");
                b.append(
                        event.accuracy+"|"+
                                event.timestamp+"|");
                for(int i = 0 ; i < event.values.length; i++){
                    b.append( event.values[i]+"|");
                }
                fout.println(b.toString());
                fout.flush();
                b = null;
            }
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                StringBuffer b = new StringBuffer("TYPE_ROTATION_VECTOR:");
                b.append(
                        event.accuracy+"|"+
                                event.timestamp+"|");
                for(int i = 0 ; i < event.values.length; i++){
                    b.append( event.values[i]+"|");
                }
                fout.println(b.toString());
                fout.flush();
                b = null;
            }
            if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){
                StringBuffer b = new StringBuffer("TYPE_GEOMAGNETIC_ROTATION_VECTOR:");
                b.append(
                        event.accuracy+"|"+
                                event.timestamp+"|");
                for(int i = 0 ; i < event.values.length; i++){
                    b.append( event.values[i]+"|");
                }
                fout.println(b.toString());
                fout.flush();
                b = null;
            }
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
                StringBuffer b = new StringBuffer("TYPE_GRAVITY:");
                b.append(
                        event.accuracy+"|"+
                                event.timestamp+"|");
                for(int i = 0 ; i < event.values.length; i++){
                    b.append( event.values[i]+"|");
                }
                fout.println(b.toString());
                fout.flush();
                b = null;
            }
            if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR){
                StringBuffer b = new StringBuffer("TYPE_GAME_ROTATION_VECTOR:");
                b.append(
                        event.accuracy+"|"+
                                event.timestamp+"|");
                for(int i = 0 ; i < event.values.length; i++){
                    b.append( event.values[i]+"|");
                }
                fout.println(b.toString());
                fout.flush();
                b = null;
            }}

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void initGaussianCoeff(){
        for(int i = 0 ; i < 44 ; i ++){
            for (int j = 0 ; j < 20; j++){
                sigmaCoeff[i][j] = 0;
                meuCoeff[i][j] = 0 ;
            }
            meuCoeff[0][1] = 82.0;
            sigmaCoeff[0][1] = 6.4;
            meuCoeff[0][2] = 85.0;
            sigmaCoeff[0][2] = 4.6;
            meuCoeff[0][3] = 81.0;
            sigmaCoeff[0][3] = 13.53;
            meuCoeff[0][4] = 72.0;
            sigmaCoeff[0][4] = 41.92;
            meuCoeff[0][5] = 71.0;
            sigmaCoeff[0][5] = 5.68333333333;
            meuCoeff[0][7] = 70.0;
            sigmaCoeff[0][7] = 5.07333333333;
            meuCoeff[0][8] = 78.0;
            sigmaCoeff[0][8] = 6.18;
            meuCoeff[0][9] = 67.0;
            sigmaCoeff[0][9] = 25.8;
            meuCoeff[1][1] = 75.0;
            sigmaCoeff[1][1] = 9.73666666667;
            meuCoeff[1][2] = 75.0;
            sigmaCoeff[1][2] = 11.0633333333;
            meuCoeff[1][3] = 69.0;
            sigmaCoeff[1][3] = 11.2733333333;
            meuCoeff[1][4] = 61.0;
            sigmaCoeff[1][4] = 8.71333333333;
            meuCoeff[1][5] = 67.0;
            sigmaCoeff[1][5] = 9.03666666667;
            meuCoeff[1][6] = 56.0;
            sigmaCoeff[1][6] = 18.4233333333;
            meuCoeff[1][7] = 68.0;
            sigmaCoeff[1][7] = 14.5;
            meuCoeff[1][8] = 72.0;
            sigmaCoeff[1][8] = 10.4866666667;
            meuCoeff[1][9] = 64.0;
            sigmaCoeff[1][9] = 13.8;
            meuCoeff[2][1] = 55.0;
            sigmaCoeff[2][1] = 7.92666666667;
            meuCoeff[2][2] = 60.0;
            sigmaCoeff[2][2] = 11.91;
            meuCoeff[2][3] = 67.0;
            sigmaCoeff[2][3] = 21.9333333333;
            meuCoeff[3][1] = 80.0;
            sigmaCoeff[3][1] = 13.2133333333;
            meuCoeff[3][2] = 81.0;
            sigmaCoeff[3][2] = 2.49;
            meuCoeff[3][3] = 78.0;
            sigmaCoeff[3][3] = 30.4466666667;
            meuCoeff[3][5] = 61.0;
            sigmaCoeff[3][5] = 12.2966666667;
            meuCoeff[5][1] = 64.0;
            sigmaCoeff[5][1] = 16.32;
            meuCoeff[6][1] = 69.0;
            sigmaCoeff[6][1] = 6.86333333333;
            meuCoeff[6][2] = 72.0;
            sigmaCoeff[6][2] = 14.3633333333;
            meuCoeff[6][3] = 66.0;
            sigmaCoeff[6][3] = 7.91333333333;
            meuCoeff[6][4] = 59.0;
            sigmaCoeff[6][4] = 51.3533333333;
            meuCoeff[6][5] = 61.0;
            sigmaCoeff[6][5] = 22.57;
            meuCoeff[6][6] = 51.0;
            sigmaCoeff[6][6] = 5.69333333333;
            meuCoeff[6][7] = 67.0;
            sigmaCoeff[6][7] = 5.90666666667;
            meuCoeff[6][8] = 70.0;
            sigmaCoeff[6][8] = 6.49;
            meuCoeff[7][1] = 55.0;
            sigmaCoeff[7][1] = 11.7466666667;
            meuCoeff[7][2] = 61.0;
            sigmaCoeff[7][2] = 13.4733333333;
            meuCoeff[7][3] = 68.0;
            sigmaCoeff[7][3] = 22.7433333333;
            meuCoeff[8][1] = 75.0;
            sigmaCoeff[8][1] = 15.0633333333;
            meuCoeff[8][2] = 81.0;
            sigmaCoeff[8][2] = 6.75666666667;
            meuCoeff[8][3] = 76.0;
            sigmaCoeff[8][3] = 13.3166666667;
            meuCoeff[8][5] = 82.0;
            sigmaCoeff[8][5] = 8.37333333333;
            meuCoeff[9][1] = 72.0;
            sigmaCoeff[9][1] = 11.1566666667;
            meuCoeff[9][2] = 69.0;
            sigmaCoeff[9][2] = 8.71666666667;
            meuCoeff[9][3] = 61.0;
            sigmaCoeff[9][3] = 29.12;
            meuCoeff[9][4] = 55.0;
            sigmaCoeff[9][4] = 23.18;
            meuCoeff[9][5] = 49.0;
            sigmaCoeff[9][5] = 14.0366666667;
            meuCoeff[9][6] = 54.0;
            sigmaCoeff[9][6] = 30.9166666667;
            meuCoeff[9][7] = 50.0;
            sigmaCoeff[9][7] = 26.85;
            meuCoeff[9][8] = 57.0;
            sigmaCoeff[9][8] = 10.3766666667;
            meuCoeff[9][9] = 65.0;
            sigmaCoeff[9][9] = 5.56333333333;
            meuCoeff[9][10] = 61.0;
            sigmaCoeff[9][10] = 8.37333333333;
            meuCoeff[12][2] = 61.0;
            sigmaCoeff[12][2] = 13.8433333333;
            meuCoeff[12][3] = 67.0;
            sigmaCoeff[12][3] = 27.13;
            meuCoeff[14][3] = 72.0;
            sigmaCoeff[14][3] = 11.6133333333;
            meuCoeff[16][4] = 73.0;
            sigmaCoeff[16][4] = 3.24333333333;
            meuCoeff[16][5] = 71.0;
            sigmaCoeff[16][5] = 20.72;
            meuCoeff[16][6] = 75.0;
            sigmaCoeff[16][6] = 6.06333333333;
            meuCoeff[16][7] = 64.0;
            sigmaCoeff[16][7] = 5.49;
            meuCoeff[16][8] = 69.0;
            sigmaCoeff[16][8] = 14.4833333333;
            meuCoeff[16][9] = 76.0;
            sigmaCoeff[16][9] = 14.95;
            meuCoeff[17][4] = 72.0;
            sigmaCoeff[17][4] = 9.79333333333;
            meuCoeff[17][5] = 68.0;
            sigmaCoeff[17][5] = 12.46;
            meuCoeff[17][6] = 64.0;
            sigmaCoeff[17][6] = 3.42333333333;
            meuCoeff[18][5] = 62.0;
            sigmaCoeff[18][5] = 10.38;
            meuCoeff[18][6] = 58.0;
            sigmaCoeff[18][6] = 17.1933333333;
            meuCoeff[18][7] = 60.0;
            sigmaCoeff[18][7] = 10.6733333333;
            meuCoeff[18][8] = 60.0;
            sigmaCoeff[18][8] = 13.3566666667;
            meuCoeff[18][9] = 47.0;
            sigmaCoeff[18][9] = 23.2133333333;
            meuCoeff[19][5] = 81.0;
            sigmaCoeff[19][5] = 12.96;
            meuCoeff[20][5] = 73.0;
            sigmaCoeff[20][5] = 15.8633333333;
            meuCoeff[20][6] = 83.0;
            sigmaCoeff[20][6] = 5.77333333333;
            meuCoeff[20][7] = 69.0;
            sigmaCoeff[20][7] = 20.14;
            meuCoeff[20][8] = 75.0;
            sigmaCoeff[20][8] = 22.4766666667;
            meuCoeff[25][8] = 69.0;
            sigmaCoeff[25][8] = 20.98;
            meuCoeff[25][10] = 68.0;
            sigmaCoeff[25][10] = 22.7066666667;
            meuCoeff[25][11] = 65.0;
            sigmaCoeff[25][11] = 22.19;
            meuCoeff[25][12] = 70.0;
            sigmaCoeff[25][12] = 9.23;
            meuCoeff[25][13] = 73.0;
            sigmaCoeff[25][13] = 3.63666666667;
            meuCoeff[26][8] = 70.0;
            sigmaCoeff[26][8] = 23.02;
            meuCoeff[26][10] = 68.0;
            sigmaCoeff[26][10] = 22.0033333333;
            meuCoeff[26][11] = 64.0;
            sigmaCoeff[26][11] = 21.82;
            meuCoeff[26][12] = 70.0;
            sigmaCoeff[26][12] = 9.55666666667;
            meuCoeff[26][13] = 73.0;
            sigmaCoeff[26][13] = 4.99;
            meuCoeff[27][8] = 69.0;
            sigmaCoeff[27][8] = 19.7666666667;
            meuCoeff[27][10] = 67.0;
            sigmaCoeff[27][10] = 26.1233333333;
            meuCoeff[27][11] = 65.0;
            sigmaCoeff[27][11] = 18.4633333333;
            meuCoeff[27][12] = 70.0;
            sigmaCoeff[27][12] = 12.12;
            meuCoeff[27][13] = 73.0;
            sigmaCoeff[27][13] = 3.14333333333;
            meuCoeff[30][11] = 70.0;
            sigmaCoeff[30][11] = 16.8933333333;
            meuCoeff[30][12] = 64.0;
            sigmaCoeff[30][12] = 31.1633333333;
            meuCoeff[30][13] = 66.0;
            sigmaCoeff[30][13] = 4.87;
            meuCoeff[30][14] = 60.0;
            sigmaCoeff[30][14] = 23.1633333333;
            meuCoeff[31][11] = 70.0;
            sigmaCoeff[31][11] = 15.9133333333;
            meuCoeff[31][12] = 63.0;
            sigmaCoeff[31][12] = 21.27;
            meuCoeff[31][13] = 66.0;
            sigmaCoeff[31][13] = 4.94666666667;
            meuCoeff[32][11] = 71.0;
            sigmaCoeff[32][11] = 6.71;
            meuCoeff[32][12] = 64.0;
            sigmaCoeff[32][12] = 14.6133333333;
            meuCoeff[32][13] = 68.0;
            sigmaCoeff[32][13] = 9.98333333333;
            meuCoeff[34][14] = 68.0;
            sigmaCoeff[34][14] = 8.46;
            meuCoeff[34][15] = 68.0;
            sigmaCoeff[34][15] = 5.93;
            meuCoeff[35][14] = 66.0;
            sigmaCoeff[35][14] = 13.3866666667;
            meuCoeff[35][15] = 60.0;
            sigmaCoeff[35][15] = 9.66666666667;
            meuCoeff[36][15] = 80.0;
            sigmaCoeff[36][15] = 2.82666666667;
            meuCoeff[36][16] = 77.0;
            sigmaCoeff[36][16] = 1.79666666667;
            meuCoeff[37][15] = 61.0;
            sigmaCoeff[37][15] = 13.8933333333;
            meuCoeff[38][15] = 61.0;
            sigmaCoeff[38][15] = 18.9266666667;
            meuCoeff[39][16] = 86.0;
            sigmaCoeff[39][16] = 2.18333333333;
            meuCoeff[40][16] = 71.0;
            sigmaCoeff[40][16] = 4.06;
            meuCoeff[41][16] = 71.0;
            sigmaCoeff[41][16] = 3.88;
            meuCoeff[42][16] = 71.0;
            sigmaCoeff[42][16] = 4.89333333333;
            meuCoeff[42][17] = 74.0;
            sigmaCoeff[42][17] = 21.28;
            meuCoeff[42][18] = 67.0;
            sigmaCoeff[42][18] = 3.66666666667;
            meuCoeff[43][18] = 80.0;
            sigmaCoeff[43][18] = 18.1866666667;

        }
    }
    public static int getMACintId(String asMAC){
        if( asMAC.compareTo( "5c:96:9d:65:76:8e") == 0 ) return 0;
        else if( asMAC.compareTo( "5c:96:9d:65:76:8d") == 0 ) return 1;
        else if( asMAC.compareTo( "1c:aa:07:7b:39:10") == 0 ) return 2;
        else if( asMAC.compareTo( "1c:aa:07:b0:7a:bd") == 0 ) return 3;
        else if( asMAC.compareTo( "f8:e9:03:ca:c9:a4") == 0 ) return 4;
        else if( asMAC.compareTo( "1c:aa:07:6e:31:ae") == 0 ) return 5;
        else if( asMAC.compareTo( "84:16:f9:c8:73:3a") == 0 ) return 6;
        else if( asMAC.compareTo( "1c:aa:07:7b:39:12") == 0 ) return 7;
        else if( asMAC.compareTo( "e0:3f:49:09:d9:9c") == 0 ) return 8;
        else if( asMAC.compareTo( "08:57:00:5b:1c:bc") == 0 ) return 9;
        else if( asMAC.compareTo( "1c:aa:07:6e:31:a0") == 0 ) return 10;
        else if( asMAC.compareTo( "1c:aa:07:7b:39:1f") == 0 ) return 11;
        else if( asMAC.compareTo( "1c:aa:07:7b:39:11") == 0 ) return 12;
        else if( asMAC.compareTo( "1c:aa:07:b0:7a:b0") == 0 ) return 13;
        else if( asMAC.compareTo( "e0:3f:49:09:d9:98") == 0 ) return 14;
        else if( asMAC.compareTo( "e0:91:f5:f4:c6:60") == 0 ) return 15;
        else if( asMAC.compareTo( "20:c9:d0:18:43:f3") == 0 ) return 16;
        else if( asMAC.compareTo( "1c:aa:07:b0:7c:00") == 0 ) return 17;
        else if( asMAC.compareTo( "1c:aa:07:b0:7a:bf") == 0 ) return 18;
        else if( asMAC.compareTo( "1c:aa:07:b0:7c:0d") == 0 ) return 19;
        else if( asMAC.compareTo( "20:c9:d0:18:43:f4") == 0 ) return 20;
        else if( asMAC.compareTo( "1c:aa:07:6e:31:ad") == 0 ) return 21;
        else if( asMAC.compareTo( "1c:aa:07:b0:7c:0f") == 0 ) return 22;
        else if( asMAC.compareTo( "1c:aa:07:b0:80:ce") == 0 ) return 23;
        else if( asMAC.compareTo( "1c:aa:07:b0:7c:02") == 0 ) return 24;
        else if( asMAC.compareTo( "1c:aa:07:b0:74:cd") == 0 ) return 25;
        else if( asMAC.compareTo( "1c:aa:07:b0:74:ce") == 0 ) return 26;
        else if( asMAC.compareTo( "1c:aa:07:b0:74:cf") == 0 ) return 27;
        else if( asMAC.compareTo( "1c:aa:07:b0:74:c0") == 0 ) return 28;
        else if( asMAC.compareTo( "64:d1:a3:3c:78:a6") == 0 ) return 29;
        else if( asMAC.compareTo( "1c:aa:07:6f:28:5d") == 0 ) return 30;
        else if( asMAC.compareTo( "1c:aa:07:6f:28:5e") == 0 ) return 31;
        else if( asMAC.compareTo( "1c:aa:07:6f:28:5f") == 0 ) return 32;
        else if( asMAC.compareTo( "1c:aa:07:6f:28:51") == 0 ) return 33;
        else if( asMAC.compareTo( "1c:aa:07:7b:28:00") == 0 ) return 34;
        else if( asMAC.compareTo( "1c:aa:07:b0:7d:50") == 0 ) return 35;
        else if( asMAC.compareTo( "6c:70:9f:eb:63:a0") == 0 ) return 36;
        else if( asMAC.compareTo( "1c:aa:07:b0:7d:52") == 0 ) return 37;
        else if( asMAC.compareTo( "1c:aa:07:b0:7d:51") == 0 ) return 38;
        else if( asMAC.compareTo( "6c:70:9f:eb:63:a1") == 0 ) return 39;
        else if( asMAC.compareTo( "1c:aa:07:7b:28:0e") == 0 ) return 40;
        else if( asMAC.compareTo( "1c:aa:07:7b:28:0d") == 0 ) return 41;
        else if( asMAC.compareTo( "1c:aa:07:7b:28:0f") == 0 ) return 42;
        else if( asMAC.compareTo( "1c:aa:07:7b:37:df") == 0 ) return 43;
        else return -1;
    }
    public void initBelief(){
        for (int i = 0; i < 20 ; i ++ ){
            belief[i] = 0.05d;
        }
    }
    public static void resetFileData(){
        for ( int i = 0 ; i < 10; i ++){
            for ( int j = 0 ; j < 44; j ++){
                fileData[j][i] = 0.00d;
            }
        }
        ReadFile r = new ReadFile();
        r.openFile(Environment.getExternalStorageDirectory()+"/RSSData/RSSData_cell_no"+cell+"time_1496415572135.txt");
        try{
            while(r.readLine().compareTo("EOF")!= 0){
                fileData[getMACintId(r.getLine().split("\\|")[1])][Integer.parseInt(r.getLine().substring(14,15))]
                        = Double.parseDouble(r.getLine().substring(15,18));
            }
        }
        catch(Exception e){

        }
    }
    public static String returnMaxIndex(String x){
        int index = Integer.parseInt(x.split(":")[0]);
        double max = Double.parseDouble(x.split(":")[0]);
        int cur_index = -1;
        for (int i =  0 ; i < 44 ; i ++){
            if((index != i && max < fileData[i][5] && fileData[i][5] <0) ||(index != i && i > cur_index  && fileData[i][5] <0 && max == fileData[i][5])  ){
                max = fileData[i][5];
                cur_index = i;
            }
        }
        return cur_index+":"+max;
    }

    public static void updateBelief() {

        String index_stat = "-999:-999";
        // fileData;
        for (int iterx = 0; iterx < 44 ; iterx++) {
            String xyz = iterx + ":";
            for (int cellIter = 0 ; cellIter <20 ; cellIter ++){
                xyz = returnMaxIndex(xyz);
                if (xyz.split(":")[0].compareTo("-1")!=0) {
                    belief[cellIter] = belief[cellIter] * pdf(xyz, Double.parseDouble(xyz.split(":")[1]), cellIter);
                }
            }
        }
        normalize();
        printBelief();
    }
    public static void normalize(){
        double fact = 0.0;
        for(int iter = 0 ; iter < 20 ; iter++){
            fact+= belief[iter];
        }
        for(int iter = 0 ; iter < 20 ; iter++){
            belief[iter]/= fact;
        }
    }
    public static double pdf(String index_stat, double rss, int index){
        double retval  =0.0;
        if(sigmaCoeff[Integer.parseInt(index_stat.split(":")[0])][index] == 0 || meuCoeff[Integer.parseInt(index_stat.split(":")[0])][index] == 0){
            return 0;
        }
        else {
            retval = (1 / (sigmaCoeff[Integer.parseInt(index_stat.split(":")[0])][index] * Math.sqrt(2 * 3.1415926535))) *
                    Math.exp(Math.pow(0 - meuCoeff[Integer.parseInt(index_stat.split(":")[0])][index], 2) / (2 * Math.pow(sigmaCoeff[Integer.parseInt(index_stat.split(":")[0])][index], 2)));
        }
        return retval;

    }
    public static  void printBelief( ){
        try{

            f.openFile(Environment.getExternalStorageDirectory()+"/RSSData/log"+System.currentTimeMillis()+".txt");

            //fout = new PrintWriter( logFile);
            //fout.println("Hello Testing");
            //fout.flush();
        }catch(Exception e){
            e.printStackTrace();
        }
         for(int iter = 0 ; iter <20 ; iter++){
             System.out.print(belief[iter]+"& ");
         }
    }


}