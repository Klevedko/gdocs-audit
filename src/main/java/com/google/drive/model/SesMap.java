package com.google.drive.model;

import org.springframework.stereotype.Component;


@Component
public class SesMap {

    private String scanFolderId;
    private String[] usersArray;
    private String scanFolderIdOutput;

    public String getScanFolderId() {
        return scanFolderId;
    }

    public void setScanFolderId(String scanFolderId) {
        this.scanFolderId = scanFolderId;
    }

    public String[] getUsersArray() {
        return usersArray;
    }

    public void setUsersArray(String[] usersArray) {
        this.usersArray = usersArray;
    }

    public String getScanFolderIdOutput() {
        return scanFolderIdOutput;
    }

    public void setScanFolderIdOutput(String scanFolderIdOutput) {
        this.scanFolderIdOutput = scanFolderIdOutput;
    }
}
