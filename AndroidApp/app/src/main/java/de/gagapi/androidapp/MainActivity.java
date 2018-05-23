package de.gagapi.androidapp;

import android.content.*;
import android.hardware.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener sensorEventListener;

    private StringBuilder stringBuilder = new StringBuilder();
    private TextView output;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        output = (TextView) findViewById(R.id.output);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                stringBuilder.delete(0, stringBuilder.length());

                stringBuilder.append("x: ");
                stringBuilder.append(String.format("%.4f", sensorEvent.values[0]));
                stringBuilder.append("\n");

                stringBuilder.append("y: ");
                stringBuilder.append(String.format("%.4f", sensorEvent.values[1]));
                stringBuilder.append("\n");

                stringBuilder.append("z: ");
                stringBuilder.append(String.format("%.4f", sensorEvent.values[2]));
                stringBuilder.append("\n");

                output.setText(stringBuilder.toString());


            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };


        output.setText("Ok");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(sensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
