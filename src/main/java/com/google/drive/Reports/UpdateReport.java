package com.google.drive.Reports;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.collect.Multimap;
import com.google.drive.WorkerThreads.UpdateWorkerThread;
import com.google.drive.api.Google.CreateGoogleFile;
import com.google.drive.api.authorize.Apiv3;
import com.google.drive.maps.UpdateReportErrorMap;
import com.google.drive.maps.UpdateReportMap;
import org.springframework.web.servlet.ModelAndView;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.drive.Writers.UpdateWriter.write_errors_to_file;
import static com.google.drive.Writers.UpdateWriter.write_to_file;
import static com.google.drive.api.authorize.Apiv3.get_driveservice_v3_files;

public class UpdateReport {
    public static String result_file_template = "Update_result_";
    public static String error_result_file_template = "Update_errors_";
    public static String result_file = "";
    public static FileOutputStream fileout;
    public static String querry_deeper = "";
    public static Drive driveservice;
    public static ExecutorService executor = Executors.newFixedThreadPool(6);
    public static List<Future<?>> futures = new ArrayList<>();

    public static void main(ModelAndView modelAndView, Multimap<String, String> email_exceptions, Multimap<String, String> folder_exceptions) {
        try {
            ArrayList<UpdateReportMap> updateReportMap = new ArrayList<>();
            ArrayList<UpdateReportErrorMap> updateReportErrorMap = new ArrayList<>();
            driveservice = Apiv3.Drive();
            String query = "'" + modelAndView.getModel().get("F_scanFolderId").toString() + "'  in parents and trashed=false";
            System.out.println("------------------------ UPDATE PERMISSIONS RUN ------------------------ ");
            System.out.println("start " + new Date());
            FileList fileList = get_driveservice_v3_files(driveservice, query);
            List<File> listFile = fileList.getFiles();
            createWorkerMainPath(updateReportErrorMap, updateReportMap, modelAndView, email_exceptions);

            deeper_in_folders(updateReportErrorMap, updateReportMap, modelAndView, "PROJECTS", modelAndView.getModel().get("F_scanFolderId").toString(), listFile, email_exceptions, folder_exceptions);
            for (Future<?> feature : futures) {
                while (!feature.isDone())
                    TimeUnit.SECONDS.sleep(1);
                feature.get();
            }
            Collections.sort(updateReportMap);
            Thread.sleep(200);
            write_to_file(updateReportMap);
            CreateGoogleFile.main(modelAndView, result_file);
            write_errors_to_file(updateReportErrorMap);
            CreateGoogleFile.main(modelAndView, result_file);
            System.out.println("end " + new Date());
        } catch (Exception exec) {
            System.out.println(exec);
        }
    }

    public static void deeper_in_folders(ArrayList<UpdateReportErrorMap> updateReportErrorMap, ArrayList<UpdateReportMap> updateReportMap, ModelAndView modelAndView, String folderName, String parentFolderID, List<File> file, Multimap<String, String> email_exceptions, Multimap<String, String> folder_exceptions) {
        for (File f : file) {
            try {
                if (!folder_exceptions.containsValue(f.getId())) {
                    if (f.getMimeType().equals("application/vnd.google-apps.folder") || f.getMimeType().equals("folder")
                            || f.getMimeType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                        System.out.println(f.getId());
                        System.out.println(f.getName());
                        querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                        createWorker(updateReportErrorMap, updateReportMap, modelAndView, f, parentFolderID, folderName, email_exceptions);
                        deeper_in_folders(updateReportErrorMap, updateReportMap, modelAndView, folderName.concat("/".concat(f.getName())), f.getId(), get_driveservice_v3_files(driveservice, querry_deeper).getFiles(), email_exceptions, folder_exceptions);
                    } else {
                        System.out.println(f.getId());
                        System.out.println(f.getName());
                        createWorker(updateReportErrorMap, updateReportMap, modelAndView, f, parentFolderID, folderName, email_exceptions);
                    }
                }
            } catch (Exception ss) {
                System.out.println("deeper_in_folders = " + f.getName() + ss);
            }
        }
    }

    public static void createWorker(ArrayList<UpdateReportErrorMap> updateReportErrorMap, ArrayList<UpdateReportMap> updateReportMap, ModelAndView modelAndView, File f, String parentFolderID, String folderName, Multimap<String, String> email_exceptions) {
        try {
            UpdateReportMap candy = new UpdateReportMap(f.getId(), folderName, f.getName(), f.getWebViewLink(), parentFolderID);
            Runnable worker = new UpdateWorkerThread(updateReportErrorMap, updateReportMap, modelAndView, candy, email_exceptions);
            Thread.sleep(140);
            futures.add(executor.submit(worker));
        } catch (Exception createWorker) {
            System.out.println(createWorker.getMessage());
        }
    }

    public static void createWorkerMainPath(ArrayList<UpdateReportErrorMap> updateReportErrorMap, ArrayList<UpdateReportMap> updateReportMap, ModelAndView modelAndView, Multimap<String, String> email_exceptions) {
        try {
            UpdateReportMap candy = new UpdateReportMap(modelAndView.getModel().get("F_scanFolderId").toString(),
                    "", "", "", "");
            Runnable worker = new UpdateWorkerThread(updateReportErrorMap, updateReportMap, modelAndView, candy, email_exceptions);
            Thread.sleep(140);
            futures.add(executor.submit(worker));
        } catch (Exception createWorker) {
            System.out.println(createWorker.getMessage());
        }
    }
}