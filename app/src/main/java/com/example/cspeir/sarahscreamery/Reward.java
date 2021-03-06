package com.example.cspeir.sarahscreamery;

import android.media.Image;

import java.util.Date;

/**
 * Created by cspeir on 11/24/2017.
 */

public class Reward {
    private String rewardName;
    private String description;
    private String direction;
    private Date startDate;
    private Date endDate;
    public static final String EXTRA_RELATION="org.pltw.examples.collegeapp.relation";
    public static final String EXTRA_INDEX= "org.pltw.examples.collegeapp.index";
    public Boolean shared;
    private String objectId;
    public Reward(){
        startDate = null;
        endDate = null;
        rewardName ="";
        description = "";
        direction = "";
        shared = false;
    }
    public Boolean getShared(){
        return shared;
    }
    public void setShared(Boolean share){
        this.shared = share;
    }
    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
