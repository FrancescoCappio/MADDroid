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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
    
    private HashMap<Integer, DailyOffer> dailyOffers;
    private HashMap<Integer, Order> orders;
    
    private int dailyOfferMaxId = -1;
    private int orderMaxId = -1;
    
    private static final int IMAGE_SIDE = 500;
    
    private DataManager(Context context) {
        // we need to load the data so that we do not load them multiple times
        Gson gson = new Gson();
        
        // load daily offers
        String dailyOffersData = readJsonData(context, DAILY_OFFERS_JSON);
    
        dailyOffers = new HashMap<>();
        if (dailyOffersData != null && !dailyOffersData.isEmpty()) {
            Type listType = new TypeToken<ArrayList<DailyOffer>>() {}.getType();
            
            List<DailyOffer> dOffers = gson.fromJson(dailyOffersData, listType);
            
            for (DailyOffer dailyOffer : dOffers) {
                if (dailyOffer.getId() > dailyOfferMaxId)
                    dailyOfferMaxId = dailyOffer.getId();
                dailyOffers.put(dailyOffer.getId(), dailyOffer);
            }
        }
        
        // load orders
        String ordersData = readJsonData(context, ORDERS_JSON);
    
        orders = new HashMap<>();
        if (ordersData != null && !ordersData.isEmpty()) {
            Type listType = new TypeToken<ArrayList<Order>>() {}.getType();
            List<Order> jorders = gson.fromJson(ordersData, listType);
            
            for (Order o : jorders) {
                if (o.getId() > orderMaxId)
                    orderMaxId = o.getId();
                orders.put(o.getId(), o);
            }
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
        Collection<DailyOffer> dao = dailyOffers.values();
        List<DailyOffer> list = new ArrayList<>(dao);
        Collections.sort(list, (o1, o2) ->  o1.getId() - o2.getId());
        
        List<DailyOffer> dailyOfferList = new ArrayList<>();
        for (DailyOffer dailyOffer : list)
            dailyOfferList.add(new DailyOffer(dailyOffer));
        return dailyOfferList;
    }
    
    public List<Order> getOrders() {
        Collection<Order> dao = orders.values();
        List<Order> list = new ArrayList<>(dao);
        Collections.sort(list, (o1, o2) ->  {
            
            if (o1.getTimeHour() != o2.getTimeHour())
                return o1.getTimeHour() - o2.getTimeHour();
            
            return o1.getTimeMinutes() - o2.getTimeMinutes();
        });
    
        List<Order> orderArrayList = new ArrayList<>();
        for (Order order : list)
            orderArrayList.add(new Order(order));
        return orderArrayList;
    }
    
    public int getNextDailyOfferId() {
        return dailyOfferMaxId + 1;
    }
    
    public int getNextOrderId() {
        return orderMaxId + 1;
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
        dailyOffers.put(newData.getId(), newData);
        
        if (newData.getId() > dailyOfferMaxId)
            dailyOfferMaxId = newData.getId();
    
        saveDailyOffers(context);
    }
    
    private void saveDailyOffers(Context context) {
        Collection<DailyOffer> dao = dailyOffers.values();
        
        List<DailyOffer> l = new ArrayList<>(dao);
        
        // obtain json string
        String json = new Gson().toJson(l);
        
        // save json to file
        saveDataToFile(context, DAILY_OFFERS_JSON, json);
    }
    
    public void addNewOrder(Context context, Order newData) {
        
        // Add new row to data
        orders.put(newData.getId(), newData);
    
        if (newData.getId() > orderMaxId)
            orderMaxId = newData.getId();
    
        saveOrders(context);
    }
    
    private void saveOrders(Context context) {
        Collection<Order> dao = orders.values();
        
        // Sort the data by DailyOfferId
        List<Order> l = new ArrayList<>(dao);
        
        // obtain json string
        String json = new Gson().toJson(l);
        
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
    
            //load bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(tmp.getAbsolutePath(), options);
            
            if (orientation != 1) {
                //rotate bitmap
                bitmap = Utility.rotateBitmap(bitmap, orientation);
            }
            
            bitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIDE, IMAGE_SIDE,false);
            
            //save rotated bitmap
            FileOutputStream fos = new FileOutputStream(main);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 99, fos);
            fos.flush();
            fos.close();
            
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found exception: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOexception: " + e.getMessage());
            e.printStackTrace();
        }
        
    }
    
    public void deleteDailyOfferWithId(Context context, int id) {
        dailyOffers.remove(id);
        saveDailyOffers(context);
        //TODO: also delete the photo!
    }
    
    public void deleteOrderWithId(Context context, int id) {
        orders.remove(id);
        saveOrders(context);
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
    
    public DailyOffer getDailyOfferWithId(int id) {
        DailyOffer dailyOffer = dailyOffers.get(id);
        if (dailyOffer != null)
            return new DailyOffer(dailyOffer);
        else
            return null;
    }

    public Order getOrderWithId(int id) {
        Order order = orders.get(id);
        
        if (order != null)
            return new Order(order);
        else
            return null;
    }
    
    public void setDailyOfferWithId(Context context, DailyOffer dailyOffer) {
        dailyOffers.put(dailyOffer.getId(), dailyOffer);
        saveDailyOffers(context);
    }

    public void setOrderWithID(Context applicationContext, Order o) {
        orders.put(o.getId(), o);
        saveOrders(applicationContext);
    }
}
