package org.firstinspires.ftc.teamcode.hardware;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qualcomm.robotcore.hardware.AnalogInput;

import java.util.ArrayList;

/**
 * Calibrated analog inputs -- used for the potentiometers to compensate for hub ADC input impedance
 */
public class CalibratedAnalogInput
{
    private Double[] calibrationX, calibrationY;
    private AnalogInput in;
    
    /**
     * Create a CalibratedAnalogInput. Reads the file specified by <code>calibrationJson</code> for
     * calibration data. The data should be JSON with the following format:
     * <pre>
     * {
     *   "xValues": [ X values (linear positions) ],
     *   "yValues": [ Y values (the same number of non-linear potentiometer values) ]
     * }
     * </pre>
     *
     * @param in              The analog input
     * @param calibrationJson The calibration data file
     */
    public CalibratedAnalogInput(AnalogInput in, JsonObject calibrationJson)
    {
        this.in = in;
        loadCalibration(calibrationJson);
    }
    
    /**
     * Get the calculated linear position based on the analog input. For optimal performance, this
     * value should be cached.
     *
     * @return A linear position between 0.0 and 1.0
     */
    public double get()
    {
        if (in == null) return 0;
        double value = in.getVoltage() / in.getMaxVoltage();
        return linearize(value);
    }
    
    private double linearize(double y)
    {
        // Iterate through the loop; find which line of the calibration curve to use
        int idx = 0;
        for (; idx < calibrationX.length; idx++)
        {
            if (calibrationY[idx] > y) break;
        }
        if (idx == 0) return 0;
        if (idx == calibrationX.length) return 1;
        // the line is between (x[idx-1], y[idx-1]) and (x[idx], y[idx])
        double xbase = calibrationX[idx];
        // find where we are on the line (interpolate)
        double dx = (calibrationX[idx] - calibrationX[idx - 1])
                * (y - calibrationY[idx]) / (calibrationY[idx] - calibrationY[idx - 1]);
        return xbase + dx;
    }
    
    private void loadCalibration(JsonObject root)
    {
        /*
            Calibration data format:
            {
              "xValues": [ X values (linear values) ],
              "yValues": [ Y values (nonlinear values) ]
            }
        */
        JsonArray xValues = root.get("xValues").getAsJsonArray();
        JsonArray yValues = root.get("yValues").getAsJsonArray();
        ArrayList<Double> xList = new ArrayList<>();
        ArrayList<Double> yList = new ArrayList<>();
        for (JsonElement elem : xValues)
        {
            xList.add(elem.getAsDouble());
        }
        for (JsonElement elem : yValues)
        {
            yList.add(elem.getAsDouble());
        }
        calibrationX = xList.toArray(new Double[0]);
        calibrationY = yList.toArray(new Double[0]);
    }
}
