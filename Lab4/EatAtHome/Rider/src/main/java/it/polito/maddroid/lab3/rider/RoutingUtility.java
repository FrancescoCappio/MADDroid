package it.polito.maddroid.lab3.rider;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;


public class RoutingUtility {
    
    public interface GetRouteCaller {
        void routeCallback(List<List<HashMap<String, String>>> route, String[] distances);
    }
    
    private LatLng origin;
    private LatLng destination;
    private GetRouteCaller caller;
    
    public RoutingUtility(Context context, LatLng origin, LatLng dest, GetRouteCaller caller) {
        this.origin = origin;
        this.destination = dest;
        this.caller = caller;
        
        String url = getUrl(origin, dest, context);
        FetchUrl fetchUrl = new FetchUrl();
        fetchUrl.execute(url);
    }
    
    private String getUrl(LatLng origin, LatLng dest, Context context) {
        
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        
        String sensor = "sensor=false";
        String mode = "mode=walking";
        
        
        String parameters = str_origin + "&" + str_dest + "&" + sensor+ "&" + mode + "&key=" + context.getString(R.string.google_maps_key);
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        
        return url;
    }
    
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            
            StringBuffer sb = new StringBuffer();
            
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            
            data = sb.toString();
            br.close();
            
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    
    private class FetchUrl extends AsyncTask<String, Void, String> {
        
        @Override
        protected String doInBackground(String... url) {
            
            String data = "";
            
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }
        
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            convertDataURLtoJson(result);
        }
    }
    
    private void convertDataURLtoJson(String... jsonData) {
        JSONObject jObject;
        try {
            jObject = new JSONObject(jsonData[0]);
            
            RoutingDataParser parser = new RoutingDataParser(jObject);
            
            caller.routeCallback(parser.parse(), parser.getDistance());
            
        } catch (Exception e) {
            Log.d("ParserTask",e.toString());
            e.printStackTrace();
        }
    }
}
