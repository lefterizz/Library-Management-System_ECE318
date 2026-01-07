package com.example.ece318_librarymanagementsys.controller.core;

import com.example.ece318_librarymanagementsys.database.SubGenreDAO;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.controlsfx.control.CheckComboBox;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchController<T> {

    private final TableView<T> tableView;
    private FilteredList<T> filteredList;
    private final TextField searchField;
    private final Function<T, String> searchTextExtractor;

    // Advanced filter components
    private CheckComboBox<String> genreFilterBox;
    private CheckComboBox<String> subGenreFilterBox;
    private Function<T, String> genreExtractor;
    private Function<T, String> subGenreExtractor;

    // Performance caches
    private Map<String, List<String>> genreToSubGenresCache;
    private Set<String> selectedGenresCache = Collections.emptySet();
    private Set<String> selectedSubGenresCache = Collections.emptySet();

    // Debouncing
    private PauseTransition debounceTimer;
    private static final int DEBOUNCE_DELAY_MS = 250;

    public SearchController(TableView<T> tableView,
                            FilteredList<T> filteredList,
                            TextField searchField,
                            Function<T, String> searchTextExtractor) {
        this.tableView = tableView;
        this.filteredList = filteredList;
        this.searchField = searchField;
        this.searchTextExtractor = searchTextExtractor;

        initializeDebounceTimer();
        attachSearchListener();
    }

    public void enableAdvancedFilters(CheckComboBox<String> genreBox,
                                      CheckComboBox<String> subGenreBox,
                                      Function<T, String> genreExtractor,
                                      Function<T, String> subGenreExtractor) {
        this.genreFilterBox = genreBox;
        this.subGenreFilterBox = subGenreBox;
        this.genreExtractor = genreExtractor;
        this.subGenreExtractor = subGenreExtractor;

        loadGenreCache();
        populateFilterDropdowns();
        attachFilterListeners();
        applyAllFilters();
    }

    // Updates the filtered list reference after table refresh
    public void updateFilteredList(FilteredList<T> newFilteredList) {
        if (newFilteredList != null) {
            this.filteredList = newFilteredList;
            applyAllFilters();
        }
    }

    // Repopulates filter dropdowns after genre/subgenre changes
    public void refreshFilters() {
        if (genreFilterBox != null && subGenreFilterBox != null) {
            loadGenreCache();
            populateFilterDropdowns();
            updateSelectedCaches();
            applyAllFilters();
        }
    }

    private void initializeDebounceTimer() {
        debounceTimer = new PauseTransition(Duration.millis(DEBOUNCE_DELAY_MS));
        debounceTimer.setOnFinished(event -> applyAllFilters());
    }

    private void attachSearchListener() {
        if (searchField == null) return;

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            debounceTimer.stop();
            debounceTimer.playFromStart();
        });
    }

    private void loadGenreCache() {
        genreToSubGenresCache = new SubGenreDAO().getAllSubGenreNamesDetailed();
    }

    private void populateFilterDropdowns() {
        if (genreFilterBox == null || subGenreFilterBox == null) return;

        // Populate genres
        Set<String> sortedGenres = new TreeSet<>(genreToSubGenresCache.keySet());
        genreFilterBox.getItems().setAll(sortedGenres);

        // Populate all subgenres
        Set<String> allSubGenres = genreToSubGenresCache.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(TreeSet::new));

        subGenreFilterBox.getItems().setAll(allSubGenres);
        subGenreFilterBox.setDisable(true);
    }

    private void attachFilterListeners() {
        if (genreFilterBox == null || subGenreFilterBox == null) return;

        genreFilterBox.getCheckModel().getCheckedItems()
                .addListener((ListChangeListener<String>) change -> {
                    updateSubGenreDropdown();
                    updateSelectedCaches();
                    applyAllFilters();
                });

        subGenreFilterBox.getCheckModel().getCheckedItems()
                .addListener((ListChangeListener<String>) change -> {
                    updateSelectedCaches();
                    applyAllFilters();
                });
    }

    private void updateSubGenreDropdown() {
        if (subGenreFilterBox == null || genreFilterBox == null) return;

        List<String> checkedGenres = genreFilterBox.getCheckModel().getCheckedItems();
        subGenreFilterBox.setDisable(checkedGenres.isEmpty());

        if (checkedGenres.isEmpty()) {
            return;
        }

        // Collect subgenres for selected genres
        Set<String> relevantSubGenres = new TreeSet<>();
        for (String genre : checkedGenres) {
            List<String> subs = genreToSubGenresCache.get(genre);
            if (subs != null) {
                relevantSubGenres.addAll(subs);
            }
        }

        subGenreFilterBox.getCheckModel().clearChecks();
        subGenreFilterBox.getItems().setAll(relevantSubGenres);
    }

    private void updateSelectedCaches() {
        selectedGenresCache = genreFilterBox != null
                ? new HashSet<>(genreFilterBox.getCheckModel().getCheckedItems())
                : Collections.emptySet();

        selectedSubGenresCache = subGenreFilterBox != null
                ? new HashSet<>(subGenreFilterBox.getCheckModel().getCheckedItems())
                : Collections.emptySet();
    }

    /**
     * Applies all active filters with optimized predicate logic
     */
    private void applyAllFilters() {
        if (filteredList == null) return;

        // Pre-compute filter conditions
        final String query = extractSearchQuery();
        final boolean hasSearchQuery = !query.isEmpty();
        final boolean hasGenreFilter = !selectedGenresCache.isEmpty();
        final boolean hasSubGenreFilter = !selectedSubGenresCache.isEmpty();

        filteredList.setPredicate(item -> {
            if (item == null) return false;

            // OPTIMIZATION 1: Check search first (most selective)
            if (hasSearchQuery && !matchesSearchQuery(item, query)) {
                return false;
            }

            // OPTIMIZATION 2: Early exit if no genre filters
            if (!hasGenreFilter && !hasSubGenreFilter) {
                return true;
            }

            // Extract genre/subgenre once
            String itemGenre = extractGenre(item);
            String itemSubGenre = extractSubGenre(item);

            // OPTIMIZATION 3: Direct O(1) contains checks
            return matchesGenreFilters(itemGenre, itemSubGenre,
                    hasGenreFilter, hasSubGenreFilter);
        });
    }

    private String extractSearchQuery() {
        return searchField != null
                ? searchField.getText().trim().toLowerCase(Locale.ROOT)
                : "";
    }

    /**
     * IMPROVED: Prefix-based search (books starting with query)
     * Also supports author name matching
     */
    private boolean matchesSearchQuery(T item, String query) {
        String searchText = searchTextExtractor != null
                ? searchTextExtractor.apply(item)
                : null;

        if (searchText == null || searchText.isEmpty()) {
            return false;
        }

        String lowerSearchText = searchText.toLowerCase(Locale.ROOT);

        // Check if any word in the search text starts with the query
        String[] words = lowerSearchText.split("\\s+");
        for (String word : words) {
            if (word.startsWith(query)) {
                return true;
            }
        }

        // Also check full contains for flexibility
        return lowerSearchText.contains(query);
    }

    private String extractGenre(T item) {
        String genre = genreExtractor != null ? genreExtractor.apply(item) : "";
        return genre != null ? genre : "";
    }

    private String extractSubGenre(T item) {
        String subGenre = subGenreExtractor != null ? subGenreExtractor.apply(item) : "";
        return subGenre != null ? subGenre : "";
    }

    private boolean matchesGenreFilters(String itemGenre,
                                        String itemSubGenre,
                                        boolean hasGenreFilter,
                                        boolean hasSubGenreFilter) {
        // Only genre filter active
        if (hasGenreFilter && !hasSubGenreFilter) {
            return selectedGenresCache.contains(itemGenre);
        }

        // Both filters active
        if (hasGenreFilter) {
            if (!selectedGenresCache.contains(itemGenre)) {
                return false;
            }

            List<String> genreSubGenres = genreToSubGenresCache
                    .getOrDefault(itemGenre, Collections.emptyList());

            // Check if any selected subgenres belong to this genre
            boolean hasRelevantSubs = genreSubGenres.stream()
                    .anyMatch(selectedSubGenresCache::contains);

            if (hasRelevantSubs) {
                return selectedSubGenresCache.contains(itemSubGenre);
            }

            return true; // Genre matches, no relevant subgenres selected
        }

        // Only subgenre filter active
        return selectedSubGenresCache.contains(itemSubGenre);
    }
}