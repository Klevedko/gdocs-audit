package com.google.drive.Reports;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.collect.Multimap;
import com.google.drive.WorkerThreads.StaticWorkerThread;
import com.google.drive.api.Google.CreateGoogleFile;
import com.google.drive.api.authorize.Apiv3;
import com.google.drive.maps.StaticReportErrorMap;
import com.google.drive.maps.StaticReportMap;
import org.springframework.web.servlet.ModelAndView;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static com.google.drive.Writers.StaticWriter.write_errors_to_file;
import static com.google.drive.Writers.StaticWriter.write_to_file;
import static com.google.drive.api.authorize.Apiv3.get_driveservice_v3_files;


public class StaticReport {

    public static String result_file_template = "Static_audit_result_";
    public static String error_result_file_template = "Static_errors_";
    public static String result_file = "";
    public static FileOutputStream fileout;
    public static String querry_deeper = "";
    public static Drive driveservice;
    public static ExecutorService executor = Executors.newFixedThreadPool(6);
    public static List<Future<?>> futures = new ArrayList<>();

    public static void main(ModelAndView modelAndView, Multimap<String, String> email_exceptions, Multimap<String, String> folder_exceptions) {
        try {
            ArrayList<StaticReportMap> staticReportMap = new ArrayList<>();
            ArrayList<StaticReportErrorMap> staticReportErrorMap = new ArrayList<>();
            driveservice = Apiv3.Drive();
            String query = "'" + modelAndView.getModel().get("F_scanFolderId").toString() + "'  in parents and trashed=false";
            System.out.println("------------------------ STATIC RUN ------------------------ ");
            System.out.println("start " + new Date());
            FileList fileList = get_driveservice_v3_files(driveservice, query);
            List<File> listFile = fileList.getFiles();
            deeper_in_folders(staticReportErrorMap, staticReportMap, "PROJECTS", "root", listFile, email_exceptions, folder_exceptions);
            for (Future<?> feature : futures) {
                while (!feature.isDone())
                    TimeUnit.SECONDS.sleep(1);
                feature.get();
            }
            Collections.sort(staticReportMap);
            Thread.sleep(400);
            write_to_file(staticReportMap);
            CreateGoogleFile.main(modelAndView, result_file);
            write_errors_to_file(staticReportErrorMap);
            CreateGoogleFile.main(modelAndView, result_file);
            System.out.println("end " + new Date());
        } catch (Exception exec) {
            System.out.println(exec);
        }
    }

    public static void deeper_in_folders(ArrayList<StaticReportErrorMap> staticReportErrorMap, ArrayList<StaticReportMap> staticReportMap, String folderName, String parentFolderID, List<File> file, Multimap<String, String> email_exceptions, Multimap<String, String> folder_exceptions) {
        for (File f : file) {
            try {
                if (!folder_exceptions.containsValue(f.getId())) {
                    if (f.getMimeType().equals("application/vnd.google-apps.folder") || f.getMimeType().equals("folder")) {
                        querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                        deeper_in_folders(staticReportErrorMap, staticReportMap, folderName.concat("/".concat(f.getName())), f.getId(), get_driveservice_v3_files(driveservice, querry_deeper).getFiles(), email_exceptions, folder_exceptions);
                    } else {
                        System.out.println(f.getName());
                        StaticReportMap candy = new StaticReportMap(f.getId(), folderName, f.getName(), f.getWebViewLink(), parentFolderID);
                        Runnable worker = new StaticWorkerThread(staticReportErrorMap, staticReportMap, candy,email_exceptions,folder_exceptions);
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
