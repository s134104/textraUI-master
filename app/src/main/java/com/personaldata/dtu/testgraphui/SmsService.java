package com.personaldata.dtu.testgraphui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SmsService extends Service {
    public SmsService() {
    }

    static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    ArrayList<Contact> contacts = new ArrayList<Contact>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //contacts = (ArrayList<Contact>) intent.getExtras().get("CONTACTS");

        SharedPreferences getSharedPreference= PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        Gson gson = new Gson();
        String json = getSharedPreference.getString("CONTACTS", "");
        //Contact contacts = gson.fromJson(json, Contact.class);
        Type type = new TypeToken<ArrayList<Contact>>(){}.getType();
        contacts = gson.fromJson(json, type);

        Log.i("SmsService", "AVG IN: " + getContact("50954535").getMessages().size());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);
        //Toast.makeText(getApplicationContext(), "Service started.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsReceiver);
        //Toast.makeText(getApplicationContext(), "Service stopped.", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final BroadcastReceiver smsReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {

                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++){
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                for (SmsMessage message : messages){
                    String strFrom = message.getDisplayOriginatingAddress();
                    String strMsg = message.getDisplayMessageBody();
                    String contactNumber = strFrom;

                    if(contactNumber.substring(0, 1).equals("+"))
                        // Remove country number prefix. Need to add 00-prefix too.
                        contactNumber = contactNumber.substring(3, contactNumber.length());

                    createNotification(contactNumber, getContactName(getApplicationContext(), contactNumber), strMsg, convertToTime(getContact(contactNumber).computeAverageRTIn(0, 168)));
                }



                    //createNotification();
                    Log.i("SmsService", "NEW SMS");
                }

            }
        }
    };

    public void createNotification(String contactNumber, String from, String message, String average) {
        // Prepare intent which is triggered if the
        // notification is selected

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", contactNumber, null));
        intent.setPackage(Telephony.Sms.getDefaultSmsPackage(getApplicationContext()));
        //Intent intent = new Intent(this, NotificationReceiverActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        //RemoteViews notificationView = new RemoteViews(this.getPackageName(), R.layout.custom_notification);

        //notificationView.setImageViewResource(R.id.notiIcon, R.drawable.ic_stat_name);
        //notificationView.setTextViewText(R.id.notiTitle, "Textra");
        //notificationView.setTextViewText(R.id.notiName, from);
        //notificationView.setTextViewText(R.id.notiAvg, "Average response time: " + average);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(this)
                .setContentTitle(from)
                .setSubText("Response Time: " + average)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setDefaults(Notification.DEFAULT_ALL)
                //.setContent(notificationView)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);

    }

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

    public String getContactName(Context context, String number) {
        // Convert phone number to contact name from phone book

        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
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

    public String convertToTime(int averageInSeconds) {
        int hours = averageInSeconds / 3600;
        int minutes = (averageInSeconds % 3600) / 60;
        int seconds = averageInSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}

