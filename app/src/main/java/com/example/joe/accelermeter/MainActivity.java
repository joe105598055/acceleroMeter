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
import android.widget.TextView;

//public class MainActivity extends AppCompatActivity implements AccelerMeterCallback.iSensorCallback {
public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    TextView x_val;
    TextView y_val;
    TextView z_val;
    TextView isMoving;
    TextView mAccelText;
//    Queue<String> queue = new LinkedList<String>();
//    private AccelerMeterCallback _accelerMeterCallback = null;
    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent = SensorManager.GRAVITY_EARTH;
    private float mAccelLast = SensorManager.GRAVITY_EARTH;
    TextView tv;
    private long perScannedTime = 0;
    private int stopCount = 0;
    private int MOTION_THRESHOLD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv=(TextView)findViewById(R.id.txtview);
        x_val = (TextView) findViewById(R.id.x_val);
        y_val = (TextView) findViewById(R.id.y_val);
        z_val = (TextView) findViewById(R.id.z_val);
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

//            x_val.setText(String.valueOf(x));
//            y_val.setText(String.valueOf(y));
//            z_val.setText(String.valueOf(z));

            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            // Make this higher or lower according to how much
            // motion you want to detect
            mAccelText.setText("9.8 : " +  mAccel);
            if(mAccel > 0.6){
                // do something
//                System.out.println("[mAccel] = " + mAccel);
                stopCount = 0;
            }else{
                stopCount++;
            }
//            System.out.println("[MOTION_THRESHOLD]" + MOTION_THRESHOLD);
            if(stopCount >= MOTION_THRESHOLD){
                isMoving.setText("stop");
            }else{
                isMoving.setText("moving");

            }


        }

    }

    @UiThread
    private void updateThreshold(long delta){

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

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



}