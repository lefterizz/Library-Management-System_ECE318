package com.example.ece318_librarymanagementsys.database;

import com.example.ece318_librarymanagementsys.model.Book;

import java.sql.*;

public class BookDAO extends BaseDAO<Book> implements DAO<Book> {
    private int skippedCount = 0; // counter for skipped entries

    @Override
    protected String getTableName() {
        return "books";
    }

    @Override
    protected Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("main_genre"),
                rs.getString("sub_genre"),
                rs.getString("type"),
                rs.getDouble("price"),
                rs.getDouble("rating"),
                rs.getInt("num_rated"),
                rs.getString("url"),
                rs.getInt("genre_id"),
                rs.getInt("subgenre_id")
        );
    }

    // deleteById(), findById(), getAll() from BaseDAO -> inheritance

    private boolean bookExists(Book book) {
        String sql = """
        SELECT COUNT(*) FROM books 
        WHERE LOWER(title) = LOWER(?) 
        AND LOWER(author) = LOWER(?)
        AND LOWER(main_genre) = LOWER(?)
        AND LOWER(sub_genre) = LOWER(?)
        AND LOWER(type) = LOWER(?)
        AND LOWER(url) = LOWER(?)
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getMainGenre());
            stmt.setString(4, book.getSubGenre());
            stmt.setString(5, book.getType());
            stmt.setString(6, book.getUrl());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void insert(Book book) {
        if (bookExists(book)) {
            skippedCount++; // increment counter
            return;
        }

        String sql = """
        INSERT INTO books 
        (title, author, main_genre, sub_genre, type, price, rating, num_rated, url, genre_id, subgenre_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getMainGenre());
            stmt.setString(4, book.getSubGenre());
            stmt.setString(5, book.getType());
            stmt.setDouble(6, book.getPrice());
            stmt.setDouble(7, book.getRating());
            stmt.setInt(8, book.getNumRated());
            stmt.setString(9, book.getUrl());
            stmt.setInt(10, book.getGenreId());
            stmt.setInt(11, book.getSubGenreId());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    book.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Book book) {
        String sql = """
            UPDATE books
            SET
                title=?, author=?, main_genre=?, sub_genre=?, type=?,
                price=?, rating=?, num_rated=?, url=?, genre_id=?, subgenre_id=?
            WHERE id=?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getMainGenre());
            stmt.setString(4, book.getSubGenre());
            stmt.setString(5, book.getType());
            stmt.setDouble(6, book.getPrice());
            stmt.setDouble(7, book.getRating());
            stmt.setInt(8, book.getNumRated());
            stmt.setString(9, book.getUrl());
            stmt.setInt(10, book.getGenreId());
            stmt.setInt(11, book.getSubGenreId());
            stmt.setInt(12, book.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearBooks() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Delete all books
            stmt.executeUpdate("DELETE FROM books");

            // Reset auto-increment counter to 1
            stmt.executeUpdate("ALTER TABLE books AUTO_INCREMENT = 1");

            System.out.println("Cleared all books and reset ID counter to 1");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void printSkippedCount() {
        System.out.println("Total skipped entries: " + skippedCount);
        skippedCount = 0;
    }
}
