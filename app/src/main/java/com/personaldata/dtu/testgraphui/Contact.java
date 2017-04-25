package com.personaldata.dtu.testgraphui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by mohammad on 25/03/2017.
 */

public class Contact implements Parcelable {
    public static final int MAX_SESSION_DURATION = 86401; // When to stop consider the average
    private String contactNumber;
    ArrayList<Messages> texts = new ArrayList<Messages>();

    // Most inelegant way of doing things:
    public Map<Integer, Collection<Long>> daysOfWeekIN = new HashMap<Integer, Collection<Long>>();
    public Map<Integer, Collection<Long>> daysOfWeekOUT = new HashMap<Integer, Collection<Long>>();
    /*
    ArrayList<Long> mondays = new ArrayList<Long>();
    ArrayList<Long> tuesdays = new ArrayList<Long>();
    ArrayList<Long> wednesdays = new ArrayList<Long>();
    ArrayList<Long> thursdays = new ArrayList<Long>();
    ArrayList<Long> fridays = new ArrayList<Long>();
    ArrayList<Long> saturdays = new ArrayList<Long>();
    ArrayList<Long> sundays = new ArrayList<Long>();
    */

    public Contact(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    private Contact(Parcel in) {
        contactNumber = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contactNumber);
    }

    public static final Parcelable.Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public String getContactName(Context context) {
        // Convert phone number to contact name from phone book

        String phoneNumber = getContactNumber();

        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }

        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    public String getContactNumber() { return contactNumber; }

    public void addMessages(Long date, String type) {
        Messages message = new Messages(contactNumber, date, type);
        texts.add(message);
    }

    public ArrayList<Messages> getMessages() { return texts; }

    public ArrayList<Long> computeResponseTimesOut(int flag, int rangeInHours) {
        ArrayList<Long> responseTimesOut = new ArrayList<Long>();

        // Monday = 2, Tuesday = 3, Wednesday = 4, Thursday = 5, Friday = 6, Saturday = 7, Sunday = 1
        daysOfWeekOUT.put(1, new ArrayList<Long>());
        daysOfWeekOUT.put(2, new ArrayList<Long>());
        daysOfWeekOUT.put(3, new ArrayList<Long>());
        daysOfWeekOUT.put(4, new ArrayList<Long>());
        daysOfWeekOUT.put(5, new ArrayList<Long>());
        daysOfWeekOUT.put(6, new ArrayList<Long>());
        daysOfWeekOUT.put(7, new ArrayList<Long>());

        for(int i = 0; i < getMessages().size(); i++) {
            try {

                if(rangeIndicator(flag, rangeInHours, i) == false)
                    break;

                //Log.i("MainActivity", "FROM: " + getMessages().get(i).getDirection() + ", " + getMessages().get(i).getDate() + ", " + getMessages().get(i).getType() + ", " + getMessages().get(i + 1).getType());

                if((getMessages().get(i).getType().equals("1")) && (getMessages().get(i+1).getType().equals("2"))) {

                    Date firstStamp = new Date();
                    firstStamp.setTime(getMessages().get(i).getTimestamp());

                    Date secondStamp = new Date();
                    secondStamp.setTime(getMessages().get(i + 1).getTimestamp());

                    //To calculate the difference between two dates
                    long diff = firstStamp.getTime() - secondStamp.getTime();
                    long convertDiff = TimeUnit.MILLISECONDS.toSeconds(diff);
                    if(convertDiff < MAX_SESSION_DURATION) {
                        responseTimesOut.add(convertDiff);
                        daysOfWeekOUT.get(getMessages().get(i).getDateInstance().get(Calendar.DAY_OF_WEEK)).add(convertDiff);
                    }
                } else {
                    //Log.i("MainActivity", "CONSECUTIVE MESSAGES");
                }
            } catch(IndexOutOfBoundsException e) {
                //Log.i("MainActivity", "OUT OF BOUNDS EXCEPTION");
            } catch(NullPointerException e) {
                Log.i("MainActivity", "NULL POINTER EXCEPTION");
            }
        }

        return responseTimesOut;
    }

    public ArrayList<Long> computeResponseTimesIn(int flag, int rangeInHours) {
        ArrayList<Long> responseTimesIn = new ArrayList<Long>();

        // Monday = 2, Tuesday = 3, Wednesday = 4, Thursday = 5, Friday = 6, Saturday = 7, Sunday = 1
        daysOfWeekIN.put(1, new ArrayList<Long>());
        daysOfWeekIN.put(2, new ArrayList<Long>());
        daysOfWeekIN.put(3, new ArrayList<Long>());
        daysOfWeekIN.put(4, new ArrayList<Long>());
        daysOfWeekIN.put(5, new ArrayList<Long>());
        daysOfWeekIN.put(6, new ArrayList<Long>());
        daysOfWeekIN.put(7, new ArrayList<Long>());

        for(int i = 0; i < getMessages().size(); i++) {
            try {

                if(rangeIndicator(flag, rangeInHours, i) == false)
                    break;

                //Log.i("MainActivity", "" + getMessages().get(i).getType() + ", " + getMessages().get(i + 1).getType());

                if((getMessages().get(i).getType().equals("2")) && (getMessages().get(i + 1).getType().equals("1"))) {
                    Date firstStamp = new Date();
                    firstStamp.setTime(getMessages().get(i).getTimestamp());

                    Date secondStamp = new Date();
                    secondStamp.setTime(getMessages().get(i + 1).getTimestamp());

                    //To calculate the difference between two dates
                    long diff = firstStamp.getTime() - secondStamp.getTime();
                    long convertDiff = TimeUnit.MILLISECONDS.toSeconds(diff);
                    if(convertDiff < MAX_SESSION_DURATION) {
                        responseTimesIn.add(convertDiff);
                        daysOfWeekIN.get(getMessages().get(i).getDateInstance().get(Calendar.DAY_OF_WEEK)).add(convertDiff);
                    }
                } else {
                    //Log.i("MainActivity", "CONSECUTIVE MESSAGES");
                }
            } catch(IndexOutOfBoundsException e) {
                //Log.i("MainActivity", "OUT OF BOUNDS EXCEPTION");
            } catch(NullPointerException e) {
                Log.i("MainActivity", "NULL POINTER EXCEPTION");
            }
        }

        return responseTimesIn;
    }

    public int computeAverageRTIn(int flag, int rangeInHours) {
        ArrayList<Long> durations = computeResponseTimesIn(flag, rangeInHours);
        int sum = 0;
        if(!durations.isEmpty()) {
            for (Long getRT : durations) {
                sum += getRT;
            }
            return sum / durations.size();
        }

        return sum;
    }

    public int computeAverageRTOut(int flag, int rangeInHours) {
        ArrayList<Long> durations = computeResponseTimesOut(flag, rangeInHours);
        int sum = 0;
        if(!durations.isEmpty()) {
            for (Long getRT : durations) {
                sum += getRT;
            }
            return sum / durations.size();
        }

        return sum;
    }

    public String getDate(long date) {
        // Convert timestamp to date format
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(date);
        return DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
    }

    public boolean rangeIndicator(int flag, int rangeInHours, int index) {
        // 0 = all, 1 = now, 2 = latest message
        //Log.i("MainActivity", "PREVIOUS: " + getDate(calRange.getTimeInMillis()));

        Date dateRange = new Date();
        Calendar calRange = Calendar.getInstance();

        switch (flag) {
            case 0:
                // Get all
                calRange.setTime(getMessages().get(getMessages().size() - 1).getDateInstance().getTime());
                //Log.i("MainActivity", "ALL: " + getDate(calRange.getTimeInMillis()));
                break;
            case 1:
                // One hour from today
                //Log.i("MainActivity", "NOW: " + getDate(calRange.getTimeInMillis()));
                break;
            case 2:
                // One hour from newest message
                calRange.setTime(getMessages().get(0).getDateInstance().getTime());
                //Log.i("MainActivity", "NEWEST: " + getDate(calRange.getTimeInMillis()));
                break;
            default:
        }

        calRange.add(Calendar.HOUR_OF_DAY, -rangeInHours);

        Date currentMessageDate = new Date();
        currentMessageDate.setTime(getMessages().get(index).getTimestamp());

        if(currentMessageDate.after(calRange.getTime())) {
            // If message date is older than one hour, loop should break
            return true;
        }

        //Log.i("MainActivity", "CURRENT MESSAGE: " + getDate(currentMessageDate.getTime()) + ", PREVIOUS DATE: " + getDate(calRange.getTimeInMillis()));

        return false;
    }

    public Map<Integer, Collection<Long>> getDaysOfWeekIN(int flag, int rangeInHours) {
        computeResponseTimesIn(flag, rangeInHours);

        return daysOfWeekIN;
    }
    public Map<Integer, Collection<Long>> getDaysOfWeekOUT(int flag, int rangeInHours) {
        computeResponseTimesOut(flag, rangeInHours);

        return daysOfWeekOUT;
    }

    public int computeDailyAverageIN(int day, int flag, int rangeInHours) {
        int sum = 0;
        if(!getDaysOfWeekIN(flag, rangeInHours).get(day).isEmpty()) {
            for (Long getDay : getDaysOfWeekIN(flag, rangeInHours).get(day)) {
                sum += getDay;
            }
            return sum / getDaysOfWeekIN(flag, rangeInHours).get(day).size();
        }

        return sum;
    }

    public int computeDailyAverageOUT(int day, int flag, int rangeInHours) {
        int sum = 0;
        if(!getDaysOfWeekOUT(flag, rangeInHours).get(day).isEmpty()) {
            for (Long getDay : getDaysOfWeekOUT(flag, rangeInHours).get(day)) {
                sum += getDay;
            }
            return sum / getDaysOfWeekOUT(flag, rangeInHours).get(day).size();
        }

        return sum;
    }

}
