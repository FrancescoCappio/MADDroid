package it.polito.maddroid.lab2;

public class DailyOffer {
    private int id;
    private String name;
    private String description;
    private int quantity;
    private float price;
    private int quantityChosen;

    public DailyOffer(int DailyOfferid, String name,String description, int quantity,float price) {

        this.id = DailyOfferid;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.quantityChosen = 0;
    }
    
    public DailyOffer(DailyOffer dailyOffer) {
        this.id = dailyOffer.id;
        this.name = dailyOffer.name;
        this.description = dailyOffer.description;
        this.price = dailyOffer.price;
        this.quantity = dailyOffer.quantity;
        this.quantityChosen = 0;
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
    
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public Float getPrice (){
        return this.price;
    }
    public void setPrice(float price){
        this.price = price;
    }

    public int getQuantityChosen(){return this.quantityChosen;}
    
    public void setQuantityChosen(int quantityChosen){
        this.quantityChosen = quantityChosen;
    }

    public void addToQuantity(){
        this.quantityChosen++;
    }

    public void removeFromQuantity(){
        if(this.quantityChosen > 0){
            this.quantityChosen--;
        }
    }
}
