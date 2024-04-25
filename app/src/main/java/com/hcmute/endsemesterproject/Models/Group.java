package com.hcmute.endsemesterproject.Models;

import java.io.Serializable;

public class Group implements Serializable {
    private String name;
    private String description;
    private String ownerId;
    private long numberOfMembers;
    private boolean isPublic;

    public Group() {

    }

    public Group(String name, String description, int numberOfMembers, boolean isPublic, String ownerId) {
        this.name = name;
        this.description = description;
        this.numberOfMembers = numberOfMembers;
        this.isPublic = isPublic;
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getNumberOfMembers() {
        return numberOfMembers;
    }

    public void setNumberOfMembers(long numberOfMembers) {
        this.numberOfMembers = numberOfMembers;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
