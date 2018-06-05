package de.gagapi.androidapp;

import android.content.*;
import android.hardware.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    String FloatFormatString = "%.4f";
    private SensorManager mSensorManager;
    private Sensor sensorRotation, sensorAcceleration, sensorGravity;
    private SensorEventListener rotationListener, accelerationListener, gravityListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitalizeSensorListeners();
    }

    void InitalizeSensorListeners()
    {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        rotationListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                ((TextView) findViewById(R.id.rot_x)).setText("x: " + String.format(FloatFormatString, sensorEvent.values[0]));
                ((TextView) findViewById(R.id.rot_y)).setText("y: " + String.format(FloatFormatString, sensorEvent.values[1]));
                ((TextView) findViewById(R.id.rot_z)).setText("z: " + String.format(FloatFormatString, sensorEvent.values[2]));
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        accelerationListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                ((TextView) findViewById(R.id.acl_x)).setText("x: " + String.format(FloatFormatString, sensorEvent.values[0]));
                ((TextView) findViewById(R.id.acl_y)).setText("y: " + String.format(FloatFormatString, sensorEvent.values[1]));
                ((TextView) findViewById(R.id.acl_z)).setText("z: " + String.format(FloatFormatString, sensorEvent.values[2]));
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        gravityListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                ((TextView) findViewById(R.id.grav_x)).setText("x: " + String.format(FloatFormatString, sensorEvent.values[0]));
                ((TextView) findViewById(R.id.grav_y)).setText("y: " + String.format(FloatFormatString, sensorEvent.values[1]));
                ((TextView) findViewById(R.id.grav_z)).setText("z: " + String.format(FloatFormatString, sensorEvent.values[2]));
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    void RegisterSensorListeners()
    {
        mSensorManager.registerListener(rotationListener, sensorRotation, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(accelerationListener, sensorAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(gravityListener, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onResume() {
        super.onResume();
        RegisterSensorListeners();
    }
}
