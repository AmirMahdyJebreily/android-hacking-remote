package com.mehrab.android.Models;

public class ContactModel {
    public String id = "";
    public String name = "";
    public String phoneNumber = "";

    public ContactModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}