package com.google.drive.maps;

import java.io.Serializable;

public class UpdateReportMap implements Serializable, Comparable<UpdateReportMap> {
    private String id;
    private String folderName;
    private String name;
    private String parentFolderId;
    private String webViewLink;
    private String idreal_owner;
    private String idowners;
    private String goodOwnersList;
    private String badOwnersList;
    private String[] sss;

    private boolean idInovus;

    public UpdateReportMap(String id, String folderName, String name, String webViewLink, String parentFolderId) {
        this.id = id;
        this.folderName = folderName;
        this.name = name;
        this.webViewLink = webViewLink;
        this.parentFolderId = parentFolderId;
    }

    public String getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(String parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    public String getIdreal_owner() {
        return idreal_owner;
    }

    public void setIdreal_owner(String idreal_owner) {
        this.idreal_owner = idreal_owner;
    }

    public String getIdowners() {
        return idowners;
    }

    public void setIdowners(String idowners) {
        this.idowners = idowners;
    }

    public String getGoodOwnersList() {
        return goodOwnersList;
    }

    public void setGoodOwnersList(String goodOwnersList) {
        this.goodOwnersList = goodOwnersList;
    }

    public String getBadOwnersList() {
        return badOwnersList;
    }

    public void setBadOwnersList(String badOwnersList) {
        this.badOwnersList = badOwnersList;
    }

    public boolean isIdInovus() {
        return idInovus;
    }

    public void setIdInovus(boolean idInovus) {
        this.idInovus = idInovus;
    }

    public Boolean getIdInovus() {
        return idInovus;
    }

    public void setIdInovus(Boolean idInovus) {
        this.idInovus = idInovus;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String[] getSss() {
        return sss;
    }


    @Override
    public boolean equals(Object obj) {
        UpdateReportMap that = (UpdateReportMap) obj;
        if (!(this.folderName.equals(that.folderName))
        ) return false;
        return true;
    }

    @Override
    //this is required to print the user friendly information about the com.google.drive.Reports.maps.DynamicReportMap
    public String toString() {
        return "[date=" + this.folderName + "]";
    }

    @Override
    public int compareTo(UpdateReportMap o) {
        int result = this.folderName.compareToIgnoreCase(o.folderName);
        if (result != 0) {
            return result;
        } else {
            return new String(this.folderName).compareTo(new String(o.folderName));
        }
    }
}
