package it.polito.maddroid.lab3.common;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class GeocodingLocation {

    private static boolean needDialog = false;

    private static final String TAG = "GeocodingLocation";
    private static List<Address> addressList;

    public static void getAddressFromLocation(final String locationAddress, final Context context, final Handler handler) {

        final Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    needDialog = false;
                    addressList = geocoder.getFromLocationName(locationAddress, 10);
                    if (addressList != null && addressList.size() > 0) {
                        if (addressList.size() > 1)
                            needDialog = true;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable to connect to Geocoder", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (addressList != null) {
                        
                        message.what = 1;

                        if (needDialog)
                            message.what = 2;

                        if (addressList.size() < 1)
                            message.what = 0;
                        else {
                            for (int i = 0; i < addressList.size(); ++i) {
                                if ((addressList.get(i).getThoroughfare() == null || addressList.get(i).getThoroughfare().isEmpty()) || (addressList.get(i).getSubThoroughfare() == null || addressList.get(i).getSubThoroughfare().isEmpty()))
                                    message.what = 0;

                            }
                        }
                        
                        Bundle bundle = new Bundle();
                        if (message.what == 0) {
                            result = "Unable to get Latitude and Longitude for this address location. " +
                                    "You must be accurate";
                            bundle.putString("address", result);

                        } else {
                            bundle.putSerializable("address", (Serializable) addressList);
                        }
                        message.setData(bundle);
                        message.sendToTarget();
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString("address", "Error getting address info");
                        message.setData(bundle);
                        message.sendToTarget();
                    }
                }
            }
        };
        thread.start();
    }

    public Address getAddress(Location location, Context context) {

        Geocoder geocoder;

        List<Address> addresses;

        geocoder = new Geocoder(context, Locale.getDefault());


        try {

            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            return addresses.get(0);

        } catch (IOException e) {

            e.printStackTrace();

        }

        return null;
    }



    }
