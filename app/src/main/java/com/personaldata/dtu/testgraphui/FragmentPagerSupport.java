package com.personaldata.dtu.testgraphui;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import java.util.Locale;

public class FragmentPagerSupport extends AppCompatActivity {

    ArrayList<Contact> contacts = new ArrayList<Contact>();

    //  GUI Widget
    ListView lvMsg;
    TextView ContactHeader,In_contactName, Out,avg_Hme,  avg_Hcontact,  avg_me,avg_contact;

    // Cursor Adapter
    CustomAdapter adapter;

    final int flag = 0; // 0 = Avg for all messages, overrides rangeInHours. 1 = Avg from today to rangeInHours. 2 = Avg from latest date in messages to rangeInHours
    final int rangeInHours = 168; // Compute averages for the past 7 days

    boolean notiChecked = false; // Toogle notification

    SlidingTabLayout mSlidingTabLayout;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pager);



        //defining all element in header and thereby being able to change
       ContactHeader = (TextView) findViewById(R.id.headerinfo);
        In_contactName = (TextView) findViewById(R.id.contactName);
        Out = (TextView) findViewById(R.id.out);
        avg_Hme = (TextView) findViewById(R.id.youAvg);
        avg_Hcontact = (TextView) findViewById(R.id.contactAvg);
        avg_me= (TextView) findViewById(R.id.YavgInt);
        avg_contact= (TextView) findViewById(R.id.CavgInt);


        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.tab_line));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mPager);

        // Read all text messages and processing
        firstRun();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(contacts);
        prefsEditor.putString("CONTACTS", json);
        prefsEditor.apply();

        SharedPreferences getSharedPreference= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean getPrefNotiChecked = getSharedPreference.getBoolean("prefNotiChecked", notiChecked);
        Intent intent = new Intent(this, SmsService.class);

        if(getPrefNotiChecked == true) {
            enableBroadcastReceiver();
            startService(intent);
        } else {
            disableBroadcastReceiver();
            stopService(intent);
        }

    }



    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.notiNoti);

        SharedPreferences getSharedPreference= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean getPrefNotiChecked = getSharedPreference.getBoolean("prefNotiChecked", notiChecked);
        //Toast.makeText(getApplicationContext(), "" + getPrefNotiChecked, Toast.LENGTH_LONG).show();
        checkable.setChecked(getPrefNotiChecked);

        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notiNoti:
                notiChecked = !item.isChecked();

                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                prefsEditor.putBoolean("prefNotiChecked", notiChecked);
                prefsEditor.apply();

                SharedPreferences getSharedPreference= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                boolean getPrefNotiChecked = getSharedPreference.getBoolean("prefNotiChecked", notiChecked);

                item.setChecked(getPrefNotiChecked);

                Intent intent = new Intent(this, SmsService.class);

                if(getPrefNotiChecked == true) {
                    //intent.putExtra("CONTACTS", contacts);
                    enableBroadcastReceiver();
                    startService(intent);
                    Toast.makeText(getApplicationContext(), "Notifications enabled.", Toast.LENGTH_SHORT).show();
                } else {
                    disableBroadcastReceiver();
                    stopService(intent);
                    Toast.makeText(getApplicationContext(), "Notifications disabled.", Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.me:

                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String getMyNumber = tm.getLine1Number();

                Bundle intentMe = new Bundle();

                if(getMyNumber.substring(0, 1).equals("+"))
                    // Remove country number prefix. Need to add 00-prefix too.
                    getMyNumber = getMyNumber.substring(3, getMyNumber.length());

                Log.i("MainActivity", "MY PHONE NUMBER: " + getMyNumber);

                ScreenSlidePageFragment.newInstance(getMyNumber, computeTotalIn(), computeTotalOut(), getDailyAverageMeClean(2, 0, 168), getDailyAverageMeClean(3, 0, 168), getDailyAverageMeClean(4, 0, 168), getDailyAverageMeClean(5, 0, 168), getDailyAverageMeClean(6, 0, 168), getDailyAverageMeClean(7, 0, 168), getDailyAverageMeClean(1, 0, 168));

                return true;
            case R.id.export:
                String fileName = "log_data.txt";

                try {
                    File root = new File(Environment.getExternalStorageDirectory() + File.separator + "Textra", "Logs");
                    //File root = new File(Environment.getExternalStorageDirectory(), "Notes");
                    if (!root.exists()) {
                        Log.i("MainActivity", "Creating dir @" + Environment.getExternalStorageDirectory() + File.separator + "Textra" + File.separator + "Logs");
                        root.mkdirs();
                    }
                    File gpxfile = new File(root, fileName);
                    if(gpxfile.delete()){
                        gpxfile.createNewFile();
                    }
                    FileWriter writer = new FileWriter(gpxfile, true);

                    for(int i = 0; i < getContacts().size(); i++) {
                        // Look up contact name in phonebook from number
                        String getName = getContacts().get(i).getContactName(getApplicationContext());

                        // If number not in phone book, display number
                        if(getName == null)
                            getName = getContacts().get(i).getContactNumber();

                        // Get initials for privacy
                        StringBuilder builder = new StringBuilder(3);
                        for(int j = 0; j < getName.split(" ").length; j++) {
                            builder.append(getName.split(" ")[j].substring(0, 1) + ". ");
                        }

                        writer.append(builder.toString() + ", IN: " + convertToTime(getContacts().get(i).computeAverageRTIn(flag, rangeInHours)) + ", OUT: " + convertToTime(getContacts().get(i).computeAverageRTOut(flag, rangeInHours)) + "\n");
                        //Log.i("MainActivity", "" + getContacts().get(i).getContactName(getApplicationContext()) + ", IN: " + convertToTime(getContacts().get(i).computeAverageRTIn()) + ", OUT: " + convertToTime(getContacts().get(i).computeAverageRTOut()));
                        for(Messages getMessage : getContacts().get(i).getMessages()) {
                            writer.append(builder.toString() + ", DATE: " + getMessage.getDate() + ", TYPE: " + getMessage.getType() + "\n");
                            writer.flush();
                        }
                    }

                    writer.flush();
                    writer.close();

                } catch (IOException e) {
                    e.printStackTrace();

                }


                Toast.makeText(getApplicationContext(), "Exported", Toast.LENGTH_LONG).show();

                return true;
            case R.id.about:
                Toast.makeText(getApplicationContext(), "About", Toast.LENGTH_LONG).show();
                return true;
            case R.id.help:
                Toast.makeText(getApplicationContext(), "Help", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Populate the custom listView accordingly
    private class CustomAdapter extends BaseAdapter {
        Context context;
        ArrayList<Contact> getContacts;

        public CustomAdapter(FragmentPagerSupport activity, ArrayList<Contact> getContacts) {

            this.context = activity;
            this.getContacts = getContacts;
        }

        @Override
        public int getCount() {
            return getContacts.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View v = view;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.row, null);

            }

            TextView contactName  = (TextView)v.findViewById(R.id.contactName);
            TextView contactIn  = (TextView)v.findViewById(R.id.contactIn);
            TextView contactOut = (TextView)v.findViewById(R.id.contactOut);

            // Look up contact name in phonebook from number
            String getName = getContacts.get(i).getContactName(getApplicationContext());

            // If number not in phone book, display number
            if(getName == null)
                getName = getContacts.get(i).getContactNumber();

            // Get initials for privacy
            StringBuilder builder = new StringBuilder(3);
            for(int j = 0; j < getName.split(" ").length; j++) {
                builder.append(getName.split(" ")[j].substring(0, 1) + ". ");
            }

            contactName.setText(builder.toString());
            contactIn.setText("" + convertToTime(getContacts.get(i).computeAverageRTIn(flag, rangeInHours)));
            contactOut.setText("" + convertToTime(getContacts.get(i).computeAverageRTOut(flag, rangeInHours)));

            return v;
        }


    }

    public void firstRun() {
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(message, null, null, null, null);
        startManagingCursor(c);
        int totalSMS = c.getCount();

        String contactNumber;

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS - 1; i++) {
                // Shift through all received text messages
                contactNumber = c.getString(c.getColumnIndexOrThrow("address"));

                if(contactNumber.substring(0, 1).equals("+"))
                    // Remove country number prefix. Need to add 00-prefix too.
                    contactNumber = contactNumber.substring(3, contactNumber.length());

                if(getContact(contactNumber) == null) {
                    Contact newContact = new Contact(contactNumber);
                    newContact.addMessages(c.getLong(c.getColumnIndex("date")), c.getString(c.getColumnIndexOrThrow("type")));
                    contacts.add(newContact);
                } else {
                    getContact(contactNumber).addMessages(c.getLong(c.getColumnIndex("date")), c.getString(c.getColumnIndexOrThrow("type")));
                }

                c.moveToNext();
            }



        }

       // Log.i("MainActivity", "SUNDAYS: " + getContact("50954535").getDaysOfWeekIN(0, 168).get(1).size());
       // Log.i("MainActivity", "AVERAGE SUNDAYS: " + convertToTime(getContact("50954535").computeDailyAverageIN(1, 0, 168)));
       // Log.i("MainActivity", "TOTAL IN: " + computeTotalIn());
       // Log.i("MainActivity", "TOTAL OUT: " + computeTotalOut());

        //Log.i("MainActivity", "GET RESPONSE TIMES OUT: " +  getContact("PHONENUMBERHERE").computeResponseTimesOut().toString());
        //Log.i("MainActivity", "GET AVERAGE RESPONSE TIMES OUT: " +  convertToTime(getContact("PHONENUMBERHERE").computeAverageRTOut(flag, rangeInHours)));

        /*
        Log.i("MainActivity", "GET NAME FROM NUMBER: " + getContact("PHONENUMBERHERE").getContactName(getApplicationContext()) );
        Log.i("MainActivity", "GET NUMBER OF MESSAGES TO X: " +  getContact("PHONENUMBERHERE").getMessages().size() );
        Log.i("MainActivity", "GET MESSAGE DATE TO X: " +  getContact("PHONENUMBERHERE").getMessages().get(7).getDate() + ", TYPE: " + getContact("PHONENUMBERHERE").getMessages().get(7).getType() );

        for(int i = 0; i < getContact("PHONENUMBERHERE").getMessages().size(); i++) {
            Log.i("MainActivity", "GET DATES: " + getContact("PHONENUMBERHERE").getMessages().get(i).getDate() + ", TYPE: " + getContact("PHONENUMBERHERE").getMessages().get(i).getType());
        }

        Log.i("MainActivity", "GET NUMBER OF CONTACTS: " +  getContacts().size());
        Log.i("MainActivity", "GET COMPUTED RESPONSE TIMES IN: " +  getContact("PHONENUMBERHERE").computeResponseTimesIn().size());
        Log.i("MainActivity", "GET COMPUTED RESPONSE TIMES OUT: " +  getContact("PHONENUMBERHERE").computeResponseTimesOut().size());
        Log.i("MainActivity", "GET RESPONSE TIMES IN: " +  getContact("PHONENUMBERHERE").computeResponseTimesIn().toString());
        Log.i("MainActivity", "GET RESPONSE TIMES OUT: " +  getContact("PHONENUMBERHERE").computeResponseTimesOut().toString());
        Log.i("MainActivity", "GET AVERAGE RESPONSE TIMES IN: " +  getContact("PHONENUMBERHERE").computeAverageRTIn());
        Log.i("MainActivity", "GET AVERAGE RESPONSE TIMES OUT: " +  getContact("PHONENUMBERHERE").computeAverageRTOut());
        */

        // Shawn's phone
        /*for(int i = 0; i < getContacts().size(); i++) {
            Log.i("MainActivity", "GET NAME: " + getContacts().get(i).getContactName(getApplicationContext()));
        }
        Log.i("MainActivity", "GET NUMBER OF CONTACTS: " +  getContacts().size());
        Log.i("MainActivity", "GET NUMBER OF TEXTS: " + c.getCount());
        */

        // LYS CODE: 160583

    }

    public ArrayList<Contact> getContacts() { return contacts; }
    public Contact getContact(String contactNumber) {
        if(!contacts.isEmpty()) {
            for(Contact contact : contacts) {
                if(contact.getContactNumber().equals(contactNumber)) {
                    return contact;
                }
            }
        } else {
            Log.i("MainActivity", "No such contact.");
        }

        return null;
    }

    public String convertToTime(int averageInSeconds) {
        int hours = averageInSeconds / 3600;
        int minutes = (averageInSeconds % 3600) / 60;
        int seconds = averageInSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String computeTotalIn() {
        ArrayList<Integer> totalIn = new ArrayList<Integer>();
        int sum = 0;

        for(Contact getContact : contacts) {
            totalIn.add(getContact.computeAverageRTIn(flag, rangeInHours));
        }

        if(!totalIn.isEmpty()) {
            for (Integer avgTotalIn : totalIn) {
                sum += avgTotalIn;
            }
            return convertToTime(sum / totalIn.size());
        }

        return convertToTime(sum);
    }

    public String computeTotalOut() {
        ArrayList<Integer> totalOut = new ArrayList<Integer>();
        int sum = 0;

        for(Contact getContact : contacts) {
            totalOut.add(getContact.computeAverageRTOut(flag, rangeInHours));
        }

        if(!totalOut.isEmpty()) {
            for (Integer avgTotalIn : totalOut) {
                sum += avgTotalIn;
            }
            return convertToTime(sum / totalOut.size());
        }

        return convertToTime(sum);
    }

    public String getDailyAverageMe(int day, int flag, int rangeInHours) {
        ArrayList<Integer> meOut = new ArrayList<Integer>();
        int sum = 0;

        for(Contact getContact : contacts) {
            meOut.add(getContact.computeDailyAverageOUT(day, flag, rangeInHours));
        }

        if(!meOut.isEmpty()) {
            for (Integer avgMeOut : meOut) {
                sum += avgMeOut;
            }
            return convertToTime(sum / meOut.size());
        }

        return convertToTime(sum);
    }

    public int getDailyAverageMeClean(int day, int flag, int rangeInHours) {
        ArrayList<Integer> meOut = new ArrayList<Integer>();
        int sum = 0;

        for(Contact getContact : contacts) {
            meOut.add(getContact.computeDailyAverageOUT(day, flag, rangeInHours));
        }

        if(!meOut.isEmpty()) {
            for (Integer avgMeOut : meOut) {
                sum += avgMeOut;
            }
            return (sum / meOut.size()) / 60;
        }

        return sum / 60;
    }

    public void enableBroadcastReceiver(){
        ComponentName receiver = new ComponentName(this, SmsService.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void disableBroadcastReceiver(){
        ComponentName receiver = new ComponentName(this, SmsService.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
            public Fragment getItem(int pos) {



            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String getMyNumber = tm.getLine1Number();

            if(getMyNumber.substring(0, 1).equals("+"))
                // Remove country number prefix. Need to add 00-prefix too.
                getMyNumber = getMyNumber.substring(3, getMyNumber.length());



                switch(pos) {
                    case 0:


                        ContactHeader.setVisibility(View.VISIBLE);
                        //TODO it needs to call a method based on contact name in list
                        In_contactName.setText("IN");
                        In_contactName.setPadding(90, 0,0,0);

                        Out.setVisibility(View.VISIBLE);
                        avg_Hme.setVisibility(View.INVISIBLE);
                        avg_Hcontact.setVisibility(View.INVISIBLE);
                        avg_me.setVisibility(View.INVISIBLE);
                        avg_contact.setVisibility(View.INVISIBLE);

                        return ContactListViev.newInstance("Contacts, Instance 2");

                    case 1:

                        ContactHeader.setVisibility(View.VISIBLE);
                        //TODO it needs to call a method based on contact name in list
                        In_contactName.setText("Monica Geller");

                        Out.setVisibility(View.INVISIBLE);
                        avg_Hme.setVisibility(View.VISIBLE);
                        avg_Hcontact.setVisibility(View.VISIBLE);
                        //TODO it needs to call a method based on contact name in list
                        avg_Hcontact.setText("Monica");
                        avg_me.setVisibility(View.VISIBLE);
                        //TODO it needs to call a method based on average
                        avg_me.setText("1.77");
                        avg_contact.setVisibility(View.VISIBLE);
                        avg_contact.setText("2.89");


                        return ScreenSlidePageFragment.newInstance(getMyNumber, computeTotalIn(), computeTotalOut(), getDailyAverageMeClean(2, 0, 168), getDailyAverageMeClean(3, 0, 168), getDailyAverageMeClean(4, 0, 168), getDailyAverageMeClean(5, 0, 168), getDailyAverageMeClean(6, 0, 168), getDailyAverageMeClean(7, 0, 168), getDailyAverageMeClean(1, 0, 168));

                    default:
                        ContactHeader.setVisibility(View.VISIBLE);
                        //TODO it needs to call a method based on contact name in list
                        In_contactName.setText("Monica Geller");

                        Out.setVisibility(View.INVISIBLE);
                        avg_Hme.setVisibility(View.VISIBLE);
                        avg_Hcontact.setVisibility(View.VISIBLE);
                        //TODO it needs to call a method based on contact name in list
                        avg_Hcontact.setText("Monica");
                        avg_me.setVisibility(View.VISIBLE);
                        //TODO it needs to call a method based on average
                        avg_me.setText("1.77");
                        avg_contact.setVisibility(View.VISIBLE);
                        avg_contact.setText("2.89");

                        return ScreenSlidePageFragment.newInstance(getMyNumber, computeTotalIn(), computeTotalOut(), getDailyAverageMeClean(2, 0, 168), getDailyAverageMeClean(3, 0, 168), getDailyAverageMeClean(4, 0, 168), getDailyAverageMeClean(5, 0, 168), getDailyAverageMeClean(6, 0, 168), getDailyAverageMeClean(7, 0, 168), getDailyAverageMeClean(1, 0, 168));
                }
        }
        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }




            @Override
            public int getCount() {
                return 2;
            }
        }
    }


