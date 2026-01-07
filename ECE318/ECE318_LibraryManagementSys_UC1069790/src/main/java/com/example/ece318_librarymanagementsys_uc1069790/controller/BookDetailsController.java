package com.example.ece318_librarymanagementsys_uc1069790.controller;

import com.example.ece318_librarymanagementsys_uc1069790.controller.DetailsPanelController;
import com.example.ece318_librarymanagementsys_uc1069790.database.*;
import com.example.ece318_librarymanagementsys_uc1069790.model.*;
import javafx.application.HostServices;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

//Concrete implementation for Book details panel, Demonstrates inheritance

public class BookDetailsController extends DetailsPanelController<Book> {

    // UI Labels for book information
    private final Label lblTitle;
    private final Label lblAuthor;
    private final Label lblGenre;
    private final Label lblSubGenre;
    private final Label lblType;
    private final Label lblPrice;
    private final Label lblRating;
    private final Label lblRatingsCount;
    private final Hyperlink lblUrl;

    // DAOs for resolving foreign keys
    private final GenreDAO genreDAO;
    private final SubGenreDAO subGenreDAO;

    public BookDetailsController(HBox contentArea,
                                 VBox detailsPanel,
                                 Button closeButton,
                                 TableView<Book> table,
                                 Label lblTitle,
                                 Label lblAuthor,
                                 Label lblGenre,
                                 Label lblSubGenre,
                                 Label lblType,
                                 Label lblPrice,
                                 Label lblRating,
                                 Label lblRatingsCount,
                                 Hyperlink lblUrl) {
        super(contentArea, detailsPanel, closeButton, table);

        this.lblTitle = lblTitle;
        this.lblAuthor = lblAuthor;
        this.lblGenre = lblGenre;
        this.lblSubGenre = lblSubGenre;
        this.lblType = lblType;
        this.lblPrice = lblPrice;
        this.lblRating = lblRating;
        this.lblRatingsCount = lblRatingsCount;
        this.lblUrl = lblUrl;

        // Initialize DAOs
        this.genreDAO = new GenreDAO();
        this.subGenreDAO = new SubGenreDAO();
    }

    @Override
    protected void populateDetails(Book book) {
        lblTitle.setText(safeText(book.getTitle()));
        lblAuthor.setText("Author: " + safeText(book.getAuthor()));

        // Resolve genre and subgenre by ID for accuracy
        Genre genre = genreDAO.findById(book.getGenreId());
        SubGenre subGenre = subGenreDAO.findById(book.getSubGenreId());

        lblGenre.setText("Genre: " + (genre != null ? genre.getName() : book.getMainGenre()));
        lblSubGenre.setText("Sub-Genre: " + (subGenre != null ? subGenre.getName() : book.getSubGenre()));
        lblType.setText("Type: " + safeText(book.getType()));
        lblPrice.setText(String.format("Price: ₹%.2f", book.getPrice()));
        lblRating.setText("Rating: " + formatNumber(book.getRating(), "%.1f"));
        lblRatingsCount.setText("No. Of Ratings: " + book.getNumRated());

        setupHyperlink(lblUrl, "View on Amazon", book.getUrl());
    }

    public static BookDetailsController create(
            HBox area, VBox panel, Button close, TableView<Book> table,
            Label title, Label author, Label genre, Label sub,
            Label type, Label price, Label rating, Label count,
            Hyperlink url, HostServices hs) {

        BookDetailsController controller = new BookDetailsController(
                area, panel, close, table, title, author, genre, sub,
                type, price, rating, count, url
        );
        controller.setHostServices(hs);
        return controller;
    }
}