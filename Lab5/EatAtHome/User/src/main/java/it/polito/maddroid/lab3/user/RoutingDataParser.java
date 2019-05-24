package it.polito.maddroid.lab3.user;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoutingDataParser {

    private JSONObject jObject;


    public RoutingDataParser(JSONObject jObject) {
        this.jObject = jObject;
    }

    public List<List<HashMap<String, String>>> parse() {

        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {

            jRoutes = this.jObject.getJSONArray("routes");

            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString((list.get(l)).latitude));
                            hm.put("lng", Double.toString((list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return routes;
    }


    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public String[] getDistance() {
        String[] data = new String[2];
        JSONArray jRoutes;
        JSONArray jLegs;
        try {
            jRoutes = this.jObject.getJSONArray("routes");
            jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("legs");
            String distance = (String) ((JSONObject) ((JSONObject) jLegs.get(0)).get("distance")).get("text");
            data[0] = Double.parseDouble(distance.split(" ")[0]) + " Km";
    
            int durationTime;
            
            if (((JSONObject) ((JSONObject) jLegs.get(0)).get("duration")).get("value") instanceof Integer)
                durationTime = (int) ((JSONObject) ((JSONObject) jLegs.get(0)).get("duration")).get("value");
            else {
                String duration = (String) ((JSONObject) ((JSONObject) jLegs.get(0)).get("duration")).get("value");
                durationTime = Integer.parseInt(duration);
            }
            
            int durationTimeBike = durationTime / 2;
            int durationTimeBikeMins = durationTimeBike / 60;
            
            if (durationTimeBikeMins > 60) {
                int hours = durationTimeBikeMins / 60;
                int remain = durationTimeBikeMins - hours*60;
                data[1] = hours + "h " + remain + "m";
            } else {
                data[1] = durationTimeBikeMins + "m";
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
}