package com.example.ece318_librarymanagementsys.model;

public class SubGenre extends BaseEntity {

    private String name;
    private String mainGenre;
    private int numBooks;
    private String url;
    private int genreId;

    public SubGenre(int id, String name, String mainGenre,
                    int numBooks, String url, int genreId) {
        super(id);
        this.name = name;
        this.mainGenre = mainGenre;
        this.numBooks = numBooks;
        this.url = url;
        this.genreId = genreId;
    }

    public String getName() { return name; }
    public String getMainGenre() { return mainGenre; }
    public int getNumBooks() { return numBooks; }
    public String getUrl() { return url; }
    public int getGenreId() { return genreId; }
}
