package it.polito.maddroid.lab2;
import java.util.Comparator;


public class DailyOffer {
    private int DailyOfferid;
    private String name;
    private String description;
    private int quantity;
    private float price;
    private String photoPath;

    public DailyOffer(int DailyOfferid, String name,String description, int quantity,float price, String photoPath) {

        this.DailyOfferid = DailyOfferid;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.photoPath = photoPath;
    }

    public int getid (){
        return this.DailyOfferid;
    }
    public String getName (){
        return this.name;
    }

    public String getDescription (){
        return this.description;
    }

    public int getQuantity (){
        return this.quantity;
    }

    public Float getPrice (){
        return this.price;
    }


    static class Sortbyroll implements Comparator<DailyOffer>
    {
        // Used for sorting in ascending order of
        // roll number
        public int compare(DailyOffer a, DailyOffer b)
        {
            return a.DailyOfferid - b.DailyOfferid;
        }
    }

}
