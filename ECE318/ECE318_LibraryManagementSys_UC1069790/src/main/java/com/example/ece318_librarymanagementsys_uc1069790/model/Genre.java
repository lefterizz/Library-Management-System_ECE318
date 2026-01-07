package com.example.ece318_librarymanagementsys_uc1069790.model;

public class Genre extends BaseEntity {

    private String name;
    private int numSubGenres;
    private String url;

    public Genre(int id, String name, int numSubGenres, String url) {
        super(id);
        this.name = name;
        this.numSubGenres = numSubGenres;
        this.url = url;
    }

    public String getName() { return name; }
    public int getNumSubGenres() { return numSubGenres; }
    public String getUrl() { return url; }

    @Override
    public String toString() { return name; }
}
