package it.polito.maddroid.lab2;

public class DailyOffer {
    private int DailyOfferid;
    private String name;
    private String description;
    private int quantity;
    private float price;
    private int quantityChose ;

    public DailyOffer(int DailyOfferid, String name,String description, int quantity,float price) {

        this.DailyOfferid = DailyOfferid;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.quantityChose = 0;
    }
    
    public int getId(){
        return this.DailyOfferid;
    }
    
    public String getName (){
        return this.name;
    }
    
    public void setName (String name){
        this.name = name;
    }

    public String getDescription (){
        return this.description;
    }
    
    public void setDescription(String description){
        this.description = description;
    }

    public int getQuantity (){ return this.quantity; }
    
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public Float getPrice (){
        return this.price;
    }
    public void setPrice(float price){
        this.price = price;
    }

    public int getQuantityChose(){return this.quantityChose;}
    public void setQuantityChose(int quantityChose){
        this.quantityChose = quantityChose;
    }

    public void addToQuantity(){
        this.quantityChose++;
    }

    public void removeFromQuantity(){
        if(this.quantityChose > 0){
            this.quantityChose--;
        }
    }
}
