package it.polito.maddroid.lab2;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DataManager {
    private static final String TAG = "DataManager";
    
    //this path must be one of the paths specified in "file_paths.xml"
    public static final String IMAGES_DIR = "images";
    private static final String DATA_DIR = "data";
    
    private static final String DAILY_OFFERS_JSON = "DailyOffersData.json";
    private static final String ORDERS_JSON = "OrdersData.json";
    
    // instance for singleton pattern
    private static DataManager instance;
    
    private List<DailyOffer> dailyOffers;
    private List<Order> orders;
    
    private DataManager(Context context) {
        // we need to load the data so that we do not load them multiple times
        Gson gson = new Gson();
        
        // load daily offers
        String dailyOffersData = readJsonData(context, DAILY_OFFERS_JSON);
    
        if (dailyOffersData != null && !dailyOffersData.isEmpty()) {
            Type listType = new TypeToken<ArrayList<DailyOffer>>() {}.getType();
            dailyOffers = gson.fromJson(dailyOffersData, listType);
        } else {
            dailyOffers = new ArrayList<>();
        }
        
        // load orders
        String ordersData = readJsonData(context, ORDERS_JSON);
        if (ordersData != null && !ordersData.isEmpty()) {
            Type listType = new TypeToken<ArrayList<Order>>() {}.getType();
            orders = gson.fromJson(ordersData, listType);
        } else {
            orders = new ArrayList<>();
        }
        
    }
    
    public static DataManager getInstance(Context context) {
        if (instance == null)
            instance = new DataManager(context);
        return instance;
    }
    
    private String readJsonData(Context context, String fileName) {
        // function to read json file into string
        String mResponse = "";
        
        try {
            
            final File root = new File(context.getFilesDir() + File.separator + DATA_DIR + File.separator);
            
            if (!root.exists()) {
                if (!root.mkdirs()) {
                    Log.e(TAG, "Could not create folder");
                    return "";
                }
            }
            
            File dst = new File(root, fileName);
            
            if (!dst.exists()) {
                Log.d(TAG, "The requested file: " + fileName + " does not exists");
                return "";
            }
            
            FileInputStream is = new FileInputStream(dst);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            mResponse = new String(buffer);
            
        } catch (IOException e) {
            Log.e(TAG, "Cannot read from file: " + fileName);
            e.printStackTrace();
        }
        return mResponse;
    }
    
    public List<DailyOffer> getDailyOffers() {
        return dailyOffers;
    }
    
    public List<Order> getOrders() {
        return orders;
    }
    
    public int getNextDailyOfferId() {
        if (dailyOffers.isEmpty())
            return 1;
        DailyOffer dailyOffer = dailyOffers.get(dailyOffers.size()-1);
        return dailyOffer.getId() + 1;
    }
    
    public int getNextOrderId() {
        if (orders.isEmpty())
            return 1;
        Order o = orders.get(orders.size()-1);
        return o.getId() + 1;
    }
    
    private void saveDataToFile(Context context, String filename, String json) {
        try {
            final File root = new File(context.getFilesDir() + File.separator + DATA_DIR + File.separator);
    
            if (!root.exists()) {
                if (!root.mkdirs()) {
                    Log.e(TAG, "Could not create folder");
                }
            }
            
            FileWriter file = new FileWriter(new File(root, filename));
            file.write(json);
            file.flush();
            file.close();
        } catch (IOException e) {
            Log.e(TAG, "Cannot write json to file " + filename);
            e.printStackTrace();
        }
    }
    
    public void addNewDailyOffer(Context context, DailyOffer newData) {
        
        // Add new row to data
        dailyOffers.add(newData);
        
        // Sort the data by DailyOfferId
        Collections.sort(dailyOffers, (o1, o2) -> {return o1.getId() - o2.getId();});
        
        // obtain json string
        String json = new Gson().toJson(dailyOffers);
        
        // save json to file
        saveDataToFile(context, DAILY_OFFERS_JSON, json);
    }
    
    public void addNewOrder(Context context, Order newData) {
        
        // Add new row to data
        orders.add(newData);
        
        // Sort the data by DailyOfferId
        Collections.sort(orders, (o1, o2) -> o1.getId() - o2.getId());
        
        // obtain json string
        String json = new Gson().toJson(orders);
        
        // save json to file
        saveDataToFile(context, ORDERS_JSON, json);
    }
    
    public static File getDishImageFile(Context context, int id) {
        // Determine Uri of camera image to save.
        final File root = new File(context.getFilesDir() + File.separator + IMAGES_DIR + File.separator);
    
        if (!root.exists()) {
            if (!root.mkdirs()) {
                Log.e(TAG, "Could not create folder");
            }
        }
        
        final String fname = "dish_" + id + ".jpg";
        
        return new File(root, fname);
    }
    
    public void saveDishImage(Context context, int id) {
        
        if (id == -1) {
            Log.e(TAG, "Cannot save image without id");
            return;
        }
        File main = getDishImageFile(context, id);
        File tmp = getDishTmpFile(context);
        
        //when saving the image if we find out that the tmp photo is rotated we should save the
        //photo rotated back
        
        try {
            
    
            ExifInterface exif = new ExifInterface(tmp.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            
            if (orientation != 1) {
                //load bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(tmp.getAbsolutePath(), options);
                
                //rotate bitmap
                bitmap = Utility.rotateBitmap(bitmap, orientation);
                
                //save rotated bitmap
                FileOutputStream fos = new FileOutputStream(main);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 99, fos);
                fos.flush();
                fos.close();
            } else {
                FileInputStream fis = new FileInputStream(tmp);
    
                FileOutputStream fos = new FileOutputStream(main);
    
                byte[] buffer = new byte[4096];
                while (true) {
                    int bytesRead = fis.read(buffer);
                    if (bytesRead == -1)
                        break;
                    fos.write(buffer, 0, bytesRead);
                }
    
                fos.flush();
                fos.close();
                fis.close();
            }
            
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found exception: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOexception: " + e.getMessage());
            e.printStackTrace();
        }
        
    }
    
    public static File getDishTmpFile(Context context) {
        final File root = new File(context.getFilesDir() + File.separator + IMAGES_DIR + File.separator);
    
        if (!root.exists()) {
            if (!root.mkdirs()) {
                Log.e(TAG, "Could not create folder");
            }
        }
        
        final String fname = "dish_tmp.jpg";
        
        return new File(root, fname);
    }
    
    public Bitmap getDishBitmap(Context context, int id) {
        File img = getDishImageFile(context, id);
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(img.getAbsolutePath(), options);
        
        return bitmap;
    }
}
