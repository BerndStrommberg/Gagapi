package de.gagapi.androidapp;

import android.content.*;
import android.hardware.*;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    String FloatFormatString = "%.4f";
    private SensorManager mSensorManager;
    private Sensor sensorGyro, sensorAcceleration, sensorGravity;
    private SensorEventListener gyroListener, accelerationListener, gravityListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitalizeSensorListeners();
        
        // outline:
        // if 2.5 sec elapsed
        //      send data with http GET and ID
        //      read response
        //      use response as new ID
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



    static class float3 implements Comparable<float3>
    {
        public float3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float3 add(float3 rhs)
        {
            this.x += rhs.x;
            this.y += rhs.y;
            this.z += rhs.z;
            return this;
        }

        public float3 sub(float3 rhs)
        {
            this.x -= rhs.x;
            this.y -= rhs.y;
            this.z -= rhs.z;
            return this;
        }

        public float3 mul(float3 rhs)
        {
            this.x *= rhs.x;
            this.y *= rhs.y;
            this.z *= rhs.z;
            return this;
        }


        public float3 mul(float rhs)
        {
            this.x *= rhs;
            this.y *= rhs;
            this.z *= rhs;
            return this;
        }

        public float length()
        {
            return (float) Math.sqrt(x * x + y * y + z * z);
        }

        @Override
        public String toString() {
            return String.format("x: %+.5f, y: %+.5f, z: %+.5f", x, y, z);
        }

        public float x, y, z;

        @Override
        public int compareTo(@NonNull float3 o) {
            float thisLength = this.length();
            float otherLength = o.length();

            if (thisLength < otherLength) return -1;
            if (thisLength == otherLength) return 0;
           /* if (thisLength > otherLength)*/ return 1;
        }
    }

    static class MathyStuffFromListyList
    {
        public float3 medianValue;
        public float medianMag;

        public float3 medianJerk;
        public MathyStuffFromListyList(ArrayList<float3> list)
        {
            int arrayLength = list.size();

            float3[] asArray = new float3[arrayLength];
            list.toArray(asArray);

            //TODO: smooth out the values with a butterworth filter (http://www-users.cs.york.ac.uk/~fisher/cgi-bin/mkfscript)
            Arrays.sort(asArray);


            int middle =  arrayLength / 2;
            if (asArray.length%2 == 1) {
                medianValue = asArray[middle];
            } else {
                medianValue = (asArray[middle-1].add(asArray[middle])).mul(0.5f);
            }

            medianMag = medianValue.length();
        }
    }


    class AdvancedSensorEventListener implements SensorEventListener
    {
        public AdvancedSensorEventListener(TextView debugRawValue, TextView debugAverageValue, TextView debugAverageMag, TextView debugAverageJerk) {
            this.debugRawValue = debugRawValue;
            this.debugAverageValue = debugAverageValue;
            this.debugAverageMag = debugAverageMag;
            this.debugAverageJerk = debugAverageJerk;
        }

        TextView debugRawValue, debugAverageValue, debugAverageMag, debugAverageJerk;

        ArrayList<float3> accumulatedValues = new ArrayList<>();
        ArrayList<float3> accumulatedJerk = new ArrayList<>();
        final float SENDING_RATE_SECONDS = 2.5f;

        float3 previousValue = new float3(0,0,0);
        long tPrevious = -1;


        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float3 value = new float3(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);

            //TODO: use actuall average Jerkness
            float3 jerkness = previousValue.sub(value);
            debugAverageJerk.setText("jerk: " + jerkness.toString());

            accumulatedJerk.add(jerkness);
            previousValue = value;
            // store rotation inside list
            accumulatedValues.add(value);

            long tCurrent = SystemClock.elapsedRealtime();
            long tDelta = tCurrent - tPrevious;
            float elapsedSeconds = tDelta / 1000.0f;

            if(elapsedSeconds > SENDING_RATE_SECONDS)
            {
                tPrevious = tCurrent;
                MathyStuffFromListyList mathstuff = new MathyStuffFromListyList(accumulatedValues);
                debugAverageValue.setText("median: " + mathstuff.medianValue.toString());
                debugAverageMag.setText("medianMag: " + Float.toString(mathstuff.medianMag));
                accumulatedValues.clear();
            }

            //  debug output
            debugRawValue.setText("value: " + value.toString());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
    void InitalizeSensorListeners()
    {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        gyroListener = new AdvancedSensorEventListener(
                (TextView) findViewById(R.id.rot_value),
                (TextView) findViewById(R.id.rot_avg),
                (TextView) findViewById(R.id.rot_avgMag),
                (TextView) findViewById(R.id.rot_jerk));

        accelerationListener =  new AdvancedSensorEventListener(
                (TextView) findViewById(R.id.acl_value),
                (TextView) findViewById(R.id.acl_avg),
                (TextView) findViewById(R.id.acl_avgMag),
                (TextView) findViewById(R.id.acl_jerk));

        gravityListener =  new AdvancedSensorEventListener(
                (TextView) findViewById(R.id.grav_value),
                (TextView) findViewById(R.id.grav_avg),
                (TextView) findViewById(R.id.grav_avgMag),
                (TextView) findViewById(R.id.grav_jerk));
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
