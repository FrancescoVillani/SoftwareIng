package com.example.softwareing;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationListener implements SensorEventListener {

    private SensorManager sm;
    private Sensor sm_sensor;
    private final float[] sm_rotation_matrix = new float[16];
    private float[] sm_array = new float[3];
    private double rotation;

    public OrientationListener (SensorManager sm){
        this.sm = sm;
        sm_sensor = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        sm.registerListener(this, sm_sensor, 10000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(sm_rotation_matrix, event.values);
        SensorManager.getOrientation(sm_rotation_matrix, sm_array);

        rotation = (Math.toDegrees(sm_array[0])+360) % 360;
    }

    public double getRotation(){
        return this.rotation;
    }

    /* non serve implementarlo */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}