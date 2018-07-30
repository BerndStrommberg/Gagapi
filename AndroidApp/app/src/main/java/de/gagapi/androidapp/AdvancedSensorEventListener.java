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

        sensorSampleProcessor = new SensorSampleProcessor();
    }

    public AdvancedSensorEventListener(String title, GraphView graph, Filter filterX, Filter filterY, Filter filterZ)
    {
        this.graph = graph;
        sensorSampleProcessor = new SensorSampleProcessor(filterX, filterY, filterZ);
        graphSeriesX = new LineGraphSeries();
        graphSeriesX.setTitle(title + "x");
        graphSeriesX.setColor(Color.RED);
        graph.addSeries(graphSeriesX);

        graphSeriesY = new LineGraphSeries();
        graphSeriesY.setTitle(title + "y");
        graphSeriesY.setColor(Color.GREEN);
        graph.addSeries(graphSeriesY);

        graphSeriesZ = new LineGraphSeries();
        graphSeriesZ.setTitle(title + "z");
        graphSeriesZ.setColor(Color.BLUE);
        graph.addSeries(graphSeriesZ);

        medianMag = new LineGraphSeries();
        medianMag.setTitle(title + "Mag.");
        medianMag.setColor(Color.BLACK);
        graph.addSeries(medianMag);

        medianJerk = new LineGraphSeries();
        medianJerk.setTitle(title + "Jerk. ");
        medianJerk.setColor(Color.YELLOW);
        graph.addSeries(medianJerk);
    }

    GraphView graph = null;
    private LineGraphSeries graphSeriesX, graphSeriesY, graphSeriesZ, medianMag, medianJerk;

    TextView debugRawValue, debugMedianValue, debugAverageMag, debugAverageJerk;

    ArrayList<float3> accumulatedValues = new ArrayList<>();

    SensorSampleProcessor sensorSampleProcessor;
    final float SENDING_RATE_SECONDS = 2.5f;

    float3 previousValue = new float3(0,0,0);
    long tPrevious = -1;

    // obtains median value of the supplied list
    static class SensorSampleProcessor
    {
        static float3 getMedian(float3[] values)
        {
            Arrays.sort(values);
            int arrayLength = values.length;
            int middle = arrayLength / 2;
            float3 median;
            if (values.length%2 == 1) {
                return values[middle];
            } else {
                return (values[middle-1].add(values[middle])).mul(0.5f);
            }
        }

        public float3 medianValue;
        public float medianMag;
        float3 preValue = new float3(0,0,0);
        public float3 medianJerk;
        ArrayList<float3> jerkness = new ArrayList<>();

        public SensorSampleProcessor(Filter filterX, Filter filterY, Filter filterZ) {
            this.filterX = filterX;
            this.filterY = filterY;
            this.filterZ = filterZ;
        }

        public SensorSampleProcessor() {}

        Filter filterX, filterY, filterZ;

        public void Process(ArrayList<float3> sampleValues)
        {
            int arrayLength = sampleValues.size();

            // convert the values to an array for futher proccessing
            float3[] sampleValuesArray = new float3[arrayLength];
            sampleValues.toArray(sampleValuesArray);

            // if a filter is present, apply it to the values
            if(filterX != null)
            {
                //(http://www-users.cs.york.ac.uk/~fisher/cgi-bin/mkfscript)
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

            // compute jerkness
            jerkness.clear();
            for(int i = 0; i < sampleValuesArray.length; i++) {
                float3 currentValue = sampleValuesArray[i];
                jerkness.add(preValue.sub(currentValue));
                preValue = currentValue;
            }

            float3[] jerknessArray = new float3[jerkness.size()];
            jerkness.toArray(jerknessArray);
            medianJerk = getMedian(jerknessArray);

            medianValue = getMedian(sampleValuesArray);
            medianMag = medianValue.length();
        }
    }
    int graphIndex = 0;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float3 value = new float3(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);

        previousValue = value;
        // store rotation inside list
        accumulatedValues.add(value);

        long tCurrent = SystemClock.elapsedRealtime();
        long tDelta = tCurrent - tPrevious;
        float elapsedSeconds = tDelta / 1000.0f;


        if(elapsedSeconds > SENDING_RATE_SECONDS)
        {
            tPrevious = tCurrent;
            sensorSampleProcessor.Process(accumulatedValues);
            accumulatedValues.clear();

            if(graph != null)
            {
                graphSeriesX.appendData(new DataPoint(graphIndex, sensorSampleProcessor.medianValue.x), true, 40);
                graphSeriesY.appendData(new DataPoint(graphIndex, sensorSampleProcessor.medianValue.y), true, 40);
                graphSeriesZ.appendData(new DataPoint(graphIndex, sensorSampleProcessor.medianValue.z), true, 40);
                medianMag.appendData(new DataPoint(graphIndex, sensorSampleProcessor.medianMag), true, 40);

                medianJerk.appendData(new DataPoint(graphIndex, sensorSampleProcessor.medianJerk.length()), true, 40);
                graphIndex++;
            }else
            {
                debugMedianValue.setText("median: " + sensorSampleProcessor.medianValue.toString());
                debugAverageMag.setText("medianMag: " + Float.toString(sensorSampleProcessor.medianMag));
                //  debug output
                debugRawValue.setText("value: " + value.toString());
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
