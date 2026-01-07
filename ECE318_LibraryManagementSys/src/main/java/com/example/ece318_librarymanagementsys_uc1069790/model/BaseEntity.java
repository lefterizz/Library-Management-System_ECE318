package com.example.ece318_librarymanagementsys.model;


public abstract class BaseEntity {

    protected int id;  // accessible to subclasses only

    protected BaseEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isNew() {
        return id == 0;
    }

    public void setId(int id) {
        this.id = id;
    }
}
