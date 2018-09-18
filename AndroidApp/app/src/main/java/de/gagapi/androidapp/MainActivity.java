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

    public static String MAIN_ACTION = "de.gagapi.androidapp.action.main";
    String FloatFormatString = "%.4f";


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
        Intent startIntent = new Intent(MainActivity.this, SensorTrackerService.class);

        startService(startIntent);
        // read settings
        userPref = getSharedPreferences("pref", 0);
        userPrefEditor = userPref.edit();

        serverIP = (TextView) findViewById(R.id.serverAdress);
        serverIP.setText(userPref.getString(SERVER_IP_PREF, ""));


        // find and setup UI graphs
        graphAcc = SetupGraphView((GraphView) findViewById(R.id.graphAcc));
        graphGrav = SetupGraphView((GraphView) findViewById(R.id.graphGrav));
        graphGyro = SetupGraphView((GraphView) findViewById(R.id.graphGyro));

        gravTextView = (TextView)  findViewById(R.id.gravOut);


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




    @Override
    protected void onResume() {
        super.onResume();

    }
}
