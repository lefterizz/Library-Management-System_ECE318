package com.example.ece318_librarymanagementsys.controller.core;

import com.example.ece318_librarymanagementsys.database.*;
import com.example.ece318_librarymanagementsys.model.*;
import static com.example.ece318_librarymanagementsys.util.InputValidator.*;

import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.*;

import java.io.*;
import java.util.List;
import java.util.Map;

public class FormController {

    // ─── Common ─────────────────────────────
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    // ─── Genre section ──────────────────────
    @FXML private VBox genreSection;
    @FXML private TextField numSubGenresField;
    @FXML private TextField urlField;

    // ─── SubGenre section ───────────────────
    @FXML private VBox subGenreSection;
    @FXML private ComboBox<String> mainGenreCombo;
    @FXML private TextField numBooksField;
    @FXML private TextField urlFieldSub;

    // ─── Book section ───────────────────────
    @FXML private VBox bookSection;
    @FXML private TextField authorField;
    @FXML private ComboBox<String> genreCombo;
    @FXML private ComboBox<String> subGenreCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField priceField;
    @FXML private TextField ratingField;
    @FXML private TextField numRatedField;
    @FXML private TextField urlFieldBook;

    // ─── DAOs ───────────────────────────────
    private final GenreDAO genreDAO = new GenreDAO();
    private final SubGenreDAO subGenreDAO = new SubGenreDAO();
    private final BookDAO bookDAO = new BookDAO();

    private Object entity;
    private Runnable refreshCallback;
    private FormType type;

    private enum FormType { GENRE, SUBGENRE, BOOK }

    @FXML
    private void initialize() {
        saveBtn.setOnAction(e -> saveEntity());
        cancelBtn.setOnAction(e -> closeForm());
    }

    public void setupAsGenre(Genre existing) {
        type = FormType.GENRE;
        formTitle.setText(existing == null || existing.getId() == 0 ? "Add Genre" : "Edit Genre");
        toggleSections(true, false, false);
        this.entity = existing;

        numSubGenresField.setEditable(false); // Disable no of subgenres
        numSubGenresField.setDisable(true);
        numSubGenresField.setFocusTraversable(false);

        if (existing != null && existing.getId() != 0) {
            nameField.setText(existing.getName());
            numSubGenresField.setText(String.valueOf(existing.getNumSubGenres()));
            urlField.setText(existing.getUrl());
        } else {
            numSubGenresField.setText("0");
        }

    }

    public void setupAsSubGenre(SubGenre existing) {
        type = FormType.SUBGENRE;
        formTitle.setText(existing == null || existing.getId() == 0 ? "Add SubGenre" : "Edit SubGenre");
        toggleSections(false, true, false);
        this.entity = existing;

        numBooksField.setEditable(false); // Disable no of books
        numBooksField.setDisable(true);
        numBooksField.setFocusTraversable(false);

        // Load main genres
        mainGenreCombo.setItems(FXCollections.observableArrayList(genreDAO.getAllGenreNames()));

        if (existing != null && existing.getId() != 0) {
            nameField.setText(existing.getName());

            Genre g = genreDAO.findById(existing.getGenreId());
            if (g != null) {
                mainGenreCombo.setValue(g.getName());
            }

            numBooksField.setText(String.valueOf(existing.getNumBooks()));
            urlFieldSub.setText(existing.getUrl());

        } else {
            numBooksField.setText("0");
        }
    }

