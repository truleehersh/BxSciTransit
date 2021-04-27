package com.khan.hersh.bxscitransit;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class MainActivity extends Activity{

    Button refresh;
    public TextView timeStamp, bx10Stops, bx10Miles, bx28Stops, bx28Miles;
    String[] bx10 = null;
    String[] bx28 = null;
    Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //gets layout objects
        refresh = (Button) findViewById(R.id.refresh);
        timeStamp = (TextView) findViewById(R.id.timeStamp);
        bx10Stops = (TextView) findViewById(R.id.arrival10);
        bx10Miles = (TextView) findViewById(R.id.distance10);
        bx28Stops = (TextView) findViewById(R.id.arrival28);
        bx28Miles = (TextView) findViewById(R.id.distance28);


        // add onclick listener to the button
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
               // refresh();
                new myTask().execute();
               // bx10Stops.setText(bx10[0]);
                //bx10Miles.setText(bx10[1]);
                //bx28Stops.setText(bx28[0]);
                //bx28Miles.setText(bx28[1]);
            }
        });
    }

    private void onBackgroundTaskDataObtained(String[][] result){
        //sets textviews text to string data
        Log.e("bgData", result[0][0]+result[0][1]+result[1][0]+result[1][1]+result[1][2]);
        bx10Stops.setText(result[0][0]);
        bx10Miles.setText(result[0][1]);
        bx28Stops.setText(result[1][0]);
        bx28Miles.setText(result[1][1]);
        timeStamp.setText(result[1][2]);
    }



    private class myTask extends AsyncTask<Void,Void,String[][]> {
        //to fetch data
        @Override
        protected String[][] doInBackground(Void... params) {
            //bx10 URL
            String sbx10 = getData("http://bustime.mta.info/api/siri/stop-monitoring.json?key=324e19f5-bbf9-4c10-a2e3-3a4d4d43aeed&MonitoringRef=100017&LineRef=MTA%20NYCT_BX10&MaximumStopVisits=1");
            String[] bx10 = parseData(sbx10);
            if(bx10 !=null)
                 Log.e("doInBg", "bx10:" + bx10[0] + " " + bx10[1]);
            //bx28 URL
            String sbx28 = getData("http://bustime.mta.info/api/siri/stop-monitoring.json?key=324e19f5-bbf9-4c10-a2e3-3a4d4d43aeed&MonitoringRef=100017&LineRef=MTA%20NYCT_BX28&MaximumStopVisits=1");
            String[] bx28 = parseData(sbx28);
            String[][] busInfo = {bx10, bx28};

        return busInfo;
        }

        public String getData(String URL){
            Log.d("x", "refresh");
            String sUrl = URL; //"";
            HttpURLConnection URLcon = null;
            try {
                //creates URL object
                URL url = new URL(sUrl);

                //Opens URL object, gets string
                URLcon = (HttpURLConnection) url.openConnection();
                Log.w("connec", "made url connection");
                StringBuffer sbValue = new StringBuffer();
                String sline = null;
                BufferedReader reader = new BufferedReader(new InputStreamReader(URLcon.getInputStream()));
                Log.w("reader", "made bufferreader");
                while ((sline = reader.readLine()) != null) {
                    Log.d("readline: ", sline);
                    sbValue.append(sline);
                }
                reader.close();
                String sValue = sbValue.toString().trim();

                //String milesAway = busRef.getJSONObject("name");
                //if
                Log.d("sValue len: ", "" + sValue.length());
                return sValue;
            }catch (IOException e) {
                Log.d("Exception:", e.getMessage());

            }finally {
                URLcon.disconnect();
                Log.d("exit", "EXIT");
            }
            return null;
        }

        //parses JSON string to get needed data
        public String[] parseData (String x){
            try {
                //creates JSON object
                JSONObject jsonMTA = new JSONObject(x);
               // JSONArray result = new JSONArray
                String siri = jsonMTA.getString("Siri");
               // Log.e("parseData siri", siri);
                JSONObject jSiri = new JSONObject(siri);

                //parses through long string to find specific data needed
                String stops = jsonMTA.getJSONObject("Siri").getJSONObject("ServiceDelivery").getJSONArray("StopMonitoringDelivery").getJSONObject(0).getJSONArray("MonitoredStopVisit").getJSONObject(0).getJSONObject("MonitoredVehicleJourney").getJSONObject("MonitoredCall").getJSONObject("Extensions").getJSONObject("Distances").getString("StopsFromCall");
                Log.e("parseData stops", stops);
                String distance = jsonMTA.getJSONObject("Siri").getJSONObject("ServiceDelivery").getJSONArray("StopMonitoringDelivery").getJSONObject(0).getJSONArray("MonitoredStopVisit").getJSONObject(0).getJSONObject("MonitoredVehicleJourney").getJSONObject("MonitoredCall").getJSONObject("Extensions").getJSONObject("Distances").getString("PresentableDistance");
                String timeStamp = jsonMTA.getJSONObject("Siri").getJSONObject("ServiceDelivery").getString("ResponseTimestamp");
                Log.e("parseData dist", distance);
                Log.e("time", timeStamp);

                //adds data to string array
                String[] ret = new String[3];
                ret[0]=stops;
                ret[1]= distance;
                ret[2] = timeStamp;

                return ret;

            } catch (JSONException j){
            Log.d("JsonException:", j.getMessage());
            }
            return null;
         }

        //@Override
        protected void onPostExecute(String[][] result){
            Log.e("onPostexecute", result[0][0]);
            MainActivity.this.onBackgroundTaskDataObtained(result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
