package Reports;

import api.Google.CreateGoogleFile;
import api.authorize.Apiv1;
import api.authorize.Apiv3;
import com.google.api.services.appsactivity.Appsactivity;
import com.google.api.services.appsactivity.model.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.PermissionList;
import maps.DynamicReportMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.stream.IntStream;

import static Quartz.CronBuild.email_exceptions;
import static Quartz.CronBuild.folder_exceptions;
import static Quartz.CronBuild.startFolderId;

public class DynamicReport implements Job {
    public static String querry_deeper = "";
    public static String history = "";
    public static String historyAdd = "";
    public static String historyDel = "";
    public static String historyRem = "";
    public static JSONArray geodata;
    public static String resultfiletemplate = "Dynamic_audit_result_";
    public static String resultfile = "";
    public static ArrayList<DynamicReportMap> resultMap = new ArrayList<DynamicReportMap>();
    public static String evlist_string = "";
    public static Drive driveservice;
    public static Appsactivity service;
    public static long oldMS;
    public static Boolean running = false;

    static {
        try {
            service = Apiv1.getAppsactivityService();
            driveservice = Apiv3.Drive();
        } catch (Exception e) {
        }
    }

    public void execute(JobExecutionContext context) {
        // блок для CRON - не запускаем, пока не выполнился предыдущий шаг
        if (running) {
            return;
        }
        // запустили
        running = true;
        try {
            oldMS = periodForActivity();
            System.out.println("------------------------ DYNAMIC RUN ------------------------ ");
            System.out.println("start " + new Date());
            FileList fileList = get_driveservice_v3_files("'" + startFolderId + "'  in parents and trashed=false");
            List<File> listFile = fileList.getFiles();
            deeper_in_folders("PROJECT", "root", listFile, "project link");
            Collections.sort(resultMap);
            prepare_to_write(resultMap);
            System.out.println("end " + new Date());
            CreateGoogleFile.main(resultfile);
            System.out.println("ENDsize=" + resultMap.size());
            resultMap.clear();
        } catch (Exception exec) {
            System.out.println("execute =" + exec);
        }
    }

    public static void activityForFile(String fileid, String link, String foldername, String parentFolderId, String parentFolderLink) {
        ListActivitiesResponse result = get_driveservice_v1_activities(fileid);
        List<Activity> activities = result.getActivities();
        if (activities == null || activities.size() == 0) {
            System.out.println("No activity.");
        } else {
            read_activities(activities, link, foldername, parentFolderId, parentFolderLink);
        }
    }

    public static FileList get_driveservice_v3_files(String query) {
        try {
            return driveservice.files().list().setQ(query).setFields("nextPageToken, " +
                    "files(id, parents, name, webViewLink, mimeType)").execute();
            //, sharingUser(emailAddress, permissionId)
        } catch (Exception x) {
            throw new RuntimeException("Cannot get_driveservice_v3_files = ", x);
        }
    }

    public static void deeper_in_folders(String FolderName, String parentFolderId, List<File> file, String parentFolderLink) {
        for (File f : file) {
            try {
                if (!folder_exceptions.containsValue(f.getId())) {
                    if (f.getMimeType().equals("application/vnd.google-apps.folder") || f.getMimeType().equals("folder")) {
                        querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                        deeper_in_folders(FolderName.concat("/".concat(f.getName())), f.getId(), get_driveservice_v3_files(querry_deeper).getFiles(), f.getWebViewLink());
                    } else {
                        activityForFile(f.getId(), f.getWebViewLink(), FolderName, parentFolderId, parentFolderLink);
                    }
                }
            } catch (Exception ss) {
                System.out.println("deeper_in_folders = " + f.getName() + ss);
            }
        }
    }

    public static ListActivitiesResponse get_driveservice_v1_activities(String query) {
        try {
            return service.activities().list().setSource("drive.google.com").setDriveAncestorId(query).execute();
        } catch (Exception x) {
            throw new RuntimeException("get_driveservice_v1_activities =", x);
        }
    }

    public static void read_activities(List<Activity> activities, String link, String foldername, String parentFolderId, String parentFolderLink) {
        try {
            for (Activity activity : activities) {
                // Get Event for every Activity
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
                            addedDeletedRemovedPermissions(evlist_string);
                        } else history = "";
                        DynamicReportMap candy = new DynamicReportMap(date, foldername, target.getId(), target.getName(), user.getName(), e.getPrimaryEventType(), history, target.getId(), parentFolderLink);
                        // if it is not PermissionChange, we will show it in wb list2
                        candy.setItPermissionChange(e.getPrimaryEventType().equals("permissionChange") ? true : false);
                        resultMap.add(candy);
                        candy.setWebViewLink(link);
                        read_editors(candy);
                    }
                }
                history = historyDel = historyAdd = historyRem = "";
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void addedDeletedRemovedPermissions(String evlist_string) {
        JSONObject obj = new JSONObject(evlist_string);
        try {
            geodata = obj.getJSONArray("addedPermissions");
            historyAdd = "addedPermissions:\n" + getHistory(geodata);
        } catch (Exception e) {
        }
        try {
            geodata = obj.getJSONArray("deletedPermissions");
            historyDel = "deletedPermissions:\n" + getHistory(geodata);
        } catch (Exception e) {
        }
        try {
            geodata = obj.getJSONArray("removedPermissions");
            historyRem = "removedPermissions:\n" + getHistory(geodata);
        } catch (Exception e) {
        }
        history = historyAdd.concat(historyDel.concat(historyRem));
    }

    public static void read_editors(DynamicReportMap elemet) {
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
                    if (!(pe.getEmailAddress().toLowerCase().contains("@i-novus"))) {
                        if (!email_exceptions.containsValue(pe.getEmailAddress())) {
                            badOwnersList += pe.getEmailAddress().toString() + "\n";
                            allEmailFromINovus = false;
                        }
                        // add @amiable-crane-213912  to goodOwnersList
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
            //elemet.setRealOnwer(realOwner);
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
            his += "   " + (person.has("name") ? person.getString("name") : person.getString("permissionId")) + ": " + person.getString("role") +"\n";
        }
        return his;
    }

    public static Long periodForActivity() throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_WEEK, -7);
        long x = cal.getTimeInMillis();
        return x;
    }

    public static void prepare_to_write(ArrayList<DynamicReportMap> resultMap) {
        try {
            System.out.println("writing to the dynamic file .......");
            String audit_date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            resultfile = resultfiletemplate.concat(audit_date.concat(".xlsx"));
            XSSFWorkbook wb = new XSSFWorkbook();
            String output = resultfile;
            FileOutputStream fileout;
            fileout = new FileOutputStream(output);
            CellStyle cs = wb.createCellStyle();
            cs.setWrapText(true);

            Sheet list1 = wb.createSheet("Общий список действий");
            Sheet list2 = wb.createSheet("Изменения прав");
            Sheet list3 = wb.createSheet("Действия над файлами");
            System.out.println("create_columns1");
            create_columns(list1);
            System.out.println("create_columns2");
            create_columns(list2);
            System.out.println("create_columns3");
            create_columns(list3);
            System.out.println("write!");
            int row1 = 1;
            int row2 = 1;
            int row3 = 1;
            for (DynamicReportMap product : resultMap) {
                //  Write all actions on list 1
                write(wb, cs, list1, row1, product);
                row1++;
                //  Write PermissionChanges on list2
                if (product.isItPermissionChange()) {
                    write(wb, cs, list2, row2, product);
                    row2++;
                }
                //  Write others actions on list3
                if (!product.isItPermissionChange()) {
                    write(wb, cs, list3, row3, product);
                    row3++;
                }
            }
            wb.write(fileout);
            fileout.close();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public static void write(XSSFWorkbook wb, CellStyle cs, Sheet x, Integer y, DynamicReportMap product) {
        Row dataRow;
        Cell cell;
        dataRow = x.createRow(y);
        dataRow.setHeight((short)811);
        cell = dataRow.createCell(0);
        cell.setCellStyle(cs);
        cell.setCellValue(product.getDate());

        cell = dataRow.createCell(1);
        cell.setCellStyle(cs);
        cell.setCellValue(product.getFoldername());

        cell = dataRow.createCell(2);
        cell.setCellStyle(cs);
        CreationHelper createHelper = wb.getCreationHelper();
        Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
        CellStyle hlink_style = wb.createCellStyle();
        link.setAddress(product.getWebViewLink());
        //cell.setCellStyle(hlink_style);
        cell.setCellValue(product.getTarget_name());
        cell.setCellStyle(cs);
        if (!product.getWebViewLink().equals(""))
            cell.setHyperlink(link);

        cell = dataRow.createCell(3);
        cell.setCellStyle(cs);
        cell.setCellValue(product.getName());

        cell = dataRow.createCell(4);
        cell.setCellStyle(cs);
        cell.setCellValue(product.getEventAction());

        cell = dataRow.createCell(5);
        cell.setCellStyle(cs);
        cell.setCellValue(product.getHistory());

        cell = dataRow.createCell(6);
        cell.setCellStyle(cs);
        cell.setCellValue(product.getIdowners());

        cell = dataRow.createCell(7);
        cell.setCellStyle(cs);

        cell.setCellValue(product.getGoodOwnersList());

        cell = dataRow.createCell(8);
        //cell.setCellStyle(cs)
        cell.setCellValue(product.getBadOwnersList());
    }

    public static void create_columns(Sheet x) {
        try {
            int row = 0;
            Cell cell;
            Row dataRow = x.createRow(row);
            cell = dataRow.createCell(0);
            cell.setCellValue("Дата");
            cell = dataRow.createCell(1);
            cell.setCellValue("Путь");
            cell = dataRow.createCell(2);
            cell.setCellValue("Файл");
            cell = dataRow.createCell(3);
            cell.setCellValue("Кто");
            cell = dataRow.createCell(4);
            cell.setCellValue("Действие");
            cell = dataRow.createCell(5);
            cell.setCellValue("Изменения");
            cell = dataRow.createCell(6);
            cell.setCellValue("Общий список прав");
            cell = dataRow.createCell(7);
            cell.setCellValue("Доступ сотрудников АН");
            cell = dataRow.createCell(8);
            cell.setCellValue("Доступ сторонних сотрудников");
            IntStream.range(4, 9).forEach((columnIndex) -> x.setColumnWidth(columnIndex, 6400));
            x.setColumnWidth(0, 2700);
            x.setColumnWidth(1, 10000);
            x.setColumnWidth(2, 10000);
            x.setColumnWidth(3, 3800);
            x.setColumnWidth(4, 4300);
            x.setColumnWidth(5, 10000);
            x.setColumnWidth(6, 14400);
            x.setColumnWidth(7, 14400);
            row++;
        } catch (Exception create_columns) {
            System.out.println("create_columns" + create_columns);
        }
    }
}