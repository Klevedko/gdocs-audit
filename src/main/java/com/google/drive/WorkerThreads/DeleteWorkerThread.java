package com.google.drive.WorkerThreads;

import com.google.api.services.drive.model.PermissionList;
import com.google.common.collect.Multimap;
import com.google.drive.maps.DeleteReportErrorMap;
import com.google.drive.maps.DeleteReportMap;
import com.google.drive.maps.PermissionsMap;
import com.google.drive.maps.StaticReportMap;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import static com.google.drive.App.*;
import static com.google.drive.Reports.DeleteReport.*;

public class DeleteWorkerThread implements Runnable {
    private DeleteReportMap elemet;
    private ModelAndView modelAndView;
    private ArrayList<DeleteReportMap> deleteReportMap;
    private ArrayList<DeleteReportErrorMap> deleteReportErrorMap;
    private String ownersList = "";
    private String goodOwnersList = "";
    private String badOwnersList = "";
    private Boolean allEmailFromINovus = true;
    private String realOwner = "";
    private static boolean need = false;
    public static ArrayList<PermissionsMap> permissionsMapArrayList = new ArrayList<>();
    public Multimap<String, String> email_exceptions;

    public DeleteWorkerThread(ArrayList<DeleteReportErrorMap> deleteReportErrorMap,
                              ArrayList<DeleteReportMap> deleteReportMap,
                              ModelAndView modelAndView,
                              DeleteReportMap income_element,
                              Multimap<String, String> email_exceptions) {
        this.deleteReportErrorMap = deleteReportErrorMap;
        this.deleteReportMap = deleteReportMap;
        this.elemet = income_element;
        this.modelAndView = modelAndView;
        this.email_exceptions = email_exceptions;
    }

    public synchronized void run() {
        try {
            PermissionList permissionList = driveservice.permissions().list(elemet.getId()).setFields("permissions(id, displayName, emailAddress, role)")
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
                deleteReportMap.add(elemet);
            }
            need = false;

        } catch (Exception e) {
            System.out.println("--------REST ERROR = " + e.getMessage());
            System.out.println(elemet.getId());
            System.out.println(elemet.getName());
            deleteReportErrorMap.add(new DeleteReportErrorMap(elemet.getId(), elemet.getName(), elemet.getFolderName()));
        }
    }

    public static void needToAdd(ModelAndView modelAndView, com.google.api.services.drive.model.Permission perm, DeleteReportMap elemet) {
        for (String accountElement : modelAndView.getModel().get("F_userArray").toString().split("\r\n")) {
            if (perm.getEmailAddress().toLowerCase().contains(accountElement.toLowerCase())) {
                PermissionsMap candyDel = new PermissionsMap(elemet.getId(), perm.getId(), perm.getDisplayName(), perm.getEmailAddress());
                permissionsMapArrayList.add(candyDel);
                deletePermissions(modelAndView, elemet, perm.getEmailAddress(), candyDel);
                need = true;
            }
        }
    }

    public static void deletePermissions(ModelAndView modelAndView, DeleteReportMap elemet, String permission_email, PermissionsMap candyDel) {
        for (String accountElement : modelAndView.getModel().get("F_userArray").toString().split("\r\n")) {
            try {
                if (permission_email.toLowerCase().contains(accountElement.toLowerCase())) {
                    System.out.println("----------- deleting permissions on file " + elemet.getName() + "\n  user: " + accountElement);
                    // Delete permission on a file!
                    driveservice.permissions().delete(elemet.getId(), candyDel.getPermissionId()).execute();
                    System.out.println("deleted! -----------");
                }
            } catch (Exception x) {
                System.out.println(x.getMessage());
            }
        }
    }
}