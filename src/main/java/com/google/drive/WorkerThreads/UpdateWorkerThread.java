package com.google.drive.WorkerThreads;


import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.google.common.collect.Multimap;
import com.google.drive.Reports.UpdateReport;
import com.google.drive.maps.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import static com.google.drive.Reports.UpdateReport.driveservice;

public class UpdateWorkerThread implements Runnable {
    private UpdateReportMap elemet;
    private ModelAndView modelAndView;
    private ArrayList<UpdateReportMap> updateReportMap;
    private ArrayList<UpdateReportErrorMap> updateReportErrorMap;
    private String ownersList = "";
    private String goodOwnersList = "";
    private String badOwnersList = "";
    private Boolean allEmailFromINovus = true;
    private String realOwner = "";
    private static boolean need = false;
    public static ArrayList<PermissionsMap> permissionsMapArrayList = new ArrayList<>();
    public Multimap<String, String> email_exceptions;

    public UpdateWorkerThread(ArrayList<UpdateReportErrorMap> updateReportErrorMap,
                              ArrayList<UpdateReportMap> updateReportMap,
                              ModelAndView modelAndView,
                              UpdateReportMap income_element,
                              Multimap<String, String> email_exceptions) {
        this.updateReportErrorMap = updateReportErrorMap;
        this.updateReportMap = updateReportMap;
        this.elemet = income_element;
        this.modelAndView = modelAndView;
        this.email_exceptions = email_exceptions;
    }

    public synchronized void run() {
        try {
            PermissionList permissionList = driveservice.permissions().list(elemet.getId()).setFields("permissions(id, displayName, emailAddress, role, type)")
                    .execute();
            // get permissions of the FILE
            List<com.google.api.services.drive.model.Permission> permissionsList = permissionList.getPermissions();
            for (com.google.api.services.drive.model.Permission pe : permissionsList) {
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
                needToAdd(modelAndView, pe, elemet);
            }
            if (need) {
                this.elemet.setIdreal_owner(realOwner);
                this.elemet.setIdowners(ownersList);
                this.elemet.setGoodOwnersList(goodOwnersList);
                this.elemet.setBadOwnersList(badOwnersList);
                this.elemet.setIdInovus(allEmailFromINovus);
                updateReportMap.add(elemet);
            }
            need = false;

        } catch (Exception e) {
            System.out.println("--------REST ERROR = " + e.getMessage());
            System.out.println(elemet.getId());
            System.out.println(elemet.getName());
            updateReportErrorMap.add(new UpdateReportErrorMap(elemet.getId(), elemet.getName(), elemet.getFolderName()));
        }
    }

    public static void deletePermissions(ModelAndView modelAndView, UpdateReportMap elemet, String permission_email, PermissionsMap candyDel) {
        for (String accountElement : modelAndView.getModel().get("F_userArray").toString().split("\r\n")) {
            try {
                if (permission_email.toLowerCase().contains(accountElement.toLowerCase())) {
                    System.out.println("----------- we are deleting permissions on file " + elemet.getName() + "\n  user: " + accountElement);
                    // Delete permission on a file!
                    UpdateReport.driveservice.permissions().delete(elemet.getId(), candyDel.getPermissionId()).execute();
                    System.out.println("deleted! -----------");
                }
            } catch (Exception x) {
                System.out.println(x.getMessage());
            }
        }
    }

    public static void createPermission(ModelAndView modelAndView, UpdateReportMap elemet, Permission perm, PermissionsMap candyUpd) {
        for (String accountElement : modelAndView.getModel().get("F_userArray").toString().split("\r\n")) {
            try {
                if (perm.getEmailAddress().toLowerCase().contains(accountElement.toLowerCase())) {
                    System.out.println("----------- we are Creating permissions on file " + elemet.getName() + "\n  user: " + accountElement);
                    // Create permission on a file!
                    Permission permission = new Permission();
                    permission.setType("user");
                    permission.setRole(modelAndView.getModel().get("F_userNewPermission").toString());
                    permission.setEmailAddress(candyUpd.getUserMail());
                    UpdateReport.driveservice.permissions().create(elemet.getId(), permission).execute();
                    System.out.println("Created permission! -----------");
                }
            } catch (Exception x) {
                System.out.println(x.getMessage());
            }
        }
    }

    public static void needToAdd(ModelAndView modelAndView, com.google.api.services.drive.model.Permission perm, UpdateReportMap elemet) {
        for (String accountElement : modelAndView.getModel().get("F_userArray").toString().split("\r\n")) {
            if (perm.getEmailAddress().toLowerCase().contains(accountElement.toLowerCase())) {
                System.out.println("contains!");
                PermissionsMap candyUpd = new PermissionsMap(elemet.getId(), perm.getId(), perm.getDisplayName(), perm.getEmailAddress());
                permissionsMapArrayList.add(candyUpd);
                deletePermissions(modelAndView, elemet, perm.getEmailAddress(), candyUpd);
                createPermission(modelAndView, elemet, perm, candyUpd);
                //updatePermissions(modelAndView, elemet, perm, candyUpd);
                need = true;
            }
        }
    }
}