package api.Writers;

import Reports.StaticReport;
import maps.StaticReportErrorMap;
import maps.StaticReportMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.IntStream;

public class StaticWriter extends StaticReport{
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
            System.out.println("created xls file!");

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
            System.out.println("created xls Error file!");
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
