package com.example.ece318_librarymanagementsys.util;

import javafx.scene.control.TextField;

// Centralized static validators for form input fields.
public final class InputValidator {

    public static String validateNotEmpty(TextField field, String fieldName) {
        String v = field.getText();
        if (v == null || v.trim().isEmpty())
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        return v.trim();
    }

    public static int validatePositiveInt(TextField field, String fieldName) {
        String v = validateNotEmpty(field, fieldName);
        try {
            int n = Integer.parseInt(v);
            if (n < 0)
                throw new IllegalArgumentException(fieldName + " must be positive");
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be an integer");
        }
    }

    public static double validatePositiveDouble(TextField field, String fieldName) {
        String v = validateNotEmpty(field, fieldName);
        try {
            double d = Double.parseDouble(v);
            if (d < 0)
                throw new IllegalArgumentException(fieldName + " must be positive");
            return d;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a number");
        }
    }

    public static double validateRating(TextField field) {
        String v = validateNotEmpty(field, "Rating");
        try {
            double r = Double.parseDouble(v);
            if (r < 0 || r > 5)
                throw new IllegalArgumentException("Rating must be between 0 and 5");
            return r;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Rating must be numeric");
        }
    }

    public static String validateUrl(TextField field, String fieldName) {
        String v = validateNotEmpty(field, fieldName);
        if (!v.startsWith("http://") && !v.startsWith("https://"))
            throw new IllegalArgumentException(fieldName + " must start with http:// or https://");
        return v;
    }
}
