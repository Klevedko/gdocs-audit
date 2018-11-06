package api.Writers;

import Reports.DynamicReport;
import maps.DynamicReportMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.IntStream;

public class DynamicWriter extends DynamicReport {
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
            System.out.println("creating columns 1-3");
            create_columns(list1);
            create_columns(list2);
            create_columns(list3);
            int row1 = 1;
            int row2 = 1;
            int row3 = 1;
            for (DynamicReportMap product : resultMap) {
                write(wb, cs, list1, row1, product);
                row1++;
                if (product.isItPermissionChange()) {
                    write(wb, cs, list2, row2, product);
                    row2++;
                }
                if (!product.isItPermissionChange()) {
                    write(wb, cs, list3, row3, product);
                    row3++;
                }
            }
            wb.write(fileout);
            fileout.close();
            System.out.println("created xls file!");
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    final static void write(Workbook wb, CellStyle cs, Sheet x, Integer y, DynamicReportMap product) {
        Row dataRow;
        Cell cell;
        dataRow = x.createRow(y);
        dataRow.setHeight((short) 811);
        cell = dataRow.createCell(0);
        cell.setCellStyle(cs);
        cell.setCellValue(product.getDate());

        cell = dataRow.createCell(1);
        cell.setCellStyle(cs);
        cell.setCellValue(product.getFoldername());
        Hyperlink link = wb.getCreationHelper().createHyperlink(Hyperlink.LINK_FILE);

        cell = dataRow.createCell(2);
        cell.setCellStyle(cs);
        link.setAddress(product.getWebViewLink());
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
        cell.setCellValue(product.getBadOwnersList());
    }

    final static void create_columns(Sheet x) {
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
