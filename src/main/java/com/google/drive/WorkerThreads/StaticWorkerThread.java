package com.google.drive.WorkerThreads;

import com.google.api.services.drive.model.PermissionList;
import com.google.common.collect.Multimap;
import com.google.drive.Reports.UpdateReport;
import com.google.drive.controller.UploadController;
import com.google.drive.maps.StaticReportErrorMap;
import com.google.drive.maps.StaticReportMap;

import java.util.ArrayList;
import java.util.List;

import static com.google.drive.Reports.StaticReport.driveservice;


public class StaticWorkerThread implements Runnable {
    private StaticReportMap elemet;
    private ArrayList<StaticReportMap> staticReportMap;
    private ArrayList<StaticReportErrorMap> staticReportErrorMap;
    private String ownersList = "";
    private String goodOwnersList = "";
    private String badOwnersList = "";
    private Boolean allEmailFromINovus = true;
    private String realOwner = "";
    private Multimap<String, String> email_exceptions;
    private Multimap<String, String> folder_exceptions;

    public StaticWorkerThread(ArrayList<StaticReportErrorMap> staticReportErrorMap,
                              ArrayList<StaticReportMap> staticReportMap,
                              StaticReportMap income_element, Multimap<String, String> email_exceptions, Multimap<String, String> folder_exceptions) {
        this.elemet = income_element;
        this.staticReportMap = staticReportMap;
        this.staticReportErrorMap = staticReportErrorMap;
        this.email_exceptions = email_exceptions;
        this.folder_exceptions = folder_exceptions;
    }

    public void run() {
        {
            try {
                PermissionList permissionList = driveservice.permissions().list(elemet.getId()).setFields("permissions(displayName, emailAddress, role)")
                        .execute();
                List<com.google.api.services.drive.model.Permission> p = permissionList.getPermissions();
                for (com.google.api.services.drive.model.Permission pe : p) {
                    if (pe.getEmailAddress() != null && pe.getEmailAddress() != "" && pe.getDisplayName() != null && pe.getDisplayName() != "") {
                        ownersList += pe.getDisplayName() + " ( " + pe.getEmailAddress() + " ) : " + pe.getRole() + "\n";
                        if (!(pe.getEmailAddress().toLowerCase().contains("@domain"))) {
                            if (!email_exceptions.containsValue(pe.getEmailAddress())) {
                                badOwnersList += pe.getEmailAddress().toString() + "\n";
                                allEmailFromINovus = false;
                            } else goodOwnersList += pe.getEmailAddress().toString() + "\n";
                        } else {
                            goodOwnersList += pe.getEmailAddress().toString() + "\n";
                        }
                        if ((pe.getRole().equals("owner"))) {
                            realOwner = pe.getDisplayName() + " ( " + pe.getEmailAddress() + " )";
                        }
                    }
                }
                this.elemet.setIdreal_owner(realOwner);
                this.elemet.setIdowners(ownersList);
                this.elemet.setGoodOwnersList(goodOwnersList);
                this.elemet.setBadOwnersList(badOwnersList);
                this.elemet.setIdInovus(allEmailFromINovus);
                staticReportMap.add(elemet);
            } catch (Exception e) {
                System.out.println("--------REST ERROR = " + e.getMessage());
                System.out.println(elemet.getId());
                System.out.println(elemet.getName());
                staticReportErrorMap.add(new StaticReportErrorMap(elemet.getId(), elemet.getName(), elemet.getFolderName()));
            }
        }
    }
}
