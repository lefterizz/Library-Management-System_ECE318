package com.example.ece318_librarymanagementsys.database;

import com.example.ece318_librarymanagementsys.model.SubGenre;

import java.sql.*;
import java.util.*;

public class SubGenreDAO extends BaseDAO<SubGenre> implements DAO<SubGenre> {

    @Override
    protected String getTableName() {
        return "subgenres";
    }

    @Override
    protected SubGenre mapRow(ResultSet rs) throws SQLException {
        return new SubGenre(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("main_genre"),
                rs.getInt("num_books"),
                rs.getString("url"),
                rs.getInt("genre_id")
        );
    }

    @Override
    public void insert(SubGenre sg) {
        insertAll(List.of(sg));
    }

    @Override
    public void update(SubGenre sg) {
        updateSubGenre(sg);
    }

    public void insertAll(List<SubGenre> subGenres) {
        String sql = """
            INSERT INTO subgenres (name, main_genre, num_books, url, genre_id)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (SubGenre sg : subGenres) {
                ps.setString(1, sg.getName());
                ps.setString(2, sg.getMainGenre());
                ps.setInt(3, sg.getNumBooks());
                ps.setString(4, sg.getUrl());
                ps.setInt(5, sg.getGenreId());
                ps.addBatch();
            }

            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSubGenre(SubGenre subGenre) {
        String sql = """
            UPDATE subgenres SET
                name = ?,
                main_genre = ?,
                num_books = ?,
                url = ?,
                genre_id = ?
            WHERE id = ?
        """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {

            s.setString(1, subGenre.getName());
            s.setString(2, subGenre.getMainGenre());
            s.setInt(3, subGenre.getNumBooks());
            s.setString(4, subGenre.getUrl());
            s.setInt(5, subGenre.getGenreId());
            s.setInt(6, subGenre.getId());
            s.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public Map<String, List<String>> getAllSubGenreNamesDetailed() {
        Map<String, List<String>> map = new HashMap<>();
        String sql = """
            SELECT g.name AS genre_name, s.name AS sub_name
            FROM subgenres s
            JOIN genres g ON s.genre_id = g.id
            ORDER BY g.name, s.name
        """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {

            while (rs.next()) {
                map.computeIfAbsent(rs.getString("genre_name"), k -> new ArrayList<>())
                        .add(rs.getString("sub_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static final class SubGenreStat {
        public final int id;
        public final String name;
        public final String mainGenre;
        public final int totalBooks;
        public final double avgRating;
        public final double avgPrice;

        public SubGenreStat(int id, String name, String mainGenre, int totalBooks, double avgRating, double avgPrice) {
            this.id = id;
            this.name = name;
            this.mainGenre = mainGenre;
            this.totalBooks = totalBooks;
            this.avgRating = avgRating;
            this.avgPrice = avgPrice;
        }
    }

    public List<SubGenreStat> getSubGenreStats() {
        String sql = """
    SELECT
        s.id,
        s.name,
        s.main_genre,
        s.num_books AS total_books,

        -- FIX: use subgenre_id instead of sub_genre text
        COALESCE((SELECT ROUND(AVG(b.rating), 2)
                  FROM books b
                  WHERE b.subgenre_id = s.id), 0.00) AS avg_rating,

        COALESCE((SELECT ROUND(AVG(b.price), 2)
                  FROM books b
                  WHERE b.subgenre_id = s.id), 0.00) AS avg_price

    FROM subgenres s
    ORDER BY s.id
""";


        List<SubGenreStat> out = new ArrayList<>();

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(new SubGenreStat(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("main_genre"),
                        rs.getInt("total_books"),
                        rs.getDouble("avg_rating"),
                        rs.getDouble("avg_price")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return out;
    }
    public void incrementBookCount(int subGenreId) {
        if (subGenreId <= 0) return;

        String sql = "UPDATE subgenres SET num_books = num_books + 1 WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, subGenreId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void decrementBookCount(int subGenreId) {
        if (subGenreId <= 0) return;

        String sql = "UPDATE subgenres SET num_books = GREATEST(num_books - 1, 0) WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, subGenreId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}