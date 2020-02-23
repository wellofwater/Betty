package com.example.admin.betty.Activity;

public class User {

    public String userlocation;
    public boolean userpush;
    public int userhour;
    public int userminute;
    public String userweek;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userlocation, boolean userpush, int userhour, int userminute, String userweek){
        this.userlocation = userlocation;
        this.userpush = userpush;
        this.userhour = userhour;
        this.userminute = userminute;
        this.userweek = userweek;
    }
}
