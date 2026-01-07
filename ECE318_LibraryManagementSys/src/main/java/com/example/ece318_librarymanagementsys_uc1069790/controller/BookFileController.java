package com.example.ece318_librarymanagementsys.controller;

import com.example.ece318_librarymanagementsys.database.BookDAO;
import com.example.ece318_librarymanagementsys.model.*;
import com.example.ece318_librarymanagementsys.util.CSVLoader;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;

// Handles file operations including CSV
public class BookFileController {
    private final BookDAO bookDAO;
    private final ObservableList<Book> books;
    private final List<Genre> genres;
    private final List<SubGenre> subGenres;

    public BookFileController(BookDAO bookDAO, ObservableList<Book> books,
                              List<Genre> genres, List<SubGenre> subGenres) {
        this.bookDAO = bookDAO;
        this.books = books;
        this.genres = genres;
        this.subGenres = subGenres;
    }

    public void loadBooksFromCSV(Window ownerWindow) {
        String message = """
            This will add books from the CSV file to the database.

            Press OK to proceed or Cancel to abort.
            """;

        if (!showConfirmation(ownerWindow, "Load Books from CSV", message)) {
            return;
        }

        File file = selectCSVFile(ownerWindow);
        if (file == null) return;

        try {
            // Clear books if database is empty or user wants fresh start
            if (bookDAO.getAll().isEmpty()) {
                bookDAO.clearBooks(); // This resets AUTO_INCREMENT
            }

            loadAndPersistBooks(file);
            books.setAll(bookDAO.getAll());
            showInformation("Books loaded successfully!");

        } catch (Exception e) {
            handleLoadError(e);
        }
    }

    private void loadAndPersistBooks(File file) throws Exception {
        List<Book> booksFromCsv = CSVLoader.loadBooks(file, genres, subGenres);

        for (Book book : booksFromCsv) {
            bookDAO.insert(book);
        }
        bookDAO.printSkippedCount();
    }

    // Opens file chooser
    private File selectCSVFile(Window ownerWindow) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Books CSV File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        return chooser.showOpenDialog(ownerWindow);
    }

    // error during book loading
    private void handleLoadError(Exception e) {
        e.printStackTrace();
        showError("Load Error", "Failed to Load Books", "Error loading CSV: " + e.getMessage());
    }

    public void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public boolean showConfirmation(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (owner != null) {
            alert.initOwner(owner);
        }
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    public void showError(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }
}
