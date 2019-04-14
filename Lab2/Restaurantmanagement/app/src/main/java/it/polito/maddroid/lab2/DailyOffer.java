package it.polito.maddroid.lab2;

public class DailyOffer {
    private int id;
    private String name;
    private String description;
    private int quantity;
    private float price;
    private int quantityLeft;

    public DailyOffer(int DailyOfferid, String name,String description, int quantity,float price) {

        this.id = DailyOfferid;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.quantityLeft = quantity;
    }
    
    public DailyOffer(DailyOffer dailyOffer) {
        this.id = dailyOffer.id;
        this.name = dailyOffer.name;
        this.description = dailyOffer.description;
        this.price = dailyOffer.price;
        this.quantity = dailyOffer.quantity;
        this.quantityLeft = dailyOffer.quantityLeft;
    }
    
    public int getId(){
        return this.id;
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
    
    private void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public Float getPrice (){
        return this.price;
    }
    public void setPrice(float price){
        this.price = price;
    }

    

    public int addToQuantity(int count){
        if(this.quantityLeft > 0)
        {
            count ++;
            quantityLeft--;
        }
        return count;
    }

    public int removeFromQuantity(int count){
        if(count > 0){
            count--;
            this.quantityLeft++;
        }
        return count;
    }


    public int getQuantityLeft() {
        return quantityLeft;
    }

    public void setQuantityLeft(int quantityLeft) {
        this.quantityLeft = quantityLeft;
    }

}
