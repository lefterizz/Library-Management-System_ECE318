package com.example.ece318_librarymanagementsys_uc1069790.util;

import com.example.ece318_librarymanagementsys_uc1069790.database.*;
import com.example.ece318_librarymanagementsys_uc1069790.model.*;
import java.io.*;
import java.util.*;

public class CSVLoader {

    public static List<Book> loadBooks(File csvFile,
                                       List<Genre> genres,
                                       List<SubGenre> subGenres) throws IOException {
        List<Book> books = new ArrayList<>();

        // Build lookup maps BY NAME
        Map<String, Integer> genreIdMap = new HashMap<>();
        for (Genre g : genres) {
            genreIdMap.put(g.getName().toLowerCase().trim(), g.getId());
        }

        Map<String, Integer> subGenreIdMap = new HashMap<>();
        Map<Integer, Integer> subGenreToGenreMap = new HashMap<>();
        for (SubGenre sg : subGenres) {
            subGenreIdMap.put(sg.getName().toLowerCase().trim(), sg.getId());
            subGenreToGenreMap.put(sg.getId(), sg.getGenreId());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine();
            if (header == null) return books;

            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (p.length < 10) {
                    System.err.println("Skipping line " + lineNumber + " - insufficient columns");
                    continue;
                }
                for (int i = 0; i < p.length; i++) p[i] = stripQuotes(p[i].trim());

                String title = p[1];
                String author = p[2];
                String mainGenre = p[3];
                String subGenre = p[4];
                String type = p[5];
                double price = parseDouble(cleanMoney(p[6]));
                double rating = parseDouble(p[7]);
                int numRated = parseInt(cleanNumber(p[8]));
                String url = p[9];

                // Find IDs
                int subGenreId = subGenreIdMap.getOrDefault(subGenre.toLowerCase().trim(), 0);
                int genreId = subGenreToGenreMap.getOrDefault(subGenreId, 0);

                if (genreId == 0) {
                    genreId = genreIdMap.getOrDefault(mainGenre.toLowerCase().trim(), 0);
                }

                // Skip books with invalid genre/subgenre references
                if (genreId == 0) {
                    System.err.println("WARNING: Line " + lineNumber + " - Genre '" + mainGenre +
                            "' not found in database. Skipping book: " + title);
                    continue;
                }

                if (subGenreId == 0) {
                    System.err.println("WARNING: Line " + lineNumber + " - SubGenre '" + subGenre +
                            "' not found in database. Skipping book: " + title);
                    continue;
                }

                // Add ALL books from CSV
                books.add(new Book(
                        0, title, author, mainGenre, subGenre, type,
                        price, rating, numRated, url, genreId, subGenreId
                ));
            }
        }

        System.out.println("Loaded " + books.size() + " books from CSV");
        return books;
    }

    public static List<Genre> loadGenres(File csvFile) throws IOException {
        List<Genre> genres = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine();
            if (header == null) return genres;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (p.length < 3) continue;

                for (int i = 0; i < p.length; i++) {
                    p[i] = stripQuotes(p[i].trim());
                }

                if (p.length > 3) {
                    StringBuilder mergedTitle = new StringBuilder();
                    for (int i = 0; i < p.length - 2; i++) {
                        mergedTitle.append(p[i]);
                        if (i < p.length - 3) mergedTitle.append(",");
                    }
                    p = new String[]{mergedTitle.toString(), p[p.length - 2], p[p.length - 1]};
                }

                String name = p[0];
                int numSubs = parseInt(cleanNumber(p[1]));
                String url = cleanUrl(p[2]);

                genres.add(new Genre(0, name, numSubs, url));
            }
        }

        System.out.println("Loaded " + genres.size() + " genres from CSV");
        return genres;
    }

    public static List<SubGenre> loadSubGenres(File csvFile, List<Genre> genres) throws IOException {
        List<SubGenre> subs = new ArrayList<>();

        Map<String, Integer> genreIdMap = new HashMap<>();
        for (Genre g : genres) {
            genreIdMap.put(g.getName().toLowerCase().trim(), g.getId());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine();
            if (header == null) return subs;

            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (p.length < 4) continue;

                for (int i = 0; i < p.length; i++) {
                    p[i] = stripQuotes(p[i].trim());
                }

                if (p.length > 4) {
                    StringBuilder mergedGenre = new StringBuilder();
                    for (int i = 1; i < p.length - 2; i++) {
                        mergedGenre.append(p[i]);
                        if (i < p.length - 3) mergedGenre.append(",");
                    }
                    p = new String[]{p[0], mergedGenre.toString(), p[p.length - 2], p[p.length - 1]};
                }

                String name = p[0];
                String mainGenre = p[1];
                int numBooks = parseInt(cleanNumber(p[2]));
                String url = cleanUrl(p[3]);

                int genreId = genreIdMap.getOrDefault(mainGenre.toLowerCase().trim(), 0);

                if (genreId == 0) {
                    System.err.println("WARNING: Line " + lineNumber + " - Genre '" + mainGenre +
                            "' not found for subgenre: " + name);
                    continue;
                }

                subs.add(new SubGenre(0, name, mainGenre, numBooks, url, genreId));
            }
        }

        System.out.println("Loaded " + subs.size() + " sub-genres from CSV");
        return subs;
    }

    // Full reset
    public static void reloadGenres(GenreDAO genreDAO) {
        try (var conn = DatabaseConnection.getConnection();
             var st = conn.createStatement()) {

            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            st.executeUpdate("TRUNCATE TABLE genres");
            st.execute("SET FOREIGN_KEY_CHECKS = 1");

            List<Genre> genreList = loadGenres(new File("Genre_df.csv"));
            genreDAO.insertAll(genreList);

        } catch (Exception e) {
            System.err.println("Failed to reload Genres: " + e.getMessage());
        }
    }

    public static void reloadSubGenres(SubGenreDAO subGenreDAO, GenreDAO genreDAO) {
        try (var conn = DatabaseConnection.getConnection();
             var st = conn.createStatement()) {

            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            st.executeUpdate("TRUNCATE TABLE subgenres");
            st.execute("SET FOREIGN_KEY_CHECKS = 1");

            List<Genre> existingGenres = genreDAO.getAll();
            List<SubGenre> subGenreList = loadSubGenres(new File("Sub_Genre_df.csv"), existingGenres);
            subGenreDAO.insertAll(subGenreList);

        } catch (Exception e) {
            System.err.println("Failed to reload SubGenres: " + e.getMessage());
        }
    }

    // HELPER
    private static String stripQuotes(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() > 1) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static String cleanMoney(String s) {
        if (s == null) return "0";
        return s.replaceAll("[^0-9.]", "").trim();
    }

    private static String cleanNumber(String s) {
        if (s == null) return "0";
        return s.replaceAll("[^0-9.]", "").trim();
    }

    private static String cleanUrl(String s) {
        if (s == null) return "";
        return s.replaceAll("^[0-9.]+,", "").trim();
    }

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static int parseInt(String s) {
        try {
            return (int) Math.round(Double.parseDouble(s.trim()));
        } catch (Exception e) {
            return 0;
        }
    }
}