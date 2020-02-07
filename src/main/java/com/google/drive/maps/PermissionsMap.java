package com.google.drive.maps;

public class PermissionsMap {
    private String objectId;
    private String permissionId;
    private String userName;
    private String userMail;

    public PermissionsMap(String objectId, String permissionId, String userName, String userMail) {
        this.objectId = objectId;
        this.permissionId = permissionId;
        this.userName = userName;
        this.userMail = userMail;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }
}
