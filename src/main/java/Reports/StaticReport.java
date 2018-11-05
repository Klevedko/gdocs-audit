package Reports;

import api.authorize.Apiv3;
import api.Google.CreateGoogleFile;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import maps.StaticReportErrorMap;
import maps.StaticReportMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;
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
                        //staticReportMap.add(candy);
                        Thread.sleep(140);
                        futures.add(executor.submit(worker));
                    }
                }
            } catch (Exception ss) {
                System.out.println("deeper_in_folders = " + f.getName() + ss);
            }
        }
    }

    public static void write_to_file(ArrayList<StaticReportMap> staticReportMap) {
        try {
            String audit_date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            result_file = result_file_template.concat(audit_date.concat(".xlsx"));
            System.out.println("writing to the file....");
            XSSFWorkbook wb = new XSSFWorkbook();
            Cell cell;
            String output = result_file;
            fileout = new FileOutputStream(output);
            Sheet goodList = wb.createSheet("Нет подозрений");
            Sheet badlist = wb.createSheet("Есть подозрения");
            System.out.println("create_columns1");
            create_columns(goodList);
            System.out.println("create_columns2");
            create_columns(badlist);
            System.out.println("write!");

            int goodRow = 1;
            int badRow = 1;
            boolean isbad;
            CellStyle cs = wb.createCellStyle();
            cs.setWrapText(true);
            for (StaticReportMap product : staticReportMap) {
                Row dataRow;
                if (product.getBadOwnersList().isEmpty()) {
                    dataRow = goodList.createRow(goodRow);
                    isbad = false;
                } else {
                    dataRow = badlist.createRow(badRow);
                    isbad = true;
                }
                dataRow.setHeight((short) 811);
                cell = dataRow.createCell(0);
                cell.setCellValue(product.getFolderName());
                cell.setCellStyle(cs);

                cell = dataRow.createCell(1);
                CreationHelper createHelper = wb.getCreationHelper();
                Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
                link.setAddress(product.getWebViewLink());
                cell.setCellValue(product.getName());
                cell.setCellStyle(cs);
                if (!product.getWebViewLink().equals(""))
                    cell.setHyperlink(link);

                cell = dataRow.createCell(2);
                cell.setCellValue(product.getIdreal_owner());
                cell.setCellStyle(cs);

                cell = dataRow.createCell(3);
                cell.setCellValue(product.getIdowners());
                cell.setCellStyle(cs);

                cell = dataRow.createCell(4);
                cell.setCellValue(product.getGoodOwnersList());
                cell.setCellStyle(cs);

                cell = dataRow.createCell(5);
                cell.setCellValue(product.getBadOwnersList());
                cell.setCellStyle(cs);

                if (isbad) badRow++;
                else goodRow++;
            }
            wb.write(fileout);
            fileout.close();
        } catch (Exception e) {
            System.out.println("write_to_file = " + e);
            System.exit(0);
        }
    }

    public static void write_errors_to_file(ArrayList<StaticReportErrorMap> staticReportErrorMap) {
        try {
            String audit_date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            System.out.println("writing errors to the file....");
            XSSFWorkbook wb = new XSSFWorkbook();
            Cell cell;
            String output = error_result_file_template.concat(audit_date.concat(".xlsx"));
            fileout = new FileOutputStream(output);
            Sheet errorList = wb.createSheet("Ошибки доступа серв. аккаунта!");
            System.out.println("create error columns");
            int errorRow = 0;
            Row dataRow = errorList.createRow(errorRow);

            cell = dataRow.createCell(0);
            cell.setCellValue("Идентификатор файла");

            cell = dataRow.createCell(1);
            cell.setCellValue("Имя файла");

            cell = dataRow.createCell(2);
            cell.setCellValue("Папка");
            IntStream.range(0, 2).forEach((columnIndex) -> errorList.setColumnWidth(columnIndex, 17700));

            System.out.println("write errors!");
            errorRow++;
            for (StaticReportErrorMap errors : staticReportErrorMap) {
                System.out.println(errors.getName());
                dataRow = errorList.createRow(errorRow);
                dataRow.setHeight((short) 811);

                cell = dataRow.createCell(0);
                cell.setCellValue(errors.getId());

                cell = dataRow.createCell(1);
                cell.setCellValue(errors.getName());

                cell = dataRow.createCell(2);
                cell.setCellValue(errors.getFolderName());
                errorRow++;
            }
            wb.write(fileout);
            fileout.close();
        } catch (Exception e) {
            System.out.println("write_to_file = " + e);
            System.exit(0);
        }
    }

    public static void create_columns(Sheet x) {
        try {
            int row = 0;
            Cell cell;
            Row dataRow = x.createRow(row);
            cell = dataRow.createCell(0);
            cell.setCellValue("Папка");
            cell = dataRow.createCell(1);
            cell.setCellValue("Файл");
            cell = dataRow.createCell(2);
            cell.setCellValue("Владелец");
            cell = dataRow.createCell(3);
            cell.setCellValue("Общий список прав");
            cell = dataRow.createCell(4);
            cell.setCellValue("Доступ сотрудников АН");
            cell = dataRow.createCell(5);
            cell.setCellValue("Доступ сторонних сотрудников");
            IntStream.range(0, 6).forEach((columnIndex) -> x.setColumnWidth(columnIndex, 9500));
            x.setColumnWidth(3, 12500);
        } catch (Exception create_columns) {
            System.out.println("create_columns" + create_columns);
        }
    }
}