package com.example.admin.betty.Activity;

public class LocationDTO {

    public int userX;
    public int userY;
    public String userlocation;

    public LocationDTO() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public LocationDTO(int userX, int userY, String userlocation){
        this.userX = userX;
        this.userY = userY;
        this.userlocation = userlocation;
    }
}
