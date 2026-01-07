package com.example.ece318_librarymanagementsys;

import com.example.ece318_librarymanagementsys.controller.*;
import com.example.ece318_librarymanagementsys.controller.core.*;
import com.example.ece318_librarymanagementsys.database.*;
import com.example.ece318_librarymanagementsys.model.*;
import com.example.ece318_librarymanagementsys.util.*;
import javafx.application.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;

import java.util.*;

public class MainController {

    // Observable collections
    private final ObservableList<Book> booksList = FXCollections.observableArrayList();
    private final ObservableList<Genre> genresList = FXCollections.observableArrayList();
    private final ObservableList<SubGenre> subGenresList = FXCollections.observableArrayList();

    // Cached statistics
    private final Map<String, GenreDAO.GenreStat> genreStatistics = new HashMap<>();
    private final Map<String, SubGenreDAO.SubGenreStat> subGenreStatistics = new HashMap<>();

    // DAO instances
    private final BookDAO bookDataAccess = new BookDAO();
    private final GenreDAO genreDataAccess = new GenreDAO();
    private final SubGenreDAO subGenreDataAccess = new SubGenreDAO();

    // Controllers
    private TableController<Book> bookTableController;
    private TableController<Genre> genreTableController;
    private TableController<SubGenre> subGenreTableController;

    private SearchController<Book> bookSearchController;
    private SearchController<Genre> genreSearchController;
    private SearchController<SubGenre> subGenreSearchController;

    private GenreDetailsController genreDetailsController;
    private BookDetailsController bookDetailsController;
    private BookFileController fileOperationsController;
    private CRUDHelper crudOperationsHelper;

    // Books tab
    @FXML private Button loadBooksButton;
    @FXML private Button exportBooksButton;
    @FXML private TableView<Book> booksTable;
    @FXML private TextField searchBooksField;
    @FXML private CheckComboBox<String> filterGenreBox;
    @FXML private CheckComboBox<String> filterSubGenreBox;
    @FXML private HBox booksContentArea;
    @FXML private VBox bookDetailsPanel;
    @FXML private Button closeDetailsBtn;
    @FXML private Label lblBookTitle;
    @FXML private Label lblBookAuthor;
    @FXML private Label lblBookGenre;
    @FXML private Label lblBookSubGenre;
    @FXML private Label lblBookType;
    @FXML private Label lblBookPrice;
    @FXML private Label lblBookRating;
    @FXML private Label lblBookRatingsCount;
    @FXML private Hyperlink lblBookURL;

    // Genres tab
    @FXML private Button exportGenresButton;
    @FXML private Button reloadGenresButton;
    @FXML private TableView<Genre> genresTable;
    @FXML private TextField searchGenreField;
    @FXML private HBox genresContentArea;
    @FXML private VBox genreDetailsPanel;
    @FXML private Button closeGenreDetailsBtn;
    @FXML private Label lblGenreName;
    @FXML private Label lblGenreTotalBooks;
    @FXML private Label lblGenreAvgRating;
    @FXML private Label lblGenreAvgPrice;
    @FXML private ListView<String> lvGenreSubGenres;
    @FXML private Hyperlink lblGenreURL;

    // Subgenres tab
    @FXML private Button exportSubGenresButton;
    @FXML private Button reloadSubGenresButton;
    @FXML private TableView<SubGenre> subGenresTable;
    @FXML private TextField searchSubGenreField;

    // State
    private HostServices hostServices;
    private boolean isFullyInitialized = false;

    // Initialization entrypoint
    @FXML
    private void initialize() {
        initializeControllers();
        loadAllData();
        setupCRUDHelper();
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
        if (!isFullyInitialized) {
            initializeControllers();
            loadAllData();
            isFullyInitialized = true;
        }
    }

    private void initializeControllers() {
        initializeTableControllers();
        initializeDetailPanels();
        initializeFileController();
    }

