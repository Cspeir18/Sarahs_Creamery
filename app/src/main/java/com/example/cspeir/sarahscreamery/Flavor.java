package com.example.cspeir.sarahscreamery;

/**
 * Created by cspeir on 11/12/2017.
 */

public class Flavor {
    private String flavorName;
    private Boolean shared;
    private String objectId;
    private Boolean pennState;
    public Flavor(){
        flavorName = "";
    }
    public Flavor(String name){
        flavorName = name;
    }
    public String getFlavorName() {
        return flavorName;
    }

    public void setFlavorName(String flavorName) {
        this.flavorName = flavorName;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
