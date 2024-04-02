package com.hcmute.endsemesterproject.Models;

public class Contacts {
    private String name, status, image;
    private String id;
    public Contacts()
    {

    }

    public Contacts(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
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

    public void setId(String id) { this.id = id; }
    public String getId() { return this.id; }
}
