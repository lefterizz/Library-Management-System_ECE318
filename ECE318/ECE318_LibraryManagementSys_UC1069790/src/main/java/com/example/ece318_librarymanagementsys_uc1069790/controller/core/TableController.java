package com.example.ece318_librarymanagementsys_uc1069790.controller.core;

import com.example.ece318_librarymanagementsys_uc1069790.database.*;
import com.example.ece318_librarymanagementsys_uc1069790.model.*;
import javafx.application.HostServices;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

//Generic reusable TableController for any model type.
public class TableController<T> {

    private final TableView<T> table;
    private FilteredList<T> filtered;
    private SortedList<T> sorted;
    private HostServices hostServices;

    public TableController(TableView<T> table) {
        this.table = table;
    }

    // binds existing FXML columns to given fields in order.
    public void setupTable(String... fieldNames) {
        ObservableList<TableColumn<T, ?>> columns = table.getColumns();

        for (int i = 0; i < fieldNames.length && i < columns.size(); i++) {
            String fieldName = fieldNames[i];
            if (fieldName == null || fieldName.isBlank()) {
                continue;
            }
            TableColumn<T, Object> col = (TableColumn<T, Object>) columns.get(i);
            col.setCellValueFactory(new PropertyValueFactory<>(fieldName));
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // observable list to the table with sorting and filtering
    public void setItems(ObservableList<T> list) {
        filtered = new FilteredList<>(list, t -> true);
        sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
    }

    public FilteredList<T> getFiltered() {
        return filtered;
    }

    // allows reusing the controller
    public void refreshItems(ObservableList<T> list) {
        setItems(list);
    }

    // Updates host services for hyperlink column
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    // selection change handler
    public void onSelect(Consumer<T> handler) {
        table.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> handler.accept(newVal));
    }

    public <R> void addComputedColumn(TableColumn<T, R> column, Function<T, R> valueProvider) {
        column.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(valueProvider.apply(cd.getValue())));
    }

    public void setupHyperlinkColumn(TableColumn<T, String> column, String linkText) {
        column.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();

            {
                link.setStyle("-fx-text-fill: #000000;");
                link.setOnAction(e -> {
                    String url = (String) link.getUserData();
                    if (url != null && hostServices != null) {
                        hostServices.showDocument(url);
                    }
                });
            }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isBlank()) {
                    setGraphic(null);
                } else {
                    link.setText(linkText);
                    link.setUserData(url);
                    setGraphic(link);
                }
            }
        });
    }

    // Helpers
    public static TableController<Book> forBooks(TableView<Book> table, HostServices hs) {
        var ctrl = new TableController<Book>(table);

        ctrl.setupTable("title", "author", null, null, "price", "rating");

        // Column 2 = Genre name
        ctrl.addComputedColumn(
                (TableColumn<Book, String>) table.getColumns().get(2),
                Book::getMainGenre
        );

        // Column 3 = Subgenre name
        ctrl.addComputedColumn(
                (TableColumn<Book, String>) table.getColumns().get(3),
                Book::getSubGenre
        );


        ctrl.setHostServices(hs);
        return ctrl;
    }

    public static TableController<Genre> forGenres(TableView<Genre> table, HostServices hs,
                                                   Map<String, GenreDAO.GenreStat> stats) {
        var ctrl = new TableController<Genre>(table);
        ctrl.setupTable("name", "numSubGenres", null, null, null, "url");
        ctrl.addComputedColumn((TableColumn<Genre, Integer>) table.getColumns().get(2),
                g -> Optional.ofNullable(stats.get(g.getName())).map(s -> s.totalBooks).orElse(0));
        ctrl.addComputedColumn((TableColumn<Genre, Double>) table.getColumns().get(3),
                g -> Optional.ofNullable(stats.get(g.getName())).map(s -> s.avgRating).orElse(0.0));
        ctrl.addComputedColumn((TableColumn<Genre, Double>) table.getColumns().get(4),
                g -> Optional.ofNullable(stats.get(g.getName())).map(s -> s.avgPrice).orElse(0.0));
        ctrl.setupHyperlinkColumn((TableColumn<Genre, String>) table.getColumns().get(5), "View on Amazon");
        ctrl.setHostServices(hs);
        return ctrl;
    }

    public static TableController<SubGenre> forSubGenres(TableView<SubGenre> table, HostServices hs,
                                                         Map<String, SubGenreDAO.SubGenreStat> stats) {

        var ctrl = new TableController<SubGenre>(table);
        ctrl.setupTable("name", null, null, null, null, "url");

        GenreDAO genreDAO = new GenreDAO();
        // column index 1
        ctrl.addComputedColumn(
                (TableColumn<SubGenre, String>) table.getColumns().get(1),
                sg -> {
                    // resolve by genre_id first
                    Genre g = genreDAO.findById(sg.getGenreId());
                        return g.getName();
                }
        );

        // Stats
        ctrl.addComputedColumn((TableColumn<SubGenre, Integer>) table.getColumns().get(2),
                s -> Optional.ofNullable(stats.get(s.getName())).map(v -> v.totalBooks).orElse(0));

        ctrl.addComputedColumn((TableColumn<SubGenre, Double>) table.getColumns().get(3),
                s -> Optional.ofNullable(stats.get(s.getName())).map(v -> v.avgRating).orElse(0.0));

        ctrl.addComputedColumn((TableColumn<SubGenre, Double>) table.getColumns().get(4),
                s -> Optional.ofNullable(stats.get(s.getName())).map(v -> v.avgPrice).orElse(0.0));

        ctrl.setupHyperlinkColumn((TableColumn<SubGenre, String>) table.getColumns().get(5), "View on Amazon");
        ctrl.setHostServices(hs);

        return ctrl;
    }



}
