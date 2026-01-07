package com.example.ece318_librarymanagementsys.controller;

import javafx.animation.TranslateTransition;
import javafx.application.HostServices;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

// reusable details panel controller w animation
public abstract class DetailsPanelController<T> {

    // core components
    protected final HBox contentArea;
    protected final VBox detailsPanel;
    protected final TableView<T> table;
    protected final Button closeButton;

    // animation transitions
    private final TranslateTransition slideIn;
    private final TranslateTransition slideOut;

    protected HostServices hostServices;

    // animation config
    private static final int ANIMATION_DURATION_MS = 200;
    private static final double SLIDE_DISTANCE = 360.0;

    protected DetailsPanelController(HBox contentArea,
                                     VBox detailsPanel,
                                     Button closeButton,
                                     TableView<T> table) {
        this.contentArea = contentArea;
        this.detailsPanel = detailsPanel;
        this.closeButton = closeButton;
        this.table = table;

        // Init animations
        this.slideIn = createTransition(SLIDE_DISTANCE, 0);
        this.slideOut = createTransition(0, SLIDE_DISTANCE);

        setupEventHandlers();

        // Initially hidden
        contentArea.getChildren().remove(detailsPanel);
    }

    public final void showEntity(T entity) {
        if (entity == null) {
            hide();
            return;
        }

        populateDetails(entity);
        attachPanelIfNeeded();
        playSlideAnimation(true);
    }

    protected abstract void populateDetails(T entity);

    public final void hide() {
        if (!isPanelVisible()) {
            clearTableSelection();
            return;
        }
        playSlideAnimation(false);
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    //Helper to open a URL in the default browser
    protected void openUrl(String url) {
        if (hostServices != null && isValidUrl(url)) {
            hostServices.showDocument(url);
        }
    }

    //Configures a hyperlink to open a URL
    protected void setupHyperlink(Hyperlink link, String displayText, String url) {
        link.setText(displayText);
        link.setOnAction(event -> openUrl(url));
    }

    // Private helper methods
    private void setupEventHandlers() {
        closeButton.setOnAction(event -> hide());
    }

    private void attachPanelIfNeeded() {
        if (!isPanelVisible()) {
            contentArea.getChildren().add(detailsPanel);
        }
    }

    private boolean isPanelVisible() {
        return contentArea.getChildren().contains(detailsPanel);
    }

    private void clearTableSelection() {
        if (table != null) {
            table.getSelectionModel().clearSelection();
        }
    }

    private TranslateTransition createTransition(double fromX, double toX) {
        TranslateTransition transition = new TranslateTransition(
                Duration.millis(ANIMATION_DURATION_MS),
                detailsPanel
        );
        transition.setFromX(fromX);
        transition.setToX(toX);
        return transition;
    }

    private void playSlideAnimation(boolean isShowing) {
        TranslateTransition transition = isShowing ? slideIn : slideOut;

        if (!isShowing) {
            transition.setOnFinished(event -> {
                contentArea.getChildren().remove(detailsPanel);
                clearTableSelection();
            });
        } else {
            transition.setOnFinished(null);
        }

        transition.playFromStart();
    }

    // Helper methods
    private boolean isValidUrl(String url) {
        return url != null && !url.isBlank() &&
                (url.startsWith("http://") || url.startsWith("https://"));
    }
    // Safe text setter that handles null values
    protected String safeText(String value) {
        return value != null ? value : "";
    }

    //Formats a numeric value safely
    protected String formatNumber(Number value, String format) {
        return value != null ? String.format(format, value) : "0";
    }
}