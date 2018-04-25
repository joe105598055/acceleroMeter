package com.example.joe.accelermeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.LinkedList;
import java.util.Queue;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by joe on 2018/4/17.
 */

public class AccelerMeterCallback implements SensorEventListener {
    private SensorManager sensorManager;
    private iSensorCallback sensorCallback;
    double ax,ay,az;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private Queue<String> queue = new LinkedList<String>();
    private long lastScannedTime = 0;



    public AccelerMeterCallback(Context ctx,iSensorCallback sensorCallback){
        this.sensorCallback = sensorCallback;
        this.sensorManager = (SensorManager) ctx.getSystemService(SENSOR_SERVICE);

        this.initAccelerMeterSensor();
    }

    public interface iSensorCallback{
        void getActive(String active);
    }

    public void initAccelerMeterSensor(){
        sensorManager.registerListener((SensorEventListener) this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            DecimalFormat df = new DecimalFormat("##.00");
            ax = Double.parseDouble(df.format(sensorEvent.values[0]));
            ay = Double.parseDouble(df.format(sensorEvent.values[1]));
            az = Double.parseDouble(df.format(sensorEvent.values[2]));
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float)Math.sqrt(ax*ax+ay*ay+az*az);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            if(mAccel > 0.4){
                queue.offer("moving");
            }else{
                queue.offer("stop");
            }
            if(queue.size() == 15){
                queue.poll();
                if(queue.contains("moving")){
                    sensorCallback.getActive("moving");
//                System.out.println("MOVING*****");
                }else{
                    sensorCallback.getActive("stop");
                }
            }
            if(!canReturnCallback()){
                return;
            }else{

            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private boolean canReturnCallback() {
        long currentScannedTime = System.currentTimeMillis();
        if (lastScannedTime == 0) {
            lastScannedTime = currentScannedTime;
            return false;
        }

        if (currentScannedTime - lastScannedTime > 200) {
            lastScannedTime = currentScannedTime;
            return true;
        } else {
            return false;
        }
    }

}
