package com.example.sentiment.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SentimentService {

    private static final Set<String> POSITIVE_WORDS = Set.of(
            "good", "great", "excellent", "amazing", "wonderful", "fantastic",
            "love", "like", "happy", "joy", "awesome", "best", "beautiful",
            "nice", "perfect", "brilliant", "superb", "outstanding", "positive",
            "glad", "pleased", "delighted", "grateful", "thankful", "blessed",
            "hello", "hi", "welcome"
    );

    private static final Set<String> NEGATIVE_WORDS = Set.of(
            "bad", "terrible", "awful", "horrible", "hate", "dislike",
            "sad", "angry", "worst", "poor", "disappointing", "negative",
            "ugly", "boring", "annoying", "frustrating", "painful", "miserable",
            "unhappy", "upset", "worried", "anxious", "stressed", "depressed"
    );

    public String analyze(String text) {
        if (text == null || text.isBlank()) {
            return "neutral";
        }

        String lowerText = text.toLowerCase();
        String[] words = lowerText.split("[\\s\\p{Punct}]+");

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : words) {
            if (POSITIVE_WORDS.contains(word)) {
                positiveCount++;
            }
            if (NEGATIVE_WORDS.contains(word)) {
                negativeCount++;
            }
        }

        if (positiveCount > negativeCount) {
            return "positive";
        } else if (negativeCount > positiveCount) {
            return "negative";
        } else {
            return "neutral";
        }
    }
}
