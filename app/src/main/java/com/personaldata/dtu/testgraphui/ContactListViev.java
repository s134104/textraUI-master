package com.personaldata.dtu.testgraphui;

/**
 * Created by lyspl on 19-04-2017.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactListViev extends ListFragment {

    //  GUI Widget
    ListView lvMsg;
//
    final int flag = 0; // 0 = Avg for all messages, overrides rangeInHours. 1 = Avg from today to rangeInHours. 2 = Avg from latest date in messages to rangeInHours
    final int rangeInHours = 168; // Compute averages for the past 7 days

    // Cursor Adapter
    CustomAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_contactlist, container, false);


        final FragmentPagerSupport getActivity = (FragmentPagerSupport) getActivity();

        lvMsg = (ListView) rootView.findViewById(android.R.id.list);
        adapter = new CustomAdapter(this, getActivity.getContacts());
        lvMsg.setAdapter(adapter);

        lvMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    Log.i("MainActivity", getActivity.getContacts().get(position).getContactName(getContext()));

                    Intent intent = new Intent(getContext(), ContactInfo.class);
                    intent.putExtra("PHONE_NUMBER", getActivity.getContacts().get(position).getContactNumber());
                    intent.putExtra("CONTACT_NAME", getActivity.getContacts().get(position).getContactName(getContext()));

                    intent.putExtra("MONDAY", convertToTime(getActivity.getContacts().get(position).computeDailyAverageIN(2, 0, 168)));
                    intent.putExtra("TUESDAY", convertToTime(getActivity.getContacts().get(position).computeDailyAverageIN(3, 0, 168)));
                    intent.putExtra("WEDNESDAY", convertToTime(getActivity.getContacts().get(position).computeDailyAverageIN(4, 0, 168)));
                    intent.putExtra("THURSDAY", convertToTime(getActivity.getContacts().get(position).computeDailyAverageIN(5, 0, 168)));
                    intent.putExtra("FRIDAY", convertToTime(getActivity.getContacts().get(position).computeDailyAverageIN(6, 0, 168)));
                    intent.putExtra("SATURDAY", convertToTime(getActivity.getContacts().get(position).computeDailyAverageIN(7, 0, 168)));
                    intent.putExtra("SUNDAY", convertToTime(getActivity.getContacts().get(position).computeDailyAverageIN(1, 0, 168)));

                    startActivity(intent);

                } catch(NullPointerException e) {
                    // Show Alert
                    Toast.makeText(getContext(), "Contact not in phonebook.", Toast.LENGTH_SHORT).show();
                }
            }

        });


        return rootView;


    }

    // Populate the custom listView accordingly
    private class CustomAdapter extends BaseAdapter {
        Context context;
        ArrayList<Contact> getContacts;

        public CustomAdapter(ContactListViev activity, ArrayList<Contact> getContacts) {

            this.context = activity.getContext();
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
            String getName = getContacts.get(i).getContactName(getContext());

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


    public static ContactListViev newInstance(String text) {

        ContactListViev f = new ContactListViev();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    public String convertToTime(int averageInSeconds) {
        int hours = averageInSeconds / 3600;
        int minutes = (averageInSeconds % 3600) / 60;
        int seconds = averageInSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}


