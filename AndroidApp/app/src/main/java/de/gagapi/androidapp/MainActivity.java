package de.gagapi.androidapp;

import android.content.*;
import android.hardware.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity {

    String FloatFormatString = "%.4f";
    private SensorManager mSensorManager;
    private Sensor sensorGyro, sensorAcceleration, sensorGravity;
    private SensorEventListener gyroListener, accelerationListener, gravityListener;

    GraphView graphAcc, graphGrav, graphGyro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        graphAcc = SetupGraphView((GraphView) findViewById(R.id.graphAcc));
        graphGrav = SetupGraphView((GraphView) findViewById(R.id.graphGrav));
        graphGyro = SetupGraphView((GraphView) findViewById(R.id.graphGyro));
        InitalizeSensorListeners();
        // outline:
        // if 2.5 sec elapsed
        //      send data with http GET and ID
        //      read response
        //      use response as new ID
    }

    static GraphView SetupGraphView(GraphView graph)
    {
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxY(1);
        graph.getLegendRenderer().setVisible(true);
        return graph;
    }
    void DemoRequest()
    {
        final TextView mTextView = (TextView) findViewById(R.id.debugRequestResponse);
// ...

// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://www.google.com"; // + ID

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        mTextView.setText("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    //    tBodyAcc-XYZ
    //    tBodyAccJerk-XYZ
    //    tBodyAccMag
    //    tBodyAccJerkMag

    //    tGravityAcc-XYZ
    //    tGravityAccMag

    //    tBodyGyro-XYZ
    //    tBodyGyroJerk-XYZ
    //    tBodyGyroMag
    //    tBodyGyroJerkMag

    // FFT processed values
    //    fBodyAcc-XYZ
    //    fBodyAccJerk-XYZ
    //    fBodyGyro-XYZ
    //    fBodyAccMag
    //    fBodyAccJerkMag
    //    fBodyGyroMag
    //    fBodyGyroJerkMag





    void InitalizeSensorListeners()
    {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        //TODO: filter for gravity
        gyroListener = new AdvancedSensorEventListener("", graphGyro, null, null, null);

        LowPassFilter LPFx = new LowPassFilter(), LPFy = new  LowPassFilter(), LPFz = new LowPassFilter();
        accelerationListener =  new AdvancedSensorEventListener("", graphAcc, LPFx, LPFy, LPFz);

        gravityListener =  new AdvancedSensorEventListener("", graphGrav, null, null, null);
    }

    void RegisterSensorListeners()
    {
        mSensorManager.registerListener(gyroListener, sensorGyro, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(accelerationListener, sensorAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(gravityListener, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RegisterSensorListeners();
    }
}
