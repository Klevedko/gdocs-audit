package com.google.drive.Reports;

import com.google.api.services.appsactivity.Appsactivity;
import com.google.api.services.appsactivity.model.*;
import com.google.api.services.appsactivity.model.User;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.*;

import com.google.common.collect.Multimap;
import com.google.drive.api.Google.CreateGoogleFile;
import com.google.drive.api.authorize.Apiv1;
import com.google.drive.api.authorize.Apiv3;
import com.google.drive.maps.DynamicReportMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

import static com.google.drive.App.*;
import static com.google.drive.Writers.DynamicWriter.prepare_to_write;
import static com.google.drive.api.authorize.Apiv1.get_driveservice_v1_activities;
import static com.google.drive.api.authorize.Apiv3.get_driveservice_v3_files;

public class DynamicReport {
    public static String querry_deeper = "";
    public static String history = "";
    public static String historyAdd = "";
    public static String historyDel = "";
    public static String historyRem = "";
    public static JSONArray geodata;
    public static String resultfiletemplate = "Dynamic_audit_result_";
    public static String resultfile = "";
    //public static ArrayList<DynamicReportMap> resultMap = new ArrayList<>();
    public static String evlist_string = "";
    public static Drive driveservice;
    public static Appsactivity service;
    public static long oldMS;


    public static void main(ModelAndView modelAndView, Multimap<String, String> email_exceptions, Multimap<String, String> folder_exceptions) {
        try {
            ArrayList<DynamicReportMap> resultMap = new ArrayList<>();
            driveservice = Apiv3.Drive();
            service = Apiv1.getAppsactivityService();
            oldMS = periodForActivity();
            System.out.println("------------------------ DYNAMIC RUN ------------------------ ");
            System.out.println("start " + new Date());
            FileList fileList = get_driveservice_v3_files(driveservice, "'" + modelAndView.getModel().get("F_scanFolderId").toString() + "'  in parents and trashed=false");
            List<File> listFile = fileList.getFiles();
            deeper_in_folders(resultMap, "PROJECT", "root", listFile, "project link", email_exceptions, folder_exceptions);
            Collections.sort(resultMap);
            prepare_to_write(resultMap);
            CreateGoogleFile.main(modelAndView, resultfile);
            System.out.println("end " + new Date());
        } catch (Exception exec) {
            System.out.println("execute =" + exec);
        }
    }

    public static void deeper_in_folders(ArrayList<DynamicReportMap> resultMap, String FolderName, String parentFolderId, List<File> file, String parentFolderLink, Multimap<String, String> email_exceptions, Multimap<String, String> folder_exceptions) {
        for (File f : file) {
            try {
                if (!folder_exceptions.containsValue(f.getId())) {
                    if (f.getMimeType().equals("application/vnd.google-apps.folder") || f.getMimeType().equals("folder")) {
                        querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                        deeper_in_folders(resultMap, FolderName.concat("/".concat(f.getName())), f.getId(), get_driveservice_v3_files(driveservice, querry_deeper).getFiles(), f.getWebViewLink(), email_exceptions, folder_exceptions);
                    } else {
                        activityForFile(resultMap, f.getId(), f.getWebViewLink(), FolderName, parentFolderId, parentFolderLink, email_exceptions);
                    }
                }
            } catch (Exception ss) {
                System.out.println("deeper_in_folders = " + f.getName() + ss);
            }
        }
    }

    public static void activityForFile(ArrayList<DynamicReportMap> resultMap, String fileid, String link, String foldername, String parentFolderId, String parentFolderLink, Multimap<String, String> email_exceptions) {
        ListActivitiesResponse result = get_driveservice_v1_activities(fileid);
        List<Activity> activities = result.getActivities();
        if (activities == null || activities.size() == 0) {
            System.out.println("No activity.");
        } else {
            read_activities(resultMap, activities, link, foldername, parentFolderId, parentFolderLink, email_exceptions);
        }
    }

    public static void read_activities(ArrayList<DynamicReportMap> resultMap, List<Activity> activities, String link, String foldername, String parentFolderId, String parentFolderLink, Multimap<String, String> email_exceptions) {
        try {
            for (Activity activity : activities) {
                List<Event> eventList = activity.getSingleEvents();
                for (Event e : eventList) {
                    if (e.getEventTimeMillis().longValue() >= oldMS) {
                        User user = e.getUser();
                        Target target = e.getTarget();
                        String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(e.getEventTimeMillis().longValue()));
                        if (user == null || target == null)
                            continue;
                        //System.out.printf(target.getId() + " %s: %s. FILE: %s,  ACTION: %s. GETPERMISSIONCHANGES_JSON %s\n", date, user.getName(), target.getName(), e.getPrimaryEventType(), e.getPermissionChanges());
                        List<PermissionChange> evlist = e.getPermissionChanges();
                        evlist_string = "";
                        // If Activity has JSON != NULL
                        if (!(evlist == null)) {
                            for (PermissionChange permissionChange : evlist) {
                                evlist_string = evlist_string + permissionChange;
                            }
                            // Getting good String from JSON's parts
                            addedDeletedRemovedPermissions(evlist_string, email_exceptions);
                        } else history = "";
                        DynamicReportMap candy = new DynamicReportMap(date, foldername, target.getId(), target.getName(), user.getName(), link,
                                e.getPrimaryEventType(), history, target.getId(), parentFolderLink);
                        // if it is not PermissionChange, we will show it in wb list2
                        candy.setItPermissionChange(e.getPrimaryEventType().equals("permissionChange") ? true : false);
                        resultMap.add(candy);
                        read_editors(resultMap, candy, email_exceptions);
                    }
                }
                history = historyDel = historyAdd = historyRem = "";
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void addedDeletedRemovedPermissions(String evlist_string, Multimap<String, String> email_exceptions) {
        JSONObject obj = new JSONObject(evlist_string);
        String[] perms = new String[]{"addedPermissions", "deletedPermissions", "removedPermissions"};
        for (String x : perms) {
            try {
                geodata = obj.getJSONArray(x);
                historyAdd = x + ":\n" + getHistory(geodata);
                history = historyAdd;
            } catch (Exception t) {
                System.out.println("----->" + t.getMessage());
            }
        }
    }

    public static void read_editors(ArrayList<DynamicReportMap> resultMap, DynamicReportMap elemet, Multimap<String, String> email_exceptions) {
        String ownersList = "";
        String goodOwnersList = "";
        String badOwnersList = "";
        Boolean allEmailFromINovus = true;
        String realOwner = "";
        try {
            PermissionList permissionList = driveservice.permissions().list(elemet.getFileid()).setFields("permissions(id, displayName, emailAddress, role)")
                    .execute();
            List<com.google.api.services.drive.model.Permission> p = permissionList.getPermissions();
            for (com.google.api.services.drive.model.Permission pe : p) {
                ownersList += pe.getDisplayName() + " ( " + pe.getEmailAddress() + " ) : " + pe.getRole() + "\n";
                if (pe.getEmailAddress() != null) {
                    if (!(pe.getEmailAddress().toLowerCase().contains("@domain"))) {
                        if (!email_exceptions.containsValue(pe.getEmailAddress())) {
                            badOwnersList += pe.getEmailAddress().toString() + "\n";
                            allEmailFromINovus = false;
                        } else goodOwnersList += pe.getEmailAddress().toString() + "\n";
                    } else {
                        goodOwnersList += pe.getEmailAddress().toString() + "\n";
                    }
                    if ((pe.getRole().equals("owner"))) {
                        // forget it
                        realOwner = pe.getDisplayName() + " ( " + pe.getEmailAddress() + " )";
                    }
                }
            }
            elemet.setIdowners(ownersList);
            elemet.setGoodOwnersList(goodOwnersList);
            elemet.setBadOwnersList(badOwnersList);
            elemet.setIdInovus(allEmailFromINovus);
        } catch (Exception e) {
            System.out.println("read_editors = " + e);
            resultMap.remove(elemet);
        }
    }

    public static String getHistory(JSONArray geodata) {
        String his = "";
        for (int i = 0; i < geodata.length(); ++i) {
            JSONObject person = geodata.getJSONObject(i);
            his += "   " + (person.has("name") ? person.getString("name")
                    : person.getString("permissionId")) + ": " + person.getString("role") + "\n";
        }
        return his;
    }

    public static Long periodForActivity() throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_WEEK, -7);
        return cal.getTimeInMillis();
    }
}
