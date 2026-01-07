package com.example.ece318_librarymanagementsys.controller;

import com.example.ece318_librarymanagementsys.database.GenreDAO;
import com.example.ece318_librarymanagementsys.model.Genre;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;


public class GenreDetailsController extends DetailsPanelController<Genre> {

    // UI components specific to Genre
    private final Label lblName;
    private final Label lblTotalBooks;
    private final Label lblAvgRating;
    private final Label lblAvgPrice;
    private final ListView<String> lvSubGenres;
    private final Hyperlink lblUrl;

    // Statistics data holder
    private GenreDAO.GenreStat currentStats;
    private List<String> currentSubGenres;

    public GenreDetailsController(HBox contentArea,
                                  VBox detailsPanel,
                                  Button closeButton,
                                  TableView<Genre> table,
                                  Label lblName,
                                  Label lblTotalBooks,
                                  Label lblAvgRating,
                                  Label lblAvgPrice,
                                  ListView<String> lvSubGenres,
                                  Hyperlink lblUrl) {
        super(contentArea, detailsPanel, closeButton, table);

        this.lblName = lblName;
        this.lblTotalBooks = lblTotalBooks;
        this.lblAvgRating = lblAvgRating;
        this.lblAvgPrice = lblAvgPrice;
        this.lvSubGenres = lvSubGenres;
        this.lblUrl = lblUrl;
    }

    public void showWithStats(Genre genre, GenreDAO.GenreStat stats, List<String> subGenres) {
        this.currentStats = stats;
        this.currentSubGenres = subGenres;
        showEntity(genre);
    }

    @Override
    protected void populateDetails(Genre genre) {
        lblName.setText(safeText(genre.getName()));

        int totalBooks = currentStats != null ? currentStats.totalBooks : 0;
        double avgRating = currentStats != null ? currentStats.avgRating : 0.0;
        double avgPrice = currentStats != null ? currentStats.avgPrice : 0.0;

        lblTotalBooks.setText("Total Books: " + totalBooks);
        lblAvgRating.setText(String.format("Avg Rating: %.2f", avgRating));
        lblAvgPrice.setText(String.format("Avg Price: ₹%.2f", avgPrice));

        List<String> subGenreList = currentSubGenres != null ? currentSubGenres : List.of();
        lvSubGenres.setItems(FXCollections.observableArrayList(subGenreList));

        setupHyperlink(lblUrl, "View on Amazon", genre.getUrl());
    }

    public static GenreDetailsController create(
            HBox area, VBox panel, Button close, TableView<Genre> table,
            Label name, Label books, Label rating, Label price,
            ListView<String> subs, Hyperlink url, HostServices hs) {

        GenreDetailsController controller = new GenreDetailsController(
                area, panel, close, table, name, books, rating, price, subs, url
        );
        controller.setHostServices(hs);
        return controller;
    }
}