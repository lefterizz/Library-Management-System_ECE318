package com.example.ece318_librarymanagementsys.util;

import com.example.ece318_librarymanagementsys.controller.BookFileController;
import com.example.ece318_librarymanagementsys.controller.core.FormController;
import com.example.ece318_librarymanagementsys.database.*;
import com.example.ece318_librarymanagementsys.model.*;
import javafx.scene.control.TableView;
import javafx.stage.Window;

public class CRUDHelper {

    private Window owner;
    private final BookFileController dialog;

    private final BookDAO bookDAO;
    private final GenreDAO genreDAO;
    private final SubGenreDAO subGenreDAO;

    public CRUDHelper(BookFileController dialog,
                      DAO<Book> bookDAO,
                      DAO<Genre> genreDAO,
                      DAO<SubGenre> subGenreDAO) {

        this.dialog = dialog;
        this.bookDAO = (BookDAO) bookDAO;
        this.genreDAO = (GenreDAO) genreDAO;
        this.subGenreDAO = (SubGenreDAO) subGenreDAO;
    }

    public void setOwner(Window owner) {
        this.owner = owner;
    }

    // BOOK CRUD
    public void addBook(Runnable refresh) {
        Book blank = new Book(0, "", "", "", "", "",
                0, 0, 0, "", 0, 0);

        FormController.open(owner, blank, () -> {

            if (blank.getId() != 0) {
                subGenreDAO.incrementBookCount(blank.getSubGenreId());
            }

            refresh.run();
        });
    }


    public void editBook(TableView<Book> table, Runnable refresh) {

        Book original = table.getSelectionModel().getSelectedItem();
        if (original == null) {
            dialog.showInformation("Please select a book to edit.");
            return;
        }

        // Make a copy to detect changes
        int oldSub = original.getSubGenreId();

        FormController.open(owner, original, () -> {

            // original now contains updated values
            int newSub = original.getSubGenreId();

            if (oldSub != newSub) {
                subGenreDAO.decrementBookCount(oldSub);
                subGenreDAO.incrementBookCount(newSub);
            }

            refresh.run();
        });
    }

    public void deleteBook(TableView<Book> table, Runnable refresh) {

        Book selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialog.showInformation("Please select a book to delete.");
            return;
        }

        if (!dialog.showConfirmation(
                owner,
                "Delete Book",
                "Are you sure you want to delete:\n\"" + selected.getTitle() + "\"?"
        )) return;

        subGenreDAO.decrementBookCount(selected.getSubGenreId());

        bookDAO.deleteById(selected.getId());
        refresh.run();
        dialog.showInformation("Book deleted successfully!");
    }

    // GENRE CRUD
    public void addGenre(Runnable refresh) {
        Genre g = new Genre(0, "", 0, "");
        FormController.open(owner, g, refresh);
    }

    public void editGenre(TableView<Genre> table, Runnable refresh) {
        Genre selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialog.showInformation("Please select a genre to edit.");
            return;
        }
        FormController.open(owner, selected, refresh);
    }

    public void deleteGenre(TableView<Genre> table, Runnable refresh) {
        Genre selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialog.showInformation("Please select a genre to delete.");
            return;
        }

        if (dialog.showConfirmation(
                owner,
                "Delete Genre",
                "Are you sure you want to delete:\n\"" + selected.getName() + "\"?"
        )) {
            genreDAO.deleteById(selected.getId());
            refresh.run();
            dialog.showInformation("Genre deleted successfully!");
        }
    }

    // SUBGENRE CRUD
    public void addSubGenre(Runnable refresh) {
        SubGenre sg = new SubGenre(0, "", "", 0, "", 0);
        FormController.open(owner, sg, refresh);
    }

    public void editSubGenre(TableView<SubGenre> table, Runnable refresh) {

        SubGenre selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialog.showInformation("Please select a sub-genre to edit.");
            return;
        }
        FormController.open(owner, selected, refresh);
    }

    public void deleteSubGenre(TableView<SubGenre> table, Runnable refresh) {
        SubGenre selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialog.showInformation("Please select a sub-genre to delete.");
            return;
        }

        if (dialog.showConfirmation(
                owner,
                "Delete Sub-Genre",
                "Are you sure you want to delete:\n\"" + selected.getName() + "\"?"
        )) {
            subGenreDAO.deleteById(selected.getId());
            genreDAO.recountSubGenres(selected.getGenreId());
            refresh.run();
            dialog.showInformation("Sub-genre deleted successfully!");
        }
    }
}
