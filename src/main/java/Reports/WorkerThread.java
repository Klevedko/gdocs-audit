package Reports;

import Quartz.CronBuild;
import com.google.api.services.drive.model.PermissionList;
import maps.StaticReportMap;

import java.util.List;

import static Reports.StaticReport.*;

class WorkerThread implements Runnable {
    private StaticReportMap elemet;
    private boolean fail=false;
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
            System.out.println("starting new thread WITH NAME= " + elemet.getName());
            try {
                PermissionList permissionList = driveservice.permissions().list(elemet.getId()).setFields("permissions(displayName, emailAddress, role)")
                        .execute();
                List<com.google.api.services.drive.model.Permission> p = permissionList.getPermissions();
                for (com.google.api.services.drive.model.Permission pe : p) {
                    if (pe.getEmailAddress() != null && pe.getEmailAddress() != "" && pe.getDisplayName() != null && pe.getDisplayName() != "") {
                        ownersList += pe.getDisplayName() + " ( " + pe.getEmailAddress() + " ) : " + pe.getRole() + "\n";
                        if (!(pe.getEmailAddress().toLowerCase().contains("@i-novus"))) {
                            if (!CronBuild.email_exceptions.containsValue(pe.getEmailAddress())) {
                                badOwnersList += pe.getEmailAddress().toString() + "\n";
                                allEmailFromINovus = false;
                            }
                            // add @serviceaccount  to goodOwnersList
                            else goodOwnersList += pe.getEmailAddress().toString() + "\n";
                        }
                        // add i-novus to goodOwnersList
                        else {
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
            } catch (Exception e) {
                System.out.println("--------REST ERROR = " + e.getMessage());
                // if there were no permission for account - we delete it from map
                System.out.println(elemet.getId());
                fail = true;
                staticReportMap.remove(this.elemet);
            } finally {
                if(this.fail) {
                    staticReportMap.remove(this.elemet);
                }
            }
            System.out.println("DONE thread WITH NAME= " + elemet.getName());
        }
    }
}