package com.example.ece318_librarymanagementsys.database;

import com.example.ece318_librarymanagementsys.model.Genre;

import java.sql.*;
import java.util.*;

public class GenreDAO extends BaseDAO<Genre> implements DAO<Genre> {

    @Override
    protected String getTableName() {
        return "genres";
    }

    @Override
    protected Genre mapRow(ResultSet rs) throws SQLException {
        return new Genre(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("num_subgenres"),
                rs.getString("url")
        );
    }

    @Override
    public void insert(Genre g) {
        insertAll(List.of(g));
    }

    @Override
    public void update(Genre g) {
        updateGenre(g);
    }

    public void insertAll(List<Genre> genres) {
        String sql = """
            INSERT INTO genres (name, num_subgenres, url)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
              num_subgenres = VALUES(num_subgenres),
              url = VALUES(url)
        """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {

            for (Genre g : genres) {
                s.setString(1, g.getName());
                s.setInt(2, g.getNumSubGenres());
                s.setString(3, g.getUrl());
                s.addBatch();
            }

            s.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateGenre(Genre genre) {
        String sql = """
        UPDATE genres SET
            name = ?,
            num_subgenres = ?,
            url = ?
        WHERE id = ?
    """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {

            s.setString(1, genre.getName());
            s.setInt(2, genre.getNumSubGenres());
            s.setString(3, genre.getUrl());
            s.setInt(4, genre.getId());
            s.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllGenreNames() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM genres ORDER BY name";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {

            while (rs.next()) list.add(rs.getString("name"));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public int findIdByName(String name) {
        if (name == null || name.isBlank()) return 0;

        String sql = "SELECT id FROM genres WHERE name = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {

            s.setString(1, name);

            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static final class GenreStat {
        public final int id;
        public final String name;
        public final int totalBooks;
        public final double avgRating;
        public final double avgPrice;

        public GenreStat(int id, String name, int totalBooks, double avgRating, double avgPrice) {
            this.id = id;
            this.name = name;
            this.totalBooks = totalBooks;
            this.avgRating = avgRating;
            this.avgPrice = avgPrice;
        }
    }

    public List<GenreStat> getGenreStats() {

        String sql = """
    SELECT
        g.id,
        g.name,

        -- total books comes from subgenres table
        COALESCE((SELECT SUM(s.num_books)
                  FROM subgenres s
                  WHERE s.genre_id = g.id), 0) AS total_books,

        -- AVG rating based on genre_id, not name
        COALESCE((SELECT ROUND(AVG(b.rating), 2)
                  FROM books b
                  WHERE b.genre_id = g.id), 0.00) AS avg_rating,

        -- AVG price based on genre_id, not name
        COALESCE((SELECT ROUND(AVG(b.price), 2)
                  FROM books b
                  WHERE b.genre_id = g.id), 0.00) AS avg_price

    FROM genres g
    ORDER BY g.id
""";
        List<GenreStat> out = new ArrayList<>();

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(new GenreStat(
                        rs.getInt("id"),
                        rs.getString("name"),
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

    public void recountSubGenres(int genreId) {
        String sql = """
            UPDATE genres SET num_subgenres =
                (SELECT COUNT(*) FROM subgenres WHERE genre_id = ?)
            WHERE id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, genreId);
            ps.setInt(2, genreId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}