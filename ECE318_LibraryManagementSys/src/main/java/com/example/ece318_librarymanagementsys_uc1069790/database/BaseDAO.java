package com.example.ece318_librarymanagementsys.database;

import com.example.ece318_librarymanagementsys.model.BaseEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T extends BaseEntity> implements DAO<T> {

    protected abstract String getTableName();
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    @Override
    public T findById(int id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {

            s.setInt(1, id);

            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<T> getAll() {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY id";
        List<T> list = new ArrayList<>();

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {

            s.setInt(1, id);
            s.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
