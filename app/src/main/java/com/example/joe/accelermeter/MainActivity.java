package com.example.joe.accelermeter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.LinkedList;
import java.util.Queue;

//public class MainActivity extends AppCompatActivity implements AccelerMeterCallback.iSensorCallback {
@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;

    @ViewById TextView x_val;
    @ViewById TextView y_val;
    @ViewById TextView z_val;
    @ViewById TextView gravity;
    @ViewById Button storeData;

    @ViewById TextView mAccelText;
    @ViewById TextView isMoving;
    Queue<Float> accelQueue = new LinkedList<>();
    Queue<String> deltaQueue = new LinkedList<>();
    Queue<String> activeQueue = new LinkedList<>();
    Queue<Integer> thresholdQueue = new LinkedList<>();
    Queue<Float> mAccelCurrentQueue = new LinkedList<>();


    //    private AccelerMeterCallback _accelerMeterCallback = null;
    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent = SensorManager.GRAVITY_EARTH;
    private float mAccelLast = SensorManager.GRAVITY_EARTH;
    private long perScannedTime = 0;
    private int stopCount = 0;
    private int MOTION_THRESHOLD;
    private final String TAG = "MainActivity";
    private long startTime = 0;
    private float gap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        x_val = (TextView) findViewById(R.id.x_val);
//        y_val = (TextView) findViewById(R.id.y_val);
//        z_val = (TextView) findViewById(R.id.z_val);
        isMoving = (TextView) findViewById(R.id.isMoving);
        mAccelText = (TextView) findViewById(R.id.mAccelText);

//        if(_accelerMeterCallback == null){
//            _accelerMeterCallback = new AccelerMeterCallback(this,this);
//        }
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        System.out.println("[Sensor.TYPE_ACCELEROMETER]" + Sensor.TYPE_ACCELEROMETER);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override   @UiThread
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType()== Sensor.TYPE_ACCELEROMETER){

            if(perScannedTime == 0){
                startTime = System.currentTimeMillis();
                perScannedTime = sensorEvent.timestamp;
            }
            if(perScannedTime !=  sensorEvent.timestamp){
                System.out.println("[delta] =" + (sensorEvent.timestamp - perScannedTime)/1000000L);
                updateThreshold((sensorEvent.timestamp - perScannedTime)/1000000L);
                perScannedTime = sensorEvent.timestamp;
            }

            mGravity = sensorEvent.values.clone();

            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];

            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
//            float delta = mAccelCurrent - mAccelLast;
            float delta = mAccelCurrent - 9.8f;

//            mAccel = mAccel * 0.9f + delta * 0.1f;
            mAccel = delta;
            // Make this higher or lower according to how much
            // motion you want to detect
            x_val.setText(String.valueOf(x));
            y_val.setText(String.valueOf(y));
            z_val.setText(String.valueOf(z));
            gravity.setText(String.valueOf(mAccelCurrent));
            mAccelText.setText("mAccel = " +  mAccel);
            mAccelCurrentQueue.offer(mAccelCurrent);
            accelQueue.offer(mAccel);
            thresholdQueue.offer(MOTION_THRESHOLD);

            if(mAccel> 0.8){
                // do something
                activeQueue.offer("Moving");
                stopCount = 0;
            }else{
                activeQueue.offer("Stop");
                stopCount++;
            }
            if(stopCount >= MOTION_THRESHOLD){
                isMoving.setText("stop");
            }else{
                isMoving.setText("moving");
            }
        }

    }

    @UiThread
    private void updateThreshold(long delta){

        deltaQueue.offer(Long.toString(delta));

        if(10 <= delta && delta <= 29){ //20
            MOTION_THRESHOLD = 45;
        }else if(30 <= delta && delta <= 49){ //40
            MOTION_THRESHOLD = 22;
        }else if(50 <= delta && delta <= 69){ //60
            MOTION_THRESHOLD = 15;
        }else if(70 <= delta && delta <= 89){ //80
            MOTION_THRESHOLD = 11;
        }else if(89 <= delta && delta <= 200){
            MOTION_THRESHOLD = 8;
        }else{
            MOTION_THRESHOLD = 7;
        }
        MOTION_THRESHOLD = (int) (1600 / delta);
        if(MOTION_THRESHOLD <= 80 ){
            gap = 0.8f;
        }if(MOTION_THRESHOLD >= 80 ){
            gap = 1.0f;
        }
        System.out.println("[MOTION_THRESHOLD] = " + MOTION_THRESHOLD);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Click(R.id.storeData)
    void storeResult() {

        doSaveResult();
        sensorManager.unregisterListener(this);

    }

    @Background
    void doSaveResult() {


        ExcelBuilder.initExcel();
        Log.d(TAG, "[accelQueue Size] = " + accelQueue.size());
        Log.d(TAG, "[delta Size] = " + deltaQueue.size());
        Log.d(TAG, "[active Size] = " + activeQueue.size());
        Log.d(TAG, "[accelQueue Size] = " + accelQueue.size());

        while(!accelQueue.isEmpty()) {
            ExcelBuilder.setAccel(accelQueue.poll());
        }

        while(!deltaQueue.isEmpty()){
            ExcelBuilder.setDelta(deltaQueue.poll());
        }

        while(!activeQueue.isEmpty()){
            ExcelBuilder.setActive(activeQueue.poll());
        }

        while(!mAccelCurrentQueue.isEmpty()){
            ExcelBuilder.setmAccelCurrent(mAccelCurrentQueue.poll());
        }
        ExcelBuilder.setDuration(System.currentTimeMillis() - startTime);

        ExcelBuilder.saveExcelFile(this, "acceleroMeter");

    }

}
