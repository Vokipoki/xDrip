package com.eveningoutpost.dexdrip;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.eveningoutpost.dexdrip.Models.Calibration;
import com.eveningoutpost.dexdrip.utils.ActivityWithMenu;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;


public class CalibrationGraph extends ActivityWithMenu {
    //public static String menu_name = "Calibration Graph";
    private LineChartView chart;
    private LineChartData data;
    public double start_x = 50;
    public double end_x = 300;

    TextView GraphHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration_graph);
        GraphHeader = (TextView) findViewById(R.id.CalibrationGraphHeader);
    }

    @Override
    public String getMenuName() {
        return getString(R.string.calibration_graph);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupCharts();
    }

    public void setupCharts() {
        chart = (LineChartView) findViewById(R.id.chart);
        List<Line> lines = new ArrayList<Line>();

        Calibration calibration = Calibration.lastValid();
        if (calibration != null) {
            //set header
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(2);
            String Header = "slope = " + df.format(calibration.slope) + " intercept = " + df.format(calibration.intercept);
            GraphHeader.setText(Header);

            //red line
            List<PointValue> lineValues = new ArrayList<PointValue>();
            lineValues.add(new PointValue((float) start_x, (float) (start_x * calibration.slope + calibration.intercept)));
            lineValues.add(new PointValue((float) end_x, (float) (end_x * calibration.slope + calibration.intercept)));
            Line calibrationLine = new Line(lineValues);
            calibrationLine.setColor(ChartUtils.COLOR_RED);
            calibrationLine.setHasLines(true);
            calibrationLine.setHasPoints(false);

            //calibration values
            List<Calibration> calibrations = Calibration.allForSensor();
            Line greyLine = getCalibrationsLine(calibrations, Color.parseColor("#66FFFFFF"));
            calibrations = Calibration.allForSensorInLastFourDays();
            Line blueLine = getCalibrationsLine(calibrations, ChartUtils.COLOR_BLUE);

            //add lines in order
            lines.add(greyLine);
            lines.add(blueLine);
            lines.add(calibrationLine);

        }
        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisX.setName("Raw Value");
        axisY.setName("BG");


        data = new LineChartData(lines);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        chart.setLineChartData(data);

    }

    @NonNull
    public Line getCalibrationsLine(List<Calibration> calibrations, int color) {
        List<PointValue> values = new ArrayList<PointValue>();
        for (Calibration calibration : calibrations) {
            PointValue point = new PointValue((float) calibration.estimate_raw_at_time_of_calibration, (float) calibration.bg);
            String time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date((long) calibration.raw_timestamp));
            point.setLabel(time.toCharArray());
            values.add(point);
        }

        Line line = new Line(values);
        line.setColor(color);
        line.setHasLines(false);
        line.setPointRadius(4);
        line.setHasPoints(true);
        line.setHasLabels(true);
        return line;
    }
}