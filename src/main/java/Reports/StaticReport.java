package Reports;

import api.authorize.Apiv3;
import api.Google.CreateGoogleFile;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import maps.StaticReportMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static Quartz.CronBuild.folder_exceptions;
import static Quartz.CronBuild.startFolderId;

public class StaticReport implements Job {

    public static ArrayList<StaticReportMap> staticReportMap = new ArrayList<>();
    private String resultfiletemplate = "Static_audit_result_";
    private String resultfile = "";
    private FileOutputStream fileout;
    private String querry_deeper = "";
    public static Drive driveservice;
    private ExecutorService executor = Executors.newFixedThreadPool(6);
    private List<Future<?>> futures = new ArrayList<>();
    private StaticReportMap candy;

    public void execute(JobExecutionContext context) {
        try {
            driveservice = Apiv3.Drive();
            String query = "'" + startFolderId + "'  in parents and trashed=false";
            System.out.println("------------------------ STATIC RUN ------------------------ ");
            System.out.println("start " + new Date());
            FileList fileList = get_driveservice_v3_files(query);
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
            CreateGoogleFile.main(resultfile);
            System.out.println("end " + new Date());
            staticReportMap.clear();
        } catch (Exception exec) {
            System.out.println(exec);
        }
    }

    public FileList get_driveservice_v3_files(String query) {
        try {
            return driveservice.files().list().setQ(query).setFields("nextPageToken, " +
                    "files(id, parents, name, webViewLink, mimeType)").execute();
            //, sharingUser(emailAddress, permissionId)
        } catch (Exception x) {
            throw new RuntimeException("Cannot get_driveservice_v3_files = ", x);
        }
    }

    public void deeper_in_folders(String folderName, String parentFolderID, List<File> file) {
        for (File f : file) {
            try {
                if (!folder_exceptions.containsValue(f.getId())) {
                    if (f.getMimeType().equals("application/vnd.google-apps.folder") || f.getMimeType().equals("folder")) {
                        querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                        deeper_in_folders(folderName.concat("/".concat(f.getName())), f.getId(), get_driveservice_v3_files(querry_deeper).getFiles());
                    } else {
                        System.out.println(f.getName());
                        candy = new StaticReportMap(f.getId(), folderName, f.getName(), f.getWebViewLink(), parentFolderID);
                        staticReportMap.add(candy);
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

    public void write_to_file(ArrayList<StaticReportMap> staticReportMap) {
        try {
            String audit_date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            resultfile = resultfiletemplate.concat(audit_date.concat(".xlsx"));
            System.out.println("writing to the file....");
            XSSFWorkbook wb = new XSSFWorkbook();
            Cell cell;
            String output = resultfile;
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
                /*System.out.println(product.getId());
                System.out.println(product.getFolderName());
                System.out.println(product.getName());
                System.out.println(product.getParentFolderId());
                System.out.println(product.getBadOwnersList());
                System.out.println(product.getGoodOwnersList());
                System.out.println(product.getIdowners());
                System.out.println(product.getIdInovus());
                System.out.println(product.getWebViewLink());*/
                Row dataRow;
                if (product.getBadOwnersList().isEmpty()) {
                    dataRow = goodList.createRow(goodRow);
                    isbad = false;
                } else {
                    dataRow = badlist.createRow(badRow);
                    isbad = true;
                }
                cell = dataRow.createCell(0);
                cell.setCellValue(product.getFolderName());
                cell.setCellStyle(cs);

                cell = dataRow.createCell(1);
                CreationHelper createHelper = wb.getCreationHelper();
                Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
                CellStyle hlink_style = wb.createCellStyle();
                link.setAddress(product.getWebViewLink());
                //cell.setCellStyle(hlink_style);
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
                else
                    goodRow++;
            }
            wb.write(fileout);
            fileout.close();
        } catch (Exception e) {
            System.out.println("write_to_file = " + e);
            System.exit(0);
        }
    }

    public void create_columns(Sheet x) {
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
            IntStream.range(1, 6).forEach((columnIndex) -> x.setColumnWidth(columnIndex, 9500));
            x.setColumnWidth(0, 5000);
            x.setColumnWidth(3, 12500);
        } catch (Exception create_columns) {
            System.out.println("create_columns" + create_columns);
        }
    }
}