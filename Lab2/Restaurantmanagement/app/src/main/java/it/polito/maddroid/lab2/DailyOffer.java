package it.polito.maddroid.lab2;

public class DailyOffer {
    private int DailyOfferid;
    private String name;
    private String description;
    private int quantity;
    private float price;

    public DailyOffer(int DailyOfferid, String name,String description, int quantity,float price) {

        this.DailyOfferid = DailyOfferid;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId(){
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
    
}
