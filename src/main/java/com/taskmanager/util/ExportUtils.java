package com.taskmanager.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.taskmanager.model.Task;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * تصدير تقرير الإنتاجية إلى CSV / Excel / PDF.
 * تُلقى كل أخطاء الإدخال/الإخراج كـ IOException واضحة يعالجها المتحكم بعرض تنبيه للمستخدم.
 */
public class ExportUtils {

    public static void exportToCsv(List<Task> tasks, Path outputPath) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("العنوان,التصنيف,الأولوية,الحالة,الوقت المقدر (د),الوقت الفعلي (د),الموعد النهائي\n");
        for (Task t : tasks) {
            sb.append(csvEscape(t.getTitle())).append(',')
              .append(t.getCategory().getDisplayName()).append(',')
              .append(t.getPriority().getDisplayName()).append(',')
              .append(t.getStatus().getDisplayName()).append(',')
              .append(t.getEstimatedMinutes()).append(',')
              .append(t.getActualMinutes()).append(',')
              .append(DateTimeUtils.formatDateTime(t.getDeadline()))
              .append('\n');
        }
        Files.write(outputPath, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void exportToExcel(List<Task> tasks, Path outputPath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("تقرير الإنتاجية");
            String[] headers = {"العنوان", "التصنيف", "الأولوية", "الحالة", "الوقت المقدر (د)", "الوقت الفعلي (د)", "الموعد النهائي"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowIdx = 1;
            for (Task t : tasks) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getTitle());
                row.createCell(1).setCellValue(t.getCategory().getDisplayName());
                row.createCell(2).setCellValue(t.getPriority().getDisplayName());
                row.createCell(3).setCellValue(t.getStatus().getDisplayName());
                row.createCell(4).setCellValue(t.getEstimatedMinutes());
                row.createCell(5).setCellValue(t.getActualMinutes());
                row.createCell(6).setCellValue(DateTimeUtils.formatDateTime(t.getDeadline()));
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                workbook.write(fos);
            }
        }
    }

    public static void exportToPdf(List<Task> tasks, Path outputPath, String reportTitle) throws IOException {
        try (PdfWriter writer = new PdfWriter(outputPath.toString());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph(reportTitle).setBold().setFontSize(16));
            Table table = new Table(6);
            table.addHeaderCell("العنوان");
            table.addHeaderCell("التصنيف");
            table.addHeaderCell("الأولوية");
            table.addHeaderCell("الحالة");
            table.addHeaderCell("مقدّر (د)");
            table.addHeaderCell("فعلي (د)");

            for (Task t : tasks) {
                table.addCell(t.getTitle());
                table.addCell(t.getCategory().getDisplayName());
                table.addCell(t.getPriority().getDisplayName());
                table.addCell(t.getStatus().getDisplayName());
                table.addCell(String.valueOf(t.getEstimatedMinutes()));
                table.addCell(String.valueOf(t.getActualMinutes()));
            }
            document.add(table);
        }
    }

    private static String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
