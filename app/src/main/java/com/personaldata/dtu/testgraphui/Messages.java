package com.personaldata.dtu.testgraphui;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by mohammad on 25/03/2017.
 */

public class Messages implements Parcelable {
    private String address;
    private Long date;
    private String type; // 1 = In, 2 = Out

    public Messages(String address, Long date, String type) {
        this.address = address;
        this.date = date;
        this.type = type;
    }

    protected Messages(Parcel in) {
        address = in.readString();
        type = in.readString();
    }

    public static final Creator<Messages> CREATOR = new Creator<Messages>() {
        @Override
        public Messages createFromParcel(Parcel in) {
            return new Messages(in);
        }

        @Override
        public Messages[] newArray(int size) {
            return new Messages[size];
        }
    };

    public String getDirection() {
        if(getType().equals("1"))
            return getAddress();

        return "Me";
    }
    public String getAddress() { return address; }
    public Long getTimestamp() { return date; }
    public String getDate() {
        // Convert timestamp to date format
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(date);
        return DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
    }
    public Calendar getDateInstance() {
        // Convert timestamp to date format
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(date);
        return cal;
    }
    public String getType() { return type; }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(type);
    }
}