    private void initializeTableControllers() {
        bookTableController = TableController.forBooks(booksTable, hostServices);
        genreTableController = TableController.forGenres(genresTable, hostServices, genreStatistics);
        subGenreTableController = TableController.forSubGenres(subGenresTable, hostServices, subGenreStatistics);
    }

    private void initializeDetailPanels() {
        bookDetailsController = BookDetailsController.create(
                booksContentArea, bookDetailsPanel, closeDetailsBtn, booksTable,
                lblBookTitle, lblBookAuthor, lblBookGenre, lblBookSubGenre,
                lblBookType, lblBookPrice, lblBookRating, lblBookRatingsCount,
                lblBookURL, hostServices
        );

        genreDetailsController = GenreDetailsController.create(
                genresContentArea, genreDetailsPanel, closeGenreDetailsBtn, genresTable,
                lblGenreName, lblGenreTotalBooks, lblGenreAvgRating, lblGenreAvgPrice,
                lvGenreSubGenres, lblGenreURL, hostServices
        );
    }

    private void initializeFileController() {
        fileOperationsController = new BookFileController(
                bookDataAccess, booksList, genresList, subGenresList
        );
    }

    private void setupCRUDHelper() {
        crudOperationsHelper = new CRUDHelper(
                fileOperationsController, bookDataAccess, genreDataAccess, subGenreDataAccess
        );

        Platform.runLater(() ->
                crudOperationsHelper.setOwner(loadBooksButton.getScene().getWindow())
        );
    }

    private void loadAllData() {
        loadBooksData();
        loadGenresData();
        loadSubGenresData();
        attachSelectionHandlers();
    }

    private void loadBooksData() {
        booksList.setAll(bookDataAccess.getAll());
        bookTableController.setItems(booksList);

        bookSearchController = new SearchController<>(
                booksTable, bookTableController.getFiltered(),
                searchBooksField, Book::getSearchText
        );

        bookSearchController.enableAdvancedFilters(
                filterGenreBox, filterSubGenreBox,
                Book::getResolvedMainGenreCached,
                Book::getResolvedSubGenreCached
        );
    }

    private void loadGenresData() {
        refreshGenreStatistics();
        genreTableController.setItems(genresList);

        genreSearchController = new SearchController<>(
                genresTable, genreTableController.getFiltered(),
                searchGenreField, Genre::getName
        );
    }

    private void loadSubGenresData() {
        refreshSubGenreStatistics();
        subGenreTableController.setItems(subGenresList);

        subGenreSearchController = new SearchController<>(
                subGenresTable, subGenreTableController.getFiltered(),
                searchSubGenreField, sg -> sg.getName() + " " + sg.getMainGenre()
        );
    }

    private void attachSelectionHandlers() {
        bookTableController.onSelect(bookDetailsController::showEntity);
        genreTableController.onSelect(this::displayGenreDetails);
    }

    private void refreshGenreStatistics() {
        genreStatistics.clear();
        List<GenreDAO.GenreStat> stats = genreDataAccess.getGenreStats();
        stats.forEach(s -> genreStatistics.put(s.name, s));
        genresList.setAll(genreDataAccess.getAll());
    }

    private void refreshSubGenreStatistics() {
        subGenreStatistics.clear();
        List<SubGenreDAO.SubGenreStat> stats = subGenreDataAccess.getSubGenreStats();
        stats.forEach(s -> subGenreStatistics.putIfAbsent(s.name, s));
        subGenresList.setAll(subGenreDataAccess.getAll());
    }

    @FXML
    private void onLoadBooks() {
        fileOperationsController.loadBooksFromCSV(loadBooksButton.getScene().getWindow());
        refreshAllData();
        bookSearchController.refreshFilters();
    }

    @FXML
    private void onAddBook() {
        crudOperationsHelper.addBook(this::refreshAllData);
    }

    @FXML
    private void onEditBook() {
        crudOperationsHelper.editBook(booksTable, this::refreshAllData);
    }

    @FXML
    private void onDeleteBook() {
        crudOperationsHelper.deleteBook(booksTable, this::refreshAllData);
    }

    @FXML
    private void onExportBooks() {
        exportData(exportBooksButton, booksTable.getItems());
    }

