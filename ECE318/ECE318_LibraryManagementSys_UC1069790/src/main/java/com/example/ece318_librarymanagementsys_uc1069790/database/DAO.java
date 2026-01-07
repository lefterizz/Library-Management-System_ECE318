package com.example.ece318_librarymanagementsys_uc1069790.database;

import com.example.ece318_librarymanagementsys_uc1069790.model.BaseEntity;
import java.util.List;

public interface DAO<T extends BaseEntity> {
    void insert(T entity);
    void update(T entity);
    void deleteById(int id);
    T findById(int id);
    List<T> getAll();
}
