package com.personaldata.dtu.testgraphui.Custom;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Created by lyspl on 14-04-2017.
 */
//This class is to ensure int value on top of bars
public class DataValueFormatter implements IValueFormatter {
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return String.valueOf((int)entry.getY());
    }
}

