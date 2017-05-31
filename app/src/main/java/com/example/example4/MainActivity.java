package com.example.example4;

import java.io.File;
import java.io.FileOutputStream;
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

/**
 * Smart Phone Sensing Example 4 - 2017. Wifi received signal strength.
 */
public class MainActivity extends Activity implements OnClickListener, SensorEventListener{

    /**
     * The wifi manager.
     */
    private WifiManager wifiManager;
    private SensorManager sensorManager;
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

            try{


                //fout = new PrintWriter( logFile);
                //fout.println("Hello Testing");
                //fout.flush();
            }catch(Exception e){
                e.printStackTrace();
            }


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
        for(int i = 0 ; i < 300; i++){
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
                Thread.sleep(100);
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
}