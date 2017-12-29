package com.example.cspeir.sarahscreamery;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.ArrayList;

/**
 * Created by cspeir on 12/17/2017.
 */
@DynamoDBTable(tableName = "Flavors")
public class Flavors {
    public Flavors(){
        name = "";

        flavorsList = new ArrayList<>();
    }
    private String name;
    private ArrayList<String> flavorsList;
    @DynamoDBHashKey(attributeName = "Name")
    @DynamoDBAttribute(attributeName = "Name")

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getFlavorsList() {
        return flavorsList;
    }

    public void setFlavorsList(ArrayList<String> flavorsList) {
        this.flavorsList = flavorsList;
    }
}
