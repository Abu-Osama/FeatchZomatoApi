package com.abuosama.b_33featchzomatoapi;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment {

    Button b;
    ListView lv;
    ArrayList<Restaurants> al;
    MyAdapter m;

    public class MyAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return al.size();
        }
        @Override
        public Object getItem(int i) {
            return al.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.row, null);
            final TextView tv1 = (TextView) v.findViewById(R.id.textview1);
            TextView tv2 = (TextView) v.findViewById(R.id.textview2);
            TextView tv3 = (TextView) v.findViewById(R.id.textview3);
            TextView tv4 = (TextView) v.findViewById(R.id.textview4);
            final TextView tv5 = (TextView) v.findViewById(R.id.textview5);
            final TextView tv6 = (TextView) v.findViewById(R.id.textview6);
            Button b1 = (Button) v.findViewById(R.id.button1);
            //GET REST OBJ FROM ARR LIST BASED ON POSITION I
            Restaurants restaurants = al.get(i);
            //APPLY DATA ONTO TEXTVIEWS - GETTERS
            tv1.setText(restaurants.getRname());
            tv2.setText(restaurants.getRaddress());
            tv3.setText(restaurants.getRlocality());
            tv4.setText(restaurants.getRcity());
            tv5.setText(restaurants.getRlat());
            tv6.setText(restaurants.getRlong());
            //BUTTON CLICK LISTENER - FOR OPENING MAP FOR THIS RESTAURANT
            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), MapsActivity.class);
                    intent.putExtra("latitude", tv5.getText().toString());
                    intent.putExtra("longitude", tv6.getText().toString());
                    intent.putExtra("name", tv1.getText().toString());
                    startActivity(intent);
                }
            });
            return v;
        }
    }
    //CHECK INTERNET METHOD
    public boolean checkInternet(){
        ConnectivityManager conn = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conn != null){
            NetworkInfo info = conn.getActiveNetworkInfo();
            if(info != null && info.isConnected()){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    public class MyTask extends AsyncTask<String, Void, String>{
        //declare all variables
        URL myurl;
        HttpURLConnection connection;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String line;
        StringBuilder result;

        @Override
        protected String doInBackground(String... p1) {
            try {
                myurl = new URL(p1[0]);
                connection = (HttpURLConnection) myurl.openConnection();

                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("user-key", "8c35b43b80354924682997cff4a22a0b");
                connection.connect();

                inputStream = connection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                result = new StringBuilder();
                line = bufferedReader.readLine();
                while(line != null){
                    result.append(line);
                    line = bufferedReader.readLine();
                }
                return result.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            if(s == null){
                Toast.makeText(getActivity(), "NETWORK ISSUE, FIX",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            //tv.setText(s);
            //JSON DATA EXTRACTION - JSON PARSING
            try {
                JSONObject j = new JSONObject(s);
                JSONArray arr = j.getJSONArray("nearby_restaurants");
                for(int i=0; i<arr.length(); i++){
                    JSONObject temp = arr.getJSONObject(i);
                    JSONObject restr = temp.getJSONObject("restaurant");
                    String name = restr.getString("name"); //gives rest name
                    JSONObject loc = restr.getJSONObject("location");
                    String address = loc.getString("address");
                    String locality = loc.getString("locality");
                    String city = loc.getString("city");
                    String latitude = loc.getString("latitude");
                    String longitude = loc.getString("longitude");
                    //PASS DATA TO ARRAYLIST - SETTER
                    Restaurants restaurants = new Restaurants();
                    restaurants.setRname(name);
                    restaurants.setRaddress(address);
                    restaurants.setRlocality(locality);
                    restaurants.setRcity(city);
                    restaurants.setRlat(latitude);
                    restaurants.setRlong(longitude);
                    //ADD TO ARRAY LIST
                    al.add(restaurants);
                    //TELL TO ADAPTER
                    m.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            super.onPostExecute(s);
        }
    }

    public MyFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my, container, false);
        b = (Button) v.findViewById(R.id.button1);
        //tv = (TextView) v.findViewById(R.id.textView1);
        lv = (ListView) v.findViewById(R.id.listview1);
        al = new ArrayList<Restaurants>();
        m = new MyAdapter();
        lv.setAdapter(m);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInternet() == false){
                    Toast.makeText(getActivity(), "NO INTERNET", Toast.LENGTH_SHORT).show();
                    return;
                }
                MyTask myTask = new MyTask();
                myTask.execute("https://developers.zomato.com/api/v2.1/geocode?lat=12.8984&lon=77.6179");
            }
        });
        return v;
    }
}