    @FXML
    private void onCloseBookDetails() {
        bookDetailsController.hide();
    }

    @FXML
    private void onAddGenre() {
        crudOperationsHelper.addGenre(this::refreshAllData);
    }

    @FXML
    private void onEditGenre() {
        crudOperationsHelper.editGenre(genresTable, this::refreshAllData);
    }

    @FXML
    private void onDeleteGenre() {
        crudOperationsHelper.deleteGenre(genresTable, this::refreshAllData);
    }

    @FXML
    private void onExportGenres() {
        exportData(exportGenresButton, genresTable.getItems());
    }

    @FXML
    private void onReloadGenres() {
        if (!confirmReload("Genres", reloadGenresButton)) return;
        CSVLoader.reloadGenres(genreDataAccess);
        refreshAllData();
        fileOperationsController.showInformation("Genres reloaded successfully!");
    }

    @FXML
    private void onCloseGenreDetails() {
        genreDetailsController.hide();
    }

    @FXML
    private void onAddSubGenre() {
        crudOperationsHelper.addSubGenre(this::refreshAllData);
    }

    @FXML
    private void onEditSubGenre() {
        crudOperationsHelper.editSubGenre(subGenresTable, this::refreshAllData);
    }

    @FXML
    private void onDeleteSubGenre() {
        crudOperationsHelper.deleteSubGenre(subGenresTable, this::refreshAllData);
    }

    @FXML
    private void onExportSubGenres() {
        exportData(exportSubGenresButton, subGenresTable.getItems());
    }

    @FXML
    private void onReloadSubGenres() {
        if (!confirmReload("Sub-Genres", reloadSubGenresButton)) return;
        CSVLoader.reloadSubGenres(subGenreDataAccess, genreDataAccess);
        refreshAllData();
        fileOperationsController.showInformation("Sub-Genres reloaded successfully!");
    }

    private void refreshAllData() {
        booksList.setAll(bookDataAccess.getAll());
        bookTableController.refreshItems(booksList);
        refreshBookCaches();

        if (bookSearchController != null) {
            bookSearchController.updateFilteredList(bookTableController.getFiltered());
            bookSearchController.refreshFilters();
        }

        refreshGenreStatistics();
        genreTableController.refreshItems(genresList);

        if (genreSearchController != null) {
            genreSearchController.updateFilteredList(genreTableController.getFiltered());
        }

        refreshSubGenreStatistics();
        subGenreTableController.refreshItems(subGenresList);

        if (subGenreSearchController != null) {
            subGenreSearchController.updateFilteredList(subGenreTableController.getFiltered());
        }
    }

    private void refreshBookCaches() {
        for (Book b : booksList) {
            b.refreshResolveCache();
        }
    }

    private void displayGenreDetails(Genre genre) {
        if (genre == null) {
            genreDetailsController.hide();
            return;
        }

        GenreDAO.GenreStat stats = genreStatistics.get(genre.getName());

        List<String> related = subGenresList.stream()
                .filter(sg -> sg.getGenreId() == genre.getId())
                .map(SubGenre::getName)
                .sorted()
                .toList();

        genreDetailsController.showWithStats(genre, stats, related);
    }

    private void exportData(Button sourceButton, List<?> dataList) {
        try {
            if (dataList == null || dataList.isEmpty()) {
                fileOperationsController.showInformation("No data to export.");
                return;
            }

            boolean ok = PDFExporter.export(sourceButton.getScene().getWindow(), dataList);
            if (ok)
                fileOperationsController.showInformation("Export successful!");

        } catch (Exception e) {
            fileOperationsController.showError("Export Error", "Export failed", e.getMessage());
        }
    }

    private boolean confirmReload(String entityType, Button sourceButton) {
        String message = "This will delete all " + entityType.toLowerCase() + " and reload them from CSV.\n\nContinue?";
        return fileOperationsController.showConfirmation(
                sourceButton.getScene().getWindow(),
                "Reload " + entityType + " CSV",
                message
        );
    }
}