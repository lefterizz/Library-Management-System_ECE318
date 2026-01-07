package com.example.ece318_librarymanagementsys.util;

import com.example.ece318_librarymanagementsys.database.GenreDAO;
import com.example.ece318_librarymanagementsys.database.SubGenreDAO;
import com.example.ece318_librarymanagementsys.model.Book;
import com.example.ece318_librarymanagementsys.model.Genre;
import com.example.ece318_librarymanagementsys.model.SubGenre;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Unified PDF Exporter

public class PDFExporter {

    // Export Books
    public static boolean promptAndExportBooks(Window owner, List<Book> books) throws Exception {
        File file = promptForFile(owner, "Export Books to PDF");
        if (file == null || books == null || books.isEmpty()) {
            return false;
        }
        exportBooks(file, books);
        return true;
    }

    private static void exportBooks(File file, List<Book> books) throws Exception {
        Document doc = new Document(PageSize.A4, 50, 50, 60, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // Title
        Paragraph title = new Paragraph("Library Books Export",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph subtitle = new Paragraph(
                "Exported on " + LocalDateTime.now() + " | Total Books: " + books.size(),
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)
        );
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        doc.add(subtitle);

        PdfPTable table = createStyledTable(6, 3f, 2.5f, 2.5f, 2.5f, 1.2f, 1.5f);
        addHeaderRow(table, "Title", "Author", "Genre", "Sub-Genre", "Rating", "Price");

        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (Book b : books) {
            addStyledCell(table, nullSafe(b.getTitle()), normal);
            addStyledCell(table, nullSafe(b.getAuthor()), normal);
            addStyledCell(table, nullSafe(b.getMainGenre()), normal);
            addStyledCell(table, nullSafe(b.getSubGenre()), normal);
            addStyledCell(table, String.format("%.1f★", b.getRating()), normal);
            addStyledCell(table, String.format("$%.2f", b.getPrice()), normal);
        }

        doc.add(table);
        doc.close();
        writer.close();
    }

    // Export Genres
    public static boolean promptAndExportGenres(Window owner, List<Genre> genres) throws Exception {
        File file = promptForFile(owner, "Export Genres to PDF");
        if (file == null || genres == null || genres.isEmpty()) {
            return false;
        }
        exportGenres(file, genres);
        return true;
    }

    private static void exportGenres(File file, List<Genre> genres) throws Exception {
        Document doc = new Document(PageSize.A4.rotate(), 50, 50, 60, 50); // Landscape
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // Title
        Paragraph title = new Paragraph("Library Genres Report",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph subtitle = new Paragraph(
                "Exported on " + LocalDateTime.now() + " | Total Genres: " + genres.size(),
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)
        );
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        doc.add(subtitle);

        // Fetch stats from DB
        GenreDAO genreDAO = new GenreDAO();
        List<GenreDAO.GenreStat> stats = genreDAO.getGenreStats();
        Map<String, GenreDAO.GenreStat> statMap = new HashMap<>();
        for (GenreDAO.GenreStat s : stats) {
            statMap.put(s.name, s);
        }

        PdfPTable table = createStyledTable(5, 3f, 2f, 2f, 2f, 2f);
        addHeaderRow(table, "Genre Name", "No. of Sub-Genres", "Total No. of Books", "Avg Rating", "Avg Price");

        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (Genre g : genres) {
            GenreDAO.GenreStat s = statMap.get(g.getName());

            addStyledCell(table, nullSafe(g.getName()), normal);
            addStyledCell(table, String.valueOf(g.getNumSubGenres()), normal);
            addStyledCell(table, s != null ? String.valueOf(s.totalBooks) : "0", normal);
            addStyledCell(table, s != null ? String.format("%.2f★", s.avgRating) : "0.00★", normal);
            addStyledCell(table, s != null ? String.format("$%.2f", s.avgPrice) : "0.00", normal);
        }

        doc.add(table);
        doc.close();
        writer.close();
    }

    // Export SubGenres
    public static boolean promptAndExportSubGenres(Window owner, List<SubGenre> subGenres) throws Exception {
        File file = promptForFile(owner, "Export Sub-Genres to PDF");
        if (file == null || subGenres == null || subGenres.isEmpty()) {
            return false;
        }
        exportSubGenres(file, subGenres);
        return true;
    }

    private static void exportSubGenres(File file, List<SubGenre> subGenres) throws Exception {
        Document doc = new Document(PageSize.A4.rotate(), 50, 50, 60, 50); // Landscape
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // Title
        Paragraph title = new Paragraph("Library Sub-Genres Report",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph subtitle = new Paragraph(
                "Exported on " + LocalDateTime.now() + " | Total Sub-Genres: " + subGenres.size(),
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)
        );
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        doc.add(subtitle);

        // Fetch stats from DB
        SubGenreDAO subGenreDAO = new SubGenreDAO();
        List<SubGenreDAO.SubGenreStat> stats = subGenreDAO.getSubGenreStats();
        Map<String, SubGenreDAO.SubGenreStat> statMap = new HashMap<>();
        for (SubGenreDAO.SubGenreStat s : stats) {
            statMap.put(s.name, s);
        }

        PdfPTable table = createStyledTable(6, 3f, 2.5f, 2f, 2f, 2f, 2f);
        addHeaderRow(table, "Sub-Genre Name", "Main Genre", "No. of Books", "Avg Rating", "Avg Price", "Total Books");

        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (SubGenre sg : subGenres) {
            SubGenreDAO.SubGenreStat s = statMap.get(sg.getName());

            addStyledCell(table, nullSafe(sg.getName()), normal);
            addStyledCell(table, nullSafe(sg.getMainGenre()), normal);
            addStyledCell(table, String.valueOf(sg.getNumBooks()), normal);
            addStyledCell(table, s != null ? String.format("%.2f★", s.avgRating) : "0.00★", normal);
            addStyledCell(table, s != null ? String.format("$%.2f", s.avgPrice) : "0.00", normal);
            addStyledCell(table, s != null ? String.valueOf(s.totalBooks) : "0", normal);
        }

        doc.add(table);
        doc.close();
        writer.close();
    }

    private static File promptForFile(Window owner, String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        return chooser.showSaveDialog(owner);
    }

    private static PdfPTable createStyledTable(int columns, float... widths) throws DocumentException {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        if (widths != null && widths.length == columns) {
            table.setWidths(widths);
        }
        table.getDefaultCell().setPadding(6);
        return table;
    }

    private static void addHeaderRow(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setPadding(6);
            table.addCell(cell);
        }
    }

    private static void addStyledCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private static String nullSafe(String text) {
        return text == null ? "" : text;
    }

    public static boolean export(Window owner, List<?> list) {
        if (list == null || list.isEmpty()) return false;

        // 1) Show save dialog ONCE
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = chooser.showSaveDialog(owner);

        // User cancelled
        if (file == null) return false;

        try {
            // 2) Determine type and export directly (NO prompt inside)
            Object first = list.get(0);

            if (first instanceof Book)
                exportBooks(file, (List<Book>) list);
            else if (first instanceof Genre)
                exportGenres(file, (List<Genre>) list);
            else if (first instanceof SubGenre)
                exportSubGenres(file, (List<SubGenre>) list);

            return true; // success

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}