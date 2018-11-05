package Reports;

import com.google.api.services.drive.model.PermissionList;
import maps.StaticReportErrorMap;
import maps.StaticReportMap;

import java.util.List;

import static Reports.StaticReport.*;
import static Reports.App.email_exceptions;

class WorkerThread implements Runnable {
    private StaticReportMap elemet;
    private String ownersList = "";
    private String goodOwnersList = "";
    private String badOwnersList = "";
    private Boolean allEmailFromINovus = true;
    private String realOwner = "";

    public WorkerThread(StaticReportMap income_element) {
        this.elemet = income_element;
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
                        if (!(pe.getEmailAddress().toLowerCase().contains("@i-novus"))) {
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
