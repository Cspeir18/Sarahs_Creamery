package com.example.cspeir.sarahscreamery;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by cspeir on 12/17/2017.
 */
@DynamoDBTable(tableName = "Flavors")
public class Flavors {
    public Flavors(){
        name = "";
    }
    private String name;
    @DynamoDBHashKey(attributeName = "Name")
    @DynamoDBAttribute(attributeName = "Name")

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
