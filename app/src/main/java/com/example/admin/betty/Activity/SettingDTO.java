package com.example.admin.betty.Activity;

public class SettingDTO {
    public int userhour;
    public int userminute;
    public boolean userpush;
    public String userweek;

    public SettingDTO() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public SettingDTO(int userhour, int userminute, boolean userpush, String userweek){
        this.userhour = userhour;
        this.userminute = userminute;
        this.userpush = userpush;
        this.userweek = userweek;
    }
}
