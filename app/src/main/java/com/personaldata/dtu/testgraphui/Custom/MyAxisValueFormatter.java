package com.personaldata.dtu.testgraphui.Custom;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by lyspl on 14-04-2017.
 */

public class MyAxisValueFormatter implements IAxisValueFormatter {


    private DecimalFormat mFormat;

    public MyAxisValueFormatter() {
        mFormat = new DecimalFormat("###,###,###,##0");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        return  "";
    }

}
