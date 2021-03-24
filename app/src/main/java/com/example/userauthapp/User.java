package com.example.userauthapp;

public class User {
    private String fullname;
    private String email;
    private String phoneNumber;

    public User() {

    }

    public User(String fullname, String email, String phoneNumber) {
        this.fullname = fullname;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
