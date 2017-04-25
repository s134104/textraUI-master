package com.personaldata.dtu.testgraphui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by mohammad on 11/04/2017.
 */

public class MeInfo extends AppCompatActivity {

    TextView viewNumber;

    int flag = 0;
    int rangeInHours = 168;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.me_info);

        String getMyNumber = getIntent().getStringExtra("MY_PHONE_NUMBER");

        String getTotalIn = getIntent().getStringExtra("TOTALIN");
        String getTotalOut = getIntent().getStringExtra("TOTALOUT");
        String monday = getIntent().getStringExtra("MONDAY");
        String tuesday = getIntent().getStringExtra("TUESDAY");
        String wednesday = getIntent().getStringExtra("WEDNESDAY");
        String thursday = getIntent().getStringExtra("THURSDAY");
        String friday = getIntent().getStringExtra("FRIDAY");
        String saturday = getIntent().getStringExtra("SATURDAY");
        String sunday = getIntent().getStringExtra("SUNDAY");

        //Contact contact = new Contact(getContactNumber);
        //MainActivity getContacts = new MainActivity();
        //Contact contact = getContacts.getContact(getContactNumber);

        //Log.i("ContactInfo", "" + contact.getContactName(this));

        viewNumber = (TextView) findViewById(R.id.myInfo);
        viewNumber.setText("NAME: Me" + "\nNUMBER: " + getMyNumber
                + "\nAVERAGE FRIEND: " + getTotalIn
                + "\nAVERAGE YOU: " + getTotalOut
                + "\n\nMONDAY: "+ monday
                + "\n\nTUESDAY: " + tuesday
                + "\n\nWEDNESDAY: " + wednesday
                + "\n\nTHURSDAY: " + thursday
                + "\n\nFRIDAY: " + friday
                + "\n\nSATURDAY: " + saturday
                + "\n\nSUNDAY: " + sunday);

    }

}
