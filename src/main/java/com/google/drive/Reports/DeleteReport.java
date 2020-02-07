package com.google.drive.Reports;

import com.google.common.collect.Multimap;
import com.google.drive.WorkerThreads.DeleteWorkerThread;
import com.google.drive.api.Google.CreateGoogleFile;
import com.google.drive.api.authorize.Apiv3;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.drive.maps.DeleteReportErrorMap;
import com.google.drive.maps.DeleteReportMap;
import org.springframework.web.servlet.ModelAndView;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static com.google.drive.App.*;
import static com.google.drive.Writers.DeleteWriter.write_errors_to_file;
import static com.google.drive.Writers.DeleteWriter.write_to_file;
import static com.google.drive.api.authorize.Apiv3.get_driveservice_v3_files;

public class DeleteReport {

    //public static ArrayList<com.google.drive.maps.DeleteReportMap> deleteReportMap = new ArrayList<>();
    //public static ArrayList<com.google.drive.maps.DeleteReportErrorMap> deleteReportErrorMap = new ArrayList<>();
    public static String result_file_template = "Delete_result_";
    public static String error_result_file_template = "Delete_errors_";
    public static String result_file = "";
    public static FileOutputStream fileout;
    public static String querry_deeper = "";
    public static Drive driveservice;
    public static ExecutorService executor = Executors.newFixedThreadPool(6);
    public static List<Future<?>> futures = new ArrayList<>();
    //public static com.google.drive.maps.DeleteReportMap candy;

    public static void main(ModelAndView modelAndView, Multimap<String, String> email_exceptions, Multimap<String, String> folder_exceptions) {
        try {
            ArrayList<DeleteReportMap> deleteReportMap = new ArrayList<>();
            ArrayList<DeleteReportErrorMap> deleteReportErrorMap = new ArrayList<>();
            driveservice = Apiv3.Drive();
            String query = "'" + modelAndView.getModel().get("F_scanFolderId").toString() + "'  in parents and trashed=false";
            System.out.println("------------------------ DELETE PERMISSIONS RUN ------------------------ ");
            System.out.println("start " + new Date());
            FileList fileList = get_driveservice_v3_files(driveservice, query);
            List<File> listFile = fileList.getFiles();
            createWorkerMainPath(deleteReportErrorMap, deleteReportMap, modelAndView,email_exceptions);

            deeper_in_folders(deleteReportErrorMap, deleteReportMap, modelAndView, "PROJECTS", modelAndView.getModel().get("F_scanFolderId").toString(), listFile
                    , email_exceptions, folder_exceptions);
            for (Future<?> feature : futures) {
                while (!feature.isDone())
                    TimeUnit.SECONDS.sleep(1);
                feature.get();
            }
            Collections.sort(deleteReportMap);
            Thread.sleep(200);
            write_to_file(deleteReportMap);
            CreateGoogleFile.main(modelAndView, result_file);
            write_errors_to_file(deleteReportErrorMap);
            CreateGoogleFile.main(modelAndView, result_file);
            System.out.println("end " + new Date());
        } catch (Exception exec) {
            System.out.println(exec);
        }
    }

    public static void deeper_in_folders(ArrayList<DeleteReportErrorMap> deleteReportErrorMap, ArrayList<DeleteReportMap> deleteReportMap, ModelAndView modelAndView, String folderName, String parentFolderID, List<File> file, Multimap<String, String> email_exceptions, Multimap<String, String> folder_exceptions) {
        for (File f : file) {
            try {
                if (!folder_exceptions.containsValue(f.getId())) {
                    if (f.getMimeType().equals("application/vnd.google-apps.folder") || f.getMimeType().equals("folder")
                            || f.getMimeType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                        System.out.println(f.getId());
                        System.out.println(f.getName());
                        querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                        createWorker(deleteReportErrorMap, deleteReportMap, modelAndView, f, parentFolderID, folderName,email_exceptions);
                        deeper_in_folders(deleteReportErrorMap, deleteReportMap, modelAndView, folderName.concat("/".concat(f.getName())), f.getId(), get_driveservice_v3_files(driveservice, querry_deeper).getFiles(), email_exceptions, folder_exceptions);
                    } else {
                        System.out.println(f.getId());
                        System.out.println(f.getName());
                        createWorker(deleteReportErrorMap, deleteReportMap, modelAndView, f, parentFolderID, folderName, email_exceptions);
                    }
                }
            } catch (Exception ss) {
                System.out.println("deeper_in_folders = " + f.getName() + ss);
            }
        }
    }

    public static void createWorker(ArrayList<DeleteReportErrorMap> deleteReportErrorMap, ArrayList<DeleteReportMap> deleteReportMap, ModelAndView modelAndView, File f, String parentFolderID, String folderName,Multimap<String, String> email_exceptions) {
        try {
            DeleteReportMap candy = new com.google.drive.maps.DeleteReportMap(f.getId(), folderName, f.getName(), f.getWebViewLink(), parentFolderID);
            Runnable worker = new DeleteWorkerThread(deleteReportErrorMap, deleteReportMap, modelAndView, candy, email_exceptions);
            Thread.sleep(140);
            futures.add(executor.submit(worker));
        } catch (Exception createWorker) {
            System.out.println(createWorker.getMessage());
        }
    }

    public static void createWorkerMainPath(ArrayList<DeleteReportErrorMap> deleteReportErrorMap, ArrayList<DeleteReportMap> deleteReportMap, ModelAndView modelAndView,Multimap<String, String> email_exceptions) {
        try {
            DeleteReportMap candy = new com.google.drive.maps.DeleteReportMap(modelAndView.getModel().get("F_scanFolderId").toString(),
                    "", "", "", "");
            Runnable worker = new DeleteWorkerThread(deleteReportErrorMap, deleteReportMap, modelAndView, candy, email_exceptions);
            Thread.sleep(140);
            futures.add(executor.submit(worker));
        } catch (Exception createWorker) {
            System.out.println(createWorker.getMessage());
        }
    }
}