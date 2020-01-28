package com.example.barfinder;

public class ModelBar {
    String Address, Name;
    Double Latitude, Longitude;

    public ModelBar(){
    }

    public ModelBar(String address, Double latitude, Double longitude, String name) {
        Address = address;
        Latitude = latitude;
        Longitude = longitude;
        Name = name;
    }

    public String getAddress() {
        return Address;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public String getName() {
        return Name;
    }
}
