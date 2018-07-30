package de.gagapi.androidapp;

import android.content.*;
import android.hardware.*;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;

public class MainActivity extends AppCompatActivity {

    String FloatFormatString = "%.4f";
    private SensorManager mSensorManager;
    private Sensor sensorGyro, sensorAcceleration, sensorGravity;
    private AdvancedSensorEventListener gyroListener, accelerationListener, gravityListener;

    Handler handler;
    GraphView graphAcc, graphGrav, graphGyro;

    TextView gravTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        graphAcc = SetupGraphView((GraphView) findViewById(R.id.graphAcc));
        graphGrav = SetupGraphView((GraphView) findViewById(R.id.graphGrav));
      //  graphGrav.getViewport().setMinY(-10);
     //   graphGrav.getViewport().setMaxY(10);
        graphGyro = SetupGraphView((GraphView) findViewById(R.id.graphGyro));

        gravTextView = (TextView)  findViewById(R.id.gravOut);
        InitalizeSensorListeners();

        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {

                gravTextView.setText(gravityListener.getSensorSampleProcessor().valueMean.toString());
                handler.postDelayed(this, 3000);
                SendDataToServer(GetHTTPRequestData());

            }
        };

        handler.postDelayed(r, 1);
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
        graph.getViewport().setMaxX(5);
 //       graph.getViewport().setYAxisBoundsManual(true);
  //      graph.getViewport().setMinY(-1);
   //     graph.getViewport().setMaxY(1);
        graph.getLegendRenderer().setVisible(true);

        return graph;
    }

    void SendDataToServer(String Data)
    {
        final TextView mTextView = (TextView) findViewById(R.id.debugRequestResponse);
// ...

// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://141.22.94.47:8080/?volume=" + Data; // + ID

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
                mTextView.setText(error.getMessage());
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

        gyroListener = new AdvancedSensorEventListener("", graphGyro, null, null, null);

        LowPassFilter LPFx = new LowPassFilter(), LPFy = new  LowPassFilter(), LPFz = new LowPassFilter();
        accelerationListener =  new AdvancedSensorEventListener("", graphAcc, LPFx, LPFy, LPFz);

        //TODO: filter for gravity
        gravityListener =  new AdvancedSensorEventListener("", graphGrav, null, null, null);
    }

    void RegisterSensorListeners()
    {
        mSensorManager.registerListener(gyroListener, sensorGyro, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(accelerationListener, sensorAcceleration, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(gravityListener, sensorGravity, SensorManager.SENSOR_DELAY_FASTEST);
    }

    String GetHTTPRequestData()
    {
        StringBuilder sb = new StringBuilder();

        AdvancedSensorEventListener.SensorSampleProcessor acc = accelerationListener.getSensorSampleProcessor();
        AdvancedSensorEventListener.SensorSampleProcessor gyro = accelerationListener.getSensorSampleProcessor();
        AdvancedSensorEventListener.SensorSampleProcessor grav = accelerationListener.getSensorSampleProcessor();
        sb.append(acc.valueMean.x).append(","); // tbodyaccmeanx
        sb.append(acc.valueMean.y).append(","); // tbodyaccmeany
        sb.append(acc.valueMean.z).append(","); // tbodyaccmeanz

        sb.append(grav.valueMean.x).append(","); // tgravityaccmeanx
        sb.append(grav.valueMean.y).append(","); // tgravityaccmeany
        sb.append(grav.valueMean.z).append(","); // tgravityaccmeanz

        sb.append(grav.jerkMean.x).append(","); // tgravityaccjerkmeanx
        sb.append(grav.jerkMean.y).append(","); // tgravityaccjerkmeany
        sb.append(grav.jerkMean.z).append(","); // tgravityaccjerkmeanz

        sb.append(gyro.valueMean.x).append(","); // tbodygyromeanx
        sb.append(gyro.valueMean.y).append(","); // tbodygyromeany
        sb.append(gyro.valueMean.z).append(","); // tbodygyromeanz

        sb.append(gyro.jerkMean.x).append(","); // tbodygyrojerkmeanx
        sb.append(gyro.jerkMean.y).append(","); // tbodygyrojerkmeany
        sb.append(gyro.jerkMean.z).append(","); // tbodygyrojerkmeanz

        sb.append(acc.valueMean.length()).append(","); // tbodyaccmagmean
        sb.append(grav.valueMean.length()).append(","); // tgravityaccmagmean
        sb.append(acc.jerkMean.length()).append(","); // tbodyaccjerkmagmean

        sb.append(gyro.valueMean.length()).append(","); // tbodygyromagmean
        sb.append(gyro.valueMean.length()).append(","); // tbodygyrojerkmagmean

        return sb.toString();
        //fbodyaccmeanx
        //fbodyaccmeany
        //fbodyaccmeanz

        //fbodyaccjerkmeanx
        //fbodyaccjerkmeany
        //fbodyaccjerkmeanz

        //fbodygyromeanx
        //fbodygyromeany
        //fbodygyromeanz


    }

    @Override
    protected void onResume() {
        super.onResume();
        RegisterSensorListeners();
    }
}
