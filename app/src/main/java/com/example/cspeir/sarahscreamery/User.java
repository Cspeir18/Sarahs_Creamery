package com.example.cspeir.sarahscreamery;

import java.util.Date;

/**
 * Created by cspeir on 11/12/2017.
 */

public class User {
    private String firstName;
    private String lastName;
    private Date birthday;
    private String rewardsUsed;
    private Boolean admin;
    private String email;

    public User(){
        firstName = "";
        rewardsUsed = "jhgjh";
        lastName = "";
        birthday = null;
        admin = false;
        email = "";
    }
    public User(String first, String last, Date bday, Boolean ad, String mail){
        firstName=first;
        lastName = last;
        birthday = bday;
        admin = ad;
        email = mail;
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRewardsUsed() {
        return rewardsUsed;
    }

    public void setRewardsUsed(String rewardsUsed) {
        this.rewardsUsed = rewardsUsed;
    }
}