    public void setupAsBook(Book existing) {
        type = FormType.BOOK;
        formTitle.setText(existing == null || existing.getId() == 0 ?
                "Add Book" : "Edit Book");
        toggleSections(false, false, true);
        this.entity = existing;

        // ─── Populate Book TYPE ComboBox ALWAYS ────────────────────────────
        typeCombo.setItems(FXCollections.observableArrayList(
                "Paperback",
                "Kindle",
                "Audiobook",
                "Hardcover"
        ));

        // Populate Genres
        List<String> genres = genreDAO.getAllGenreNames();
        genreCombo.setItems(FXCollections.observableArrayList(genres));

        // Populate SubGenres dynamically
        Map<String, List<String>> subGenreMap = subGenreDAO.getAllSubGenreNamesDetailed();
        if (subGenreMap == null || subGenreMap.isEmpty()) {
            subGenreMap = Map.of();
        }

        Map<String, List<String>> finalMap = subGenreMap;
        genreCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && finalMap.containsKey(newVal)) {
                subGenreCombo.setItems(FXCollections.observableArrayList(finalMap.get(newVal)));
            } else {
                subGenreCombo.getItems().clear();
            }
        });

        // fill in existing data on edit
        if (existing != null && existing.getId() != 0) {

            nameField.setText(existing.getTitle());
            authorField.setText(existing.getAuthor());

            // Set genre by ID
            Genre g = genreDAO.findById(existing.getGenreId());
            if (g != null) {
                genreCombo.setValue(g.getName());
            }

            // Set subgenre by ID
            SubGenre sg = subGenreDAO.findById(existing.getSubGenreId());
            if (sg != null) {
                subGenreCombo.setValue(sg.getName());
            }

            typeCombo.setValue(existing.getType());
            priceField.setText(String.valueOf(existing.getPrice()));
            ratingField.setText(String.valueOf(existing.getRating()));
            numRatedField.setText(String.valueOf(existing.getNumRated()));
            urlFieldBook.setText(existing.getUrl());
        }
    }


    private void saveEntity() {
        try {
            switch (type) {
                case GENRE -> saveGenre();
                case SUBGENRE -> saveSubGenre();
                case BOOK -> saveBook();
            }

            if (refreshCallback != null) refreshCallback.run();
            closeForm();

        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
        } catch (Exception e) {
            showError("Save Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveGenre() {
        String name = validateNotEmpty(nameField, "Genre name");
        int count = validatePositiveInt(numSubGenresField, "Number of sub-genres");
        String url = validateUrl(urlField, "URL");

        Genre g = (entity instanceof Genre existing && existing.getId() != 0)
                ? new Genre(existing.getId(), name, count, url)
                : new Genre(0, name, count, url);

        if (g.getId() != 0) {
            genreDAO.update(g);
        } else {
            genreDAO.insert(g);
        }
    }

    private void saveSubGenre() {
        String name = validateNotEmpty(nameField, "Sub-genre name");
        String mainGenre = mainGenreCombo.getValue();

        if (mainGenre == null || mainGenre.isBlank())
            throw new IllegalArgumentException("Please select a main genre");

        int count = validatePositiveInt(numBooksField, "Number of books");
        String url = validateUrl(urlFieldSub, "URL");
        int genreId = genreDAO.findIdByName(mainGenre);

        SubGenre sg = (entity instanceof SubGenre existing && existing.getId() != 0)
                ? new SubGenre(existing.getId(), name, mainGenre, count, url, genreId)
                : new SubGenre(0, name, mainGenre, count, url, genreId);
        if (sg.getId() != 0) {
            subGenreDAO.update(sg);
        } else {
            subGenreDAO.insert(sg);
        }
        genreDAO.recountSubGenres(genreId);
    }

    private void saveBook() {
        Book existing = (entity instanceof Book ex && ex.getId() != 0) ? ex : null;
        int oldSub = existing != null ? existing.getSubGenreId() : 0;

        String title = validateNotEmpty(nameField, "Title");
        String author = validateNotEmpty(authorField, "Author");
        String mainGenre = genreCombo.getValue();
        String subGenre = subGenreCombo.getValue();

        if (mainGenre == null || mainGenre.isBlank())
            throw new IllegalArgumentException("Please select a genre");

        if (subGenre == null || subGenre.isBlank())
            throw new IllegalArgumentException("Please select a sub-genre");

        String typeVal = typeCombo.getValue();
        if (typeVal == null || typeVal.isBlank()) {
            throw new IllegalArgumentException("Please select a type");
        }
        double price = validatePositiveDouble(priceField, "Price");
        double rating = validateRating(ratingField);
        int numRated = validatePositiveInt(numRatedField, "Number of ratings");
        String url = validateUrl(urlFieldBook, "URL");

        int genreId = genreDAO.findIdByName(mainGenre);

        int subGenreId = subGenreDAO.getAll().stream()
                .filter(sg -> sg.getGenreId() == genreId)
                .filter(sg -> sg.getName().equals(subGenre))
                .mapToInt(SubGenre::getId)
                .findFirst()
                .orElse(0);

        Book book = (existing != null)
                ? new Book(existing.getId(), title, author, mainGenre, subGenre, typeVal,
                price, rating, numRated, url, genreId, subGenreId)
                : new Book(0, title, author, mainGenre, subGenre, typeVal,
                price, rating, numRated, url, genreId, subGenreId);

        if (book.getId() != 0) {
            bookDAO.update(book);

            if (oldSub != subGenreId) {
                if (oldSub > 0) subGenreDAO.decrementBookCount(oldSub);
                if (subGenreId > 0) subGenreDAO.incrementBookCount(subGenreId);
            }

        } else {
            bookDAO.insert(book);

            if (subGenreId > 0)
                subGenreDAO.incrementBookCount(subGenreId);
        }
    }

    private void toggleSections(boolean g, boolean sg, boolean b) {
        genreSection.setVisible(g); genreSection.setManaged(g);
        subGenreSection.setVisible(sg); subGenreSection.setManaged(sg);
        if (bookSection != null) { bookSection.setVisible(b); bookSection.setManaged(b); }
    }

    private void closeForm() {
        ((Stage) saveBtn.getScene().getWindow()).close();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    public void setRefreshCallback(Runnable cb) { this.refreshCallback = cb; }

    public static void open(Window owner, Object entity, Runnable refreshCallback) {
        try {
            FXMLLoader loader = new FXMLLoader(FormController.class.getResource(
                    "/com/example/ece318_librarymanagementsys/EntityForm.fxml"));
            Parent root = loader.load();
            FormController ctrl = loader.getController();

            if (entity instanceof Genre g) ctrl.setupAsGenre(g);
            else if (entity instanceof SubGenre sg) ctrl.setupAsSubGenre(sg);
            else if (entity instanceof Book b) ctrl.setupAsBook(b);

            ctrl.setRefreshCallback(refreshCallback);

            String entityType = entity instanceof Genre ? "Genre" :
                    entity instanceof SubGenre ? "SubGenre" : "Book";
            boolean isNew = (entity instanceof Genre && ((Genre) entity).getId() == 0)
                    || (entity instanceof SubGenre && ((SubGenre) entity).getId() == 0)
                    || (entity instanceof Book && ((Book) entity).getId() == 0);
            String action = isNew ? "Add" : "Edit";

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(action + " " + entityType);
            stage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) stage.initOwner(owner);
            stage.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Could not open form: " + e.getMessage(), ButtonType.OK);
            alert.setTitle("Form Error");
            alert.showAndWait();
        }
    }
}