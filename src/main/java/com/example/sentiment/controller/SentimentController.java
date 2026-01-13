package com.example.sentiment.controller;

import com.example.sentiment.model.SentimentResponse;
import com.example.sentiment.service.SentimentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SentimentController {

    private final SentimentService sentimentService;

    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    @GetMapping("/sentiment")
    public SentimentResponse analyzeSentiment(@RequestParam String text) {
        String sentiment = sentimentService.analyze(text);
        return new SentimentResponse(sentiment);
    }
}
