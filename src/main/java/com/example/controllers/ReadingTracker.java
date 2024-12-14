package com.example.controllers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReadingTracker {
    // Page reading state tracking
    private Map<Integer, PageReadingState> pageStates;
    private int totalPages;
    private int currentPage;

    // Keyword extraction configuration
    private Set<String> stopWords;
    private int minWordLength = 4;
    private int topKeywordCount = 10;

    // Configuration constants
    private static final long MINIMUM_READ_TIME_SECONDS = 30; // 30 seconds minimum to consider page "read"
    private static final double SCROLL_COMPLETION_THRESHOLD = 0.5; // 50% of page scrolled
    
    /**
     * Constructor initializes reading tracker for a document
     * 
     * @param totalPages Total number of pages in the document
     */
    public ReadingTracker(int totalPages) {
        this.totalPages = totalPages;
        this.pageStates = new HashMap<>();
        this.currentPage = 1;

        Set<String> stopWords = new HashSet<>(Arrays.asList(
        // English Stop Words
        "the", "a", "an", "and", "or", "but", "in", "on", "at", 
        "to", "for", "of", "with", "by", "from", "up", "about", 
        "into", "over", "after", "is", "are", "was", "were", "this", "that",
        
        // French Stop Words
        "le", "la", "les", "un", "une", "des", "de", "du", "aux", 
        "et", "ou", "mais", "car", "ni", "donc", "or", "dans", "sur", 
        "à", "pour", "par", "avec", "sans", "sous", "contre", "entre", 
        "pendant", "depuis", "vers", "chez", "envers", "est", "sont", 
        "était", "étaient", "ce", "cette", "ces", "mon", "ton", "son", 
        "notre", "votre", "leur", "qui", "que", "quoi", "dont", "où", 
        "quand", "comment", "pourquoi"
        ));

        // Initialize tracking for all pages
        for (int i = 1; i <= totalPages; i++) {
            pageStates.put(i, new PageReadingState());
        }
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    public int getCurrentPage() {
        return currentPage;
    }
    
    public Map<Integer, PageReadingState> getPageStates() {
        return pageStates;
    }
    

    /**
     * Configurable method to customize keyword extraction
     * 
     * @param customStopWords Custom set of stop words to filter out
     * @param minWordLength Minimum word length to consider as a keyword
     * @param topKeywordCount Maximum number of top keywords to extract
     */
    public void setKeywordExtractionConfig(
        Set<String> customStopWords, 
        int minWordLength, 
        int topKeywordCount
    ) {
        if (customStopWords != null) {
            this.stopWords = new HashSet<>(customStopWords);
        }
        this.minWordLength = minWordLength > 0 ? minWordLength : 4;
        this.topKeywordCount = topKeywordCount > 0 ? topKeywordCount : 10;
    }

    /**
     * Inner class to track individual page reading state
     */
    public static class PageReadingState {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private double scrollPercentage;
        private boolean isCompleted;
        private String pageText; // Store page text for keyword extraction

        public void startReading() {
            this.startTime = LocalDateTime.now();
        }

        public void updateScrollProgress(double scrollPercentage) {
            this.scrollPercentage = scrollPercentage;
        }

        public void markAsCompleted() {
            this.endTime = LocalDateTime.now();
            this.isCompleted = true;
        }

        public void setPageText(String text) {
            this.pageText = text;
        }
        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }
        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }
        public void setCompleted(boolean isCompleted) {
            this.isCompleted = isCompleted;
        }
        public void setScrollPercentage(double scrollPercentage) {
            this.scrollPercentage = scrollPercentage;
        }
        public LocalDateTime getStartTime() {
            return startTime;
        }
        public LocalDateTime getEndTime() {
            return endTime;
        }
        public String getPageText() {
            return pageText;
        }
        public double getScrollPercentage() {
            return scrollPercentage;
        }

        public boolean isPageFullyRead() {
            System.out.println("Page Completion Check - Start Time: " + startTime + 
                   ", End Time: " + endTime + 
                   ", Scroll Percentage: " + scrollPercentage + 
                   ", Is Completed: " + isCompleted);

    
                // Ensure both start and end times exist
                if (startTime == null || endTime == null) {
                    return false;
                }
                
                // Lower the scroll threshold and add time-based check
                return scrollPercentage >= SCROLL_COMPLETION_THRESHOLD && 
                        (Duration.between(startTime, endTime).getSeconds() >= MINIMUM_READ_TIME_SECONDS);
        }
    }

    /**
     * Track page reading progress
     * 
     * @param page Current page being read
     * @param scrollPercentage Percentage of page scrolled
     */
    public void trackPageReading(int page, double scrollPercentage) {
        if (page < 1 || page > totalPages) return;
        
        PageReadingState state = pageStates.get(page);
        
        // Start tracking if not already started
        if (state.startTime == null) {
            state.startTime = LocalDateTime.now();
        }
        
        // Update scroll progress
        state.updateScrollProgress(scrollPercentage);
        
        // Check and mark page as completed if criteria met
        if (scrollPercentage >= SCROLL_COMPLETION_THRESHOLD) {
            state.endTime = LocalDateTime.now();
            state.markAsCompleted();
        }
        
        System.out.println("Tracking Page: " + page + 
                   ", Scroll Percentage: " + scrollPercentage + 
                   ", Start Time: " + state.getStartTime() + 
                   ", End Time: " + state.getEndTime());


        // Update current page
        this.currentPage = page;
    }

    /**
     * Set page text for keyword extraction
     * 
     * @param page Page number
     * @param text Text content of the page
     */
    public void setPageText(int page, String text) {
        if (page < 1 || page > totalPages) return;
        
        PageReadingState state = pageStates.get(page);
        state.setPageText(text);
    }

    /**
     * Get completed pages
     * 
     * @return Set of page numbers that are fully read
     */
    public Set<Integer> getCompletedPages() {
        Set<Integer> completedPages = new HashSet<>();
        for (Map.Entry<Integer, PageReadingState> entry : pageStates.entrySet()) {
            if (entry.getValue().isPageFullyRead()) {
                completedPages.add(entry.getKey());
            }
        }
        System.out.println("completed pages line206 readingtracker getCompletedPages() : "+ completedPages);
        return completedPages;
    }

    /**
     * Extract keywords from completed pages
     * 
     * @return Map of page numbers to their extracted keywords
     */
    public Map<Integer, Set<String>> extractKeywordsFromCompletedPages(int currentPage) {
        Map<Integer, Set<String>> pageKeywords = new HashMap<>();
        
        try {
            // Iterate through pages up to the current page
            for (int pageNumber = 1; pageNumber <= currentPage; pageNumber++) {
                PageReadingState state = pageStates.get(pageNumber);
                String pageText = state.pageText;
                
                if (pageText != null && !pageText.trim().isEmpty()) {
                    // Process and extract keywords
                    Set<String> keywords = processPageText(pageText);
                    
                    // Store keywords for the page
                    pageKeywords.put(pageNumber, keywords);
                }
            }
        } catch (Exception e) {
            // Log or handle any extraction errors
            System.err.println("Error extracting keywords: " + e.getMessage());
        }
        
        return pageKeywords;
    }

    /**
     * Process page text to extract meaningful keywords
     * 
     * @param text Page text to process
     * @return Set of top keywords
     */
    private Set<String> processPageText(String text) {
        // Support for removing accents and normalizing text
        text = normalizeText(text);

        // Remove punctuation and convert to lowercase
        text = text.replaceAll("[^a-zA-Z\\s]", "").toLowerCase();
        
        // Split into words
        String[] words = text.split("\\s+");
        
        // Process word frequencies
        Map<String, Integer> wordFrequency = new HashMap<>();
        for (String word : words) {
            // Filter out stop words and very short words
            if (word.length() >= minWordLength && !stopWords.contains(word)) {
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
        
        // Select top N most frequent words as keywords
        return wordFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(topKeywordCount)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    private String normalizeText(String text) {
        // Method to remove accents and normalize text
        // we might want to use a library like Apache Commons Lang for more robust normalization
        return text.replaceAll("[àáâãäå]", "a")
                   .replaceAll("[èéêë]", "e")
                   .replaceAll("[ìíîï]", "i")
                   .replaceAll("[òóôõö]", "o")
                   .replaceAll("[ùúûü]", "u")
                   .replaceAll("[ýÿ]", "y")
                   .replaceAll("[ñ]", "n");
    }
}