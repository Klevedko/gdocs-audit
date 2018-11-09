package Reports;

import api.authorize.Apiv3;
import api.Google.CreateGoogleFile;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import maps.StaticReportErrorMap;
import maps.StaticReportMap;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static Writers.StaticWriter.write_errors_to_file;
import static Writers.StaticWriter.write_to_file;
import static api.authorize.Apiv3.get_driveservice_v3_files;

import static Reports.App.folder_exceptions;
import static Reports.App.startFolderId;

public class StaticReport {

    public static ArrayList<StaticReportMap> staticReportMap = new ArrayList<>();
    public static ArrayList<StaticReportErrorMap> staticReportErrorMap = new ArrayList<>();
    public static String result_file_template = "Static_audit_result_";
    public static String error_result_file_template = "Static_errors_";
    public static String result_file = "";
    public static FileOutputStream fileout;
    public static String querry_deeper = "";
    public static Drive driveservice;
    public static ExecutorService executor = Executors.newFixedThreadPool(6);
    public static List<Future<?>> futures = new ArrayList<>();
    public static StaticReportMap candy;

    public static void main(String[] args) {
        try {
            driveservice=Apiv3.Drive();
            String query = "'" + startFolderId + "'  in parents and trashed=false";
            System.out.println("------------------------ STATIC RUN ------------------------ ");
            System.out.println("start " + new Date());
            FileList fileList = get_driveservice_v3_files(driveservice,query);
            List<File> listFile = fileList.getFiles();
            deeper_in_folders("PROJECTS", "root", listFile);
            for (Future<?> feature : futures) {
                while (!feature.isDone())
                    TimeUnit.SECONDS.sleep(1);
                feature.get();
            }
            Collections.sort(staticReportMap);
            Thread.sleep(400);
            write_to_file(staticReportMap);
            write_errors_to_file(staticReportErrorMap);
            CreateGoogleFile.main(result_file);
            staticReportMap.clear();
            System.out.println("end " + new Date());
        } catch (Exception exec) {
            System.out.println(exec);
        }
    }

    public static void deeper_in_folders(String folderName, String parentFolderID, List<File> file) {
        for (File f : file) {
            try {
                if (!folder_exceptions.containsValue(f.getId())) {
                    if (f.getMimeType().equals("application/vnd.google-apps.folder") || f.getMimeType().equals("folder")) {
                        querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                        deeper_in_folders(folderName.concat("/".concat(f.getName())), f.getId(), get_driveservice_v3_files(driveservice,querry_deeper).getFiles());
                    } else {
                        //System.out.println(f.getName());
                        candy = new StaticReportMap(f.getId(), folderName, f.getName(), f.getWebViewLink(), parentFolderID);
                        Runnable worker = new WorkerThread(candy);
                        Thread.sleep(140);
                        futures.add(executor.submit(worker));
                    }
                }
            } catch (Exception ss) {
                System.out.println("deeper_in_folders = " + f.getName() + ss);
            }
        }
    }
}