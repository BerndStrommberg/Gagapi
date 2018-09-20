package de.gagapi.androidapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class SensorTrackerService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }
    public SensorTrackerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    final String SERVER_IP_PREF = "serverIP";
    Handler handler;
    SharedPreferences userPref;
    SharedPreferences.Editor userPrefEditor;

    private SensorManager mSensorManager;
    private Sensor sensorGyro, sensorAcceleration, sensorGravity;
    private AdvancedSensorEventListener gyroListener, accelerationListener, gravityListener;
    Context contex;
    public int i = 0;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this,"Start Service",Toast.LENGTH_SHORT).show();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MainActivity.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        RemoteViews notificationView = new RemoteViews(this.getPackageName(),R.layout.notification);

        Intent buttonCloseIntent = new Intent(this, NotificationCloseButtonHandler.class);
        buttonCloseIntent.putExtra("action", "close");

        PendingIntent buttonClosePendingIntent = pendingIntent.getBroadcast(this, 0, buttonCloseIntent,0);
        notificationView.setOnClickPendingIntent(R.id.notification_button_close, buttonClosePendingIntent);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Test")
                .setTicker("Test")
                .setContentText("Test")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContent(notificationView)
                .setOngoing(true).build();

        startForeground(101,
                notification);

        contex = this;
        // read settings
        userPref = getSharedPreferences("pref", 0);
        userPrefEditor = userPref.edit();

        InitalizeSensorListeners();
        RegisterSensorListeners();
        handler = new Handler();


        final Runnable r = new Runnable() {
            public void run() {

                //gravTextView.setText(gravityListener.getSensorSampleProcessor().valueMean.toString()); // set raw value from gravity sensor, not really important
                handler.postDelayed(this, 1250); // wait 1250 ms
                String serverIP = userPref.getString(SERVER_IP_PREF, "");
                SendDataToServer(serverIP, GetHTTPRequestData());
             //   Toast.makeText(contex, "Tick " + i, Toast.LENGTH_SHORT).show();
              //  i++;
            }
        };

        handler.postDelayed(r, 1);

        return START_STICKY;
    }



    final int SampleRate = 20000; //in microseconds

    /**
     * Registers the sensor listeners, aka start them
     */
    void RegisterSensorListeners()
    {
        mSensorManager.registerListener(gyroListener, sensorGyro, SampleRate);
        mSensorManager.registerListener(accelerationListener, sensorAcceleration, SampleRate);
        mSensorManager.registerListener(gravityListener, sensorGravity, SampleRate);
    }

    /**
     * Sets up the sensorListeners
     */
    void InitalizeSensorListeners()
    {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        gyroListener = new AdvancedSensorEventListener("", null, null, null, null);

        LowPassFilter LPFx = new LowPassFilter(), LPFy = new  LowPassFilter(), LPFz = new LowPassFilter();
        accelerationListener =  new AdvancedSensorEventListener("", null, LPFx, LPFy, LPFz);

        //TODO: filter for gravity
        gravityListener =  new AdvancedSensorEventListener("", null, null, null, null);
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

    void SendDataToServer(String Adress, String Data)
    {
        //final TextView mTextView = (TextView) findViewById(R.id.debugRequestResponse);
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
                       // mTextView.setText("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

              //  mTextView.setText(error.getMessage());
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    public static class NotificationCloseButtonHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context,"Close Clicked",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(true);
    }
}
