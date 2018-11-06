package maps;

import java.io.Serializable;

public class DynamicReportMap implements Serializable, Comparable<DynamicReportMap> {

    private String date;
    private String realOnwer;
    private String foldername;
    private String parentFolderId;
    private String name;
    private String webViewLink;
    private String target_name;
    private String eventAction;
    private String history;
    private String fileid;
    private String idowners;
    private String goodOwnersList;
    private String badOwnersList;
    private boolean idInovus;
    private boolean itPermissionChange;
    private String parentFolderLink;

    public DynamicReportMap(String date, String foldername, String parentFolderId, String target_name, String name, String webViewLink, String eventAction, String history, String fileid, String parentFolderLink) {
        this.date = date;
        this.foldername = foldername;
        this.parentFolderId = parentFolderId;
        this.name = name;
        this.webViewLink = webViewLink;
        this.target_name = target_name;
        this.eventAction = eventAction;
        this.history = history;
        this.fileid = fileid;
        this.parentFolderLink = parentFolderLink;
    }

    @Override
    public int compareTo(DynamicReportMap o) {
        int result = this.target_name.compareToIgnoreCase(o.target_name);
        if (result != 0) {
            return result;
        } else {
            return new String(this.target_name).compareTo(new String(o.target_name));
        }
    }

    public String getParentFolderLink() {
        return parentFolderLink;
    }

    public void setParentFolderLink(String parentFolderLink) {
        this.parentFolderLink = parentFolderLink;
    }

    public String getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(String parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget_name() {
        return target_name;
    }

    public void setTarget_name(String target_name) {
        this.target_name = target_name;
    }

    public String getEventAction() {
        return eventAction;
    }

    public void setEventAction(String eventAction) {
        this.eventAction = eventAction;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
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

    public String getFileid() {
        return fileid;
    }

    public void setFileid(String fileid) {
        this.fileid = fileid;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    public String getRealOnwer() {
        return realOnwer;
    }

    public void setRealOnwer(String realOnwer) {
        this.realOnwer = realOnwer;
    }

    public boolean isItPermissionChange() {
        return itPermissionChange;
    }

    public void setItPermissionChange(boolean itPermissionChange) {
        this.itPermissionChange = itPermissionChange;
    }

    public String getFoldername() {
        return foldername;
    }

    public void setFoldername(String foldername) {
        this.foldername = foldername;
    }

    @Override
    public boolean equals(Object obj) {
        DynamicReportMap that = (DynamicReportMap) obj;
        if (!(this.name.equals(that.name))
                || !(this.target_name.equals(that.target_name))
                || !(this.date.equals(that.date))
                || !(this.eventAction.equals(that.eventAction))
                || !(this.history.equals(that.history))
                ) return false;
        return true;
    }

    @Override
    //this is required to print the user friendly information about the maps.DynamicReportMap
    public String toString() {
        return "[date=" + this.date + ", name=" + this.name + ", target_name=" + this.target_name + ", eventAction=" +
                this.eventAction + ", history=" + this.history + "]";
    }
}