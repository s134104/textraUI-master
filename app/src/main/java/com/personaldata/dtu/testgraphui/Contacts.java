package com.personaldata.dtu.testgraphui;

/**
 * Created by lyspl on 19-04-2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;

public class Contacts extends ListFragment implements AdapterView.OnItemClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_contactlist, container, false);

        //FragmentPagerSupport activity = (FragmentPagerSupport) getActivity();
        //String myDataFromActivity = activity.();


/*
        String[] values = new String[] { "Monica", "Chandler", "Pheobe", "ross", "barney" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);



        ListView myListView = (ListView) rootView.findViewById(android.R.id.list);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast t = Toast.makeText(getActivity(), "Message",
                        Toast.LENGTH_SHORT);
                t.show();

                switch(position) {
                    case 0: Intent myIntent = new Intent(getActivity(), ContactsGeneral.class);
                        getActivity().startActivity(myIntent);
                    case 1:Intent secondIntent = new Intent(getActivity(), ScrollingActivity.class);
                        getActivity().startActivity(secondIntent);
                    case 2: Intent third = new Intent(getActivity(), ContactsGeneral.class);
                        getActivity().startActivity(third);
                    case 3:Intent fourth = new Intent(getActivity(), ScrollingActivity.class);
                        getActivity().startActivity(fourth);


                }



                Log.d("MainActivity", "ListView item clicked.");
            }
        });
*/


        return rootView;


    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] values = new String[] { "Monica", "Chandler", "Pheobe", "ross", "barney" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }
//TODO change the array for each time we press on a given contact, based on data. graph etc.
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
        switch(position) {
            case 0:
                Intent myIntent = new Intent(getActivity(), ContactsGeneral.class);
                getActivity().startActivity(myIntent);

            case 1:
                Intent secondIntent = new Intent(getActivity(), ScrollingActivity.class);
                getActivity().startActivity(secondIntent);
            case 2:
                Intent third = new Intent(getActivity(), ContactsGeneral.class);
                getActivity().startActivity(third);
            case 3:
                Intent fourth = new Intent(getActivity(), ScrollingActivity.class);
                getActivity().startActivity(fourth);
        }


        }





    public static Contacts newInstance(String text) {

        Contacts f = new Contacts();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

}


