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

    TextView serverIP;
    TextView gravTextView;
    SharedPreferences userPref;
    SharedPreferences.Editor userPrefEditor;
    final String SERVER_IP_PREF = "serverIP";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userPref = getSharedPreferences("pref", 0);
        userPrefEditor = userPref.edit();

        serverIP = (TextView) findViewById(R.id.serverAdress);
        serverIP.setText(userPref.getString(SERVER_IP_PREF, ""));


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
                handler.postDelayed(this, 1250);

                userPrefEditor.putString(SERVER_IP_PREF, serverIP.getText().toString());
                userPrefEditor.commit();
                SendDataToServer(serverIP.getText().toString(), GetHTTPRequestData());

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

    void SendDataToServer(String Adress, String Data)
    {
        final TextView mTextView = (TextView) findViewById(R.id.debugRequestResponse);
// ...

// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://" + Adress + "/?action=evaluate&data=" + Data; // + ID

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

    final int SampleRate = 20000; //in microseconds
    void RegisterSensorListeners()
    {
        mSensorManager.registerListener(gyroListener, sensorGyro, SampleRate);
        mSensorManager.registerListener(accelerationListener, sensorAcceleration, SampleRate);
        mSensorManager.registerListener(gravityListener, sensorGravity, SampleRate);
    }

    class RequestBuilder
    {
        StringBuilder sb = new StringBuilder();
        public RequestBuilder(){}

        public void AppendValue(float value, float minRange, float maxRange)
        {
            float normalizedValue = (value - minRange) / (maxRange - minRange) * (1-(-1)) + (-1);
            sb.append(value).append(",");
        }

        public void AppendValueFinal(float value, float minRange, float maxRange)
        {
            float normalizedValue = (value - minRange) / (maxRange - minRange) * (1-(-1)) + (-1);
            sb.append(value);
        }

        public String GetString()
        {
            return sb.toString();
        }
        public void Clear()
        {
            sb.setLength(0);
        }
    }

    RequestBuilder requestBuilder = new RequestBuilder();
    String GetHTTPRequestData()
    {

        AdvancedSensorEventListener.SensorSampleProcessor body = accelerationListener.getSensorSampleProcessor();
        AdvancedSensorEventListener.SensorSampleProcessor bodyGyro = gyroListener.getSensorSampleProcessor();
        AdvancedSensorEventListener.SensorSampleProcessor grav = gravityListener.getSensorSampleProcessor();
        //tbodyaccmeanx	tbodyaccmeany	tbodyaccmeanz

        requestBuilder.Clear();
        requestBuilder.AppendValue(body.valueMean.x,-1.04f, 10.22f); // tbodyaccmeanx
        requestBuilder.AppendValue(body.valueMean.y,-1.72f, 4.62f);; // tbodyaccmeany
        requestBuilder.AppendValue(body.valueMean.z,-2.52f, 9.64f);; // tbodyaccmeanz

        requestBuilder.AppendValue(grav.valueMean.x,-0.96f, 9.76f); // tgravityaccmeanx
        requestBuilder.AppendValue(grav.valueMean.y,-1.88f, 4.86f); // tgravityaccmeany
        requestBuilder.AppendValue(grav.valueMean.z,-2.38f, 9.63f); // tgravityaccmeanz

        requestBuilder.AppendValue(body.jerkMean.x,-4.91f, 0.11f  ); // tbodyaccjerkmeanx
        requestBuilder.AppendValue(body.jerkMean.y,-0.06f, 0.09f  ); // tbodyaccjerkmeanx
        requestBuilder.AppendValue(body.jerkMean.z,-0.13f, 0.60f  ); // tbodyaccjerkmeanx

        requestBuilder.AppendValue(bodyGyro.valueMean.x, -1.22f, 1.57f); // tbodygyromeanx
        requestBuilder.AppendValue(bodyGyro.valueMean.y, -0.31f, 0.81f); // tbodygyromeany
        requestBuilder.AppendValue(bodyGyro.valueMean.z, -0.5f, 0.37f); // tbodygyromeanz

        requestBuilder.AppendValue(bodyGyro.jerkMean.x, -0.24f, 0.02f); // tbodygyrojerkmeanx
        requestBuilder.AppendValue(bodyGyro.jerkMean.y, -0.01f, 0.11f); // tbodygyrojerkmeany
        requestBuilder.AppendValue(bodyGyro.jerkMean.z, -0.02f, 0.03f); // tbodygyrojerkmeanz

        requestBuilder.AppendValue(body.valueMean.length(), 4.95f, 10.27f); // tbodyaccmagmean
        requestBuilder.AppendValue(grav.valueMean.length(), 6.85f, 9.81f); // tgravityaccmagmean
        requestBuilder.AppendValue(body.jerkMean.length(), 0f, 4.95f); // tbodyaccjerkmagmean

        requestBuilder.AppendValue(bodyGyro.valueMean.length(), 0f, 1.78f); // tbodygyromagmean
        requestBuilder.AppendValueFinal(bodyGyro.jerkMean.length(), 0f, 0.26f); // tbodygyrojerkmagmean

        return requestBuilder.GetString();
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
