package com.example.ece318_librarymanagementsys.model;

import com.example.ece318_librarymanagementsys.database.*;

public class Book extends BaseEntity {

    private String title;
    private String author;
    private String mainGenre;
    private String subGenre;
    private String type;
    private double price;
    private double rating;
    private int numRated;
    private String url;
    private int genreId;
    private int subGenreId;

    private transient String searchText;

    public Book(int id, String title, String author, String mainGenre,
                String subGenre, String type, double price, double rating,
                int numRated, String url, int genreId, int subGenreId) {
        super(id);
        this.title = title;
        this.author = author;
        this.mainGenre = mainGenre;
        this.subGenre = subGenre;
        this.type = type;
        this.price = price;
        this.rating = rating;
        this.numRated = numRated;
        this.url = url;
        this.genreId = genreId;
        this.subGenreId = subGenreId;
    }

    // Getters / setters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getMainGenre() { return mainGenre; }
    public String getSubGenre() { return subGenre; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public double getRating() { return rating; }
    public int getNumRated() { return numRated; }
    public String getUrl() { return url; }
    public int getGenreId() { return genreId; }
    public int getSubGenreId() { return subGenreId; }

    public String getSearchText() {
        if (searchText == null) {
            String raw = (title == null ? "" : title) + " " +
                    (author == null ? "" : author);
            searchText = raw.toLowerCase();
        }
        return searchText;
    }

    private transient String resolvedMainGenre;
    private transient String resolvedSubGenre;

    public String getResolvedMainGenreCached() {
        if (resolvedMainGenre == null) {
            Genre g = new GenreDAO().findById(genreId);
            resolvedMainGenre = (g != null) ? g.getName() : mainGenre;
        }
        return resolvedMainGenre;
    }

    public String getResolvedSubGenreCached() {
        if (resolvedSubGenre == null) {
            SubGenre sg = new SubGenreDAO().findById(subGenreId);
            resolvedSubGenre = (sg != null) ? sg.getName() : subGenre;
        }
        return resolvedSubGenre;
    }

    public void refreshResolveCache() {
        resolvedMainGenre = null;
        resolvedSubGenre = null;
    }

    @Override
    public String toString() {
        return title + " by " + author + " (" + mainGenre + " / " + subGenre + ")";
    }
}
