package com.personaldata.dtu.testgraphui.Custom;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by lyspl on 14-04-2017.
 */

public class WeekdayAxisValueFormatter implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int days = (int) value;
        String appendix = "day";

        switch (days) {
            case 1:
                appendix = "Mon";
                break;
            case 2:
                appendix = "Tue";
                break;
            case 3:
                appendix = "Wed";
                break;
            case 4:
                appendix = "Thu";
                break;
            case 5:
                appendix = "Fri";
                break;
            case 6:
                appendix = "Sat";
                break;
            case 7:
                appendix = "Sun";
                break;
        }

        return  appendix ;
    }
}









