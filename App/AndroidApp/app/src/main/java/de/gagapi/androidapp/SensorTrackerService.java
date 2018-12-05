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
import android.os.PowerManager;
import android.support.v4.media.app.NotificationCompat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

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

    public static SensorTrackerService Instance;
    final String SERVER_IP_PREF = "serverIP";
    Handler handler;
    SharedPreferences userPref;
    SharedPreferences.Editor userPrefEditor;

    private SensorManager mSensorManager;
    private Sensor sensorGyro, sensorAcceleration, sensorGravity;
    private AdvancedSensorEventListener gyroListener, accelerationListener, gravityListener;
    Context contex;
    public int packagesSent = 0;
    boolean isRunning = false;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Instance = this;
        Toast.makeText(this,"Intent: " + intent.getAction(),Toast.LENGTH_SHORT).show();


        Log.d("Service", "" + intent.getAction());
        if(intent != null && intent.getAction() != null)
        {
            switch (intent.getAction())
            {
                case "start":
                    StartService();

                    break;
                case "close":
                    stopForeground(true);
                    stopSelf();
                    isRunning = false;
                    if(wakeLock != null)
                    {
                        wakeLock.release();
                    }
                    break;
            }
        }


        return START_STICKY;
    }
    final int SampleRate = 20000; //in microseconds

    Runnable r;
    PowerManager.WakeLock wakeLock;
    void StartService()
    {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire();

        Intent buttonCloseIntent = new Intent(this, SensorTrackerService.class);
        buttonCloseIntent.setAction("close");

        PendingIntent buttonClosePendingIntent = PendingIntent.getService(this, 0, buttonCloseIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Test")
                .setTicker("Sende Bewegungsdaten...")
                .setContentText("Tippe zum beenden.")
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(buttonClosePendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                //.setContent(notificationView)
                .setOngoing(true)
                //.addAction(android.R.drawable.ic_menu_close_clear_cancel, "stop",
               //         buttonClosePendingIntent)
                .build();

        startForeground(101,  notification);

        contex = this;
        // read settings
        userPref = getSharedPreferences("pref", 0);
        userPrefEditor = userPref.edit();

        InitalizeSensorListeners();
        RegisterSensorListeners();
        handler = new Handler();

        requestQueue = Volley.newRequestQueue(this);
         r = new Runnable() {
            public void run() {

                //gravTextView.setText(gravityListener.getSensorSampleProcessor().valueMean.toString()); // set raw value from gravity sensor, not really important

                String serverIP = userPref.getString(SERVER_IP_PREF, "");
                SendDataToServer(serverIP, GetHTTPRequestData());
                packagesSent++;
                if(MainActivity.instance != null)
                {
                    MainActivity.instance.SetDebugLabel("sent packages: " + packagesSent);
                }

                if(isRunning)
                {
                    handler.postDelayed(this, 1250); // wait 1250 ms
                }
                else
                {
                    packagesSent = 0;
                }


                //   Toast.makeText(contex, "Tick " + i, Toast.LENGTH_SHORT).show();
                //  i++;
            }
        };

        handler.postDelayed(r, 1);

        isRunning = true;
    }
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
    RequestQueue requestQueue;
    void SendDataToServer(String Adress, String Data)
    {
        //final TextView mTextView = (TextView) findViewById(R.id.debugRequestResponse);
        JSONObject body = new JSONObject();
        try {
            body.put("data", Data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = "http://" + Adress + "/action"; // + ID

        Log.v("RequestBuilder", "url: " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, body,  new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    VolleyLog.v("Response:%n %s", response.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        requestQueue.add(jsonObjectRequest);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this,"Service ended",Toast.LENGTH_SHORT).show();
        stopForeground(true);
    }
}
