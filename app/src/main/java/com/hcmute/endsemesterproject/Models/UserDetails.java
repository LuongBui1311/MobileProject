package com.hcmute.endsemesterproject.Models;

import java.io.Serializable;

public class UserDetails implements Serializable {
    private String uid;
    private String name;
    private String status;
    private String image;
    private String deviceToken;

    // Constructors, getters, and setters
    public UserDetails() {
        // Default constructor required for Firebase
    }

    public UserDetails(String uid, String name, String status, String image, String deviceToken) {
        this.uid = uid;
        this.name = name;
        this.status = status;
        this.image = image;
        this.deviceToken = deviceToken;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

}
