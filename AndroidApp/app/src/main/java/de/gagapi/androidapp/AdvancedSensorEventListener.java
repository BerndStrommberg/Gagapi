package de.gagapi.androidapp;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;

class AdvancedSensorEventListener implements SensorEventListener
{
    int HISTORY_SIZE = 1000;
    public AdvancedSensorEventListener(TextView debugRawValue, TextView debugMedianValue, TextView debugAverageMag, TextView debugAverageJerk) {
        this.debugRawValue = debugRawValue;
        this.debugMedianValue = debugMedianValue;
        this.debugAverageMag = debugAverageMag;
        this.debugAverageJerk = debugAverageJerk;
    }

    public AdvancedSensorEventListener(GraphView graph, Filter filterX, Filter filterY, Filter filterZ, TextView debugRawValue, TextView debugMedianValue, TextView debugAverageMag, TextView debugAverageJerk) {

        this.graph = graph;
        FilterX = filterX;
        FilterY = filterY;
        FilterZ = filterZ;
        this.debugRawValue = debugRawValue;
        this.debugMedianValue = debugMedianValue;
        this.debugAverageMag = debugAverageMag;
        this.debugAverageJerk = debugAverageJerk;

        graphSeriesX = new LineGraphSeries();
        graphSeriesY = new LineGraphSeries();
        graphSeriesZ = new LineGraphSeries();
        graphSeriesX.setColor(Color.RED);
        graphSeriesY.setColor(Color.GREEN);
        graphSeriesZ.setColor(Color.BLUE);
        graph.addSeries(graphSeriesX);
        graph.addSeries(graphSeriesY);
        graph.addSeries(graphSeriesZ);
    }

    GraphView graph = null;
    private LineGraphSeries graphSeriesX, graphSeriesY, graphSeriesZ;

    Filter FilterX;
    Filter FilterY;
    Filter FilterZ;

    TextView debugRawValue, debugMedianValue, debugAverageMag, debugAverageJerk;

    ArrayList<float3> accumulatedValues = new ArrayList<>();
    ArrayList<float3> accumulatedJerk = new ArrayList<>();
    final float SENDING_RATE_SECONDS = 2.5f;

    float3 previousValue = new float3(0,0,0);
    long tPrevious = -1;

    static class MathyStuffFromListyList
    {
        public float3 medianValue;
        public float medianMag;

        public float3 medianJerk;
        public MathyStuffFromListyList(ArrayList<float3> sampleValues, Filter filterX, Filter filterY, Filter filterZ)
        {
            int arrayLength = sampleValues.size();

            // convert the values to an array for futher proccessing
            float3[] sampleValuesArray = new float3[arrayLength];
            sampleValues.toArray(sampleValuesArray);

            // if a filter is present, apply it to the values
            if(filterX != null)
            {
                // loop over each sample and apply the filter
                for(int i = 0; i < sampleValuesArray.length; i++)
                {
                    float3 sample = sampleValues.get(i);
                    float x = filterX.Step(sample.x);
                    float y = filterY.Step(sample.y);
                    float z = filterZ.Step(sample.z);
                    sampleValuesArray[i] = new float3(x, y, z); // write filtered value
                }
            }
            //(http://www-users.cs.york.ac.uk/~fisher/cgi-bin/mkfscript)
            Arrays.sort(sampleValuesArray);

            int middle = arrayLength / 2;
            if (sampleValuesArray.length%2 == 1) {
                medianValue = sampleValuesArray[middle];
            } else {
                medianValue = (sampleValuesArray[middle-1].add(sampleValuesArray[middle])).mul(0.5f);
            }

            medianMag = medianValue.length();
        }
    }
    int graphIndex = 0;
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
            MathyStuffFromListyList mathstuff = new MathyStuffFromListyList(accumulatedValues, FilterX, FilterY, FilterZ);
            debugMedianValue.setText("median: " + mathstuff.medianValue.toString());
            debugAverageMag.setText("medianMag: " + Float.toString(mathstuff.medianMag));
            accumulatedValues.clear();

            if(graph != null)
            {
                graphSeriesX.appendData(new DataPoint(graphIndex, mathstuff.medianValue.x), true, 40);
                graphSeriesY.appendData(new DataPoint(graphIndex, mathstuff.medianValue.y), true, 40);
                graphSeriesZ.appendData(new DataPoint(graphIndex, mathstuff.medianValue.z), true, 40);
                graphIndex++;
            }
        }

        //  debug output
        debugRawValue.setText("value: " + value.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
