package com.alertbot.scraperservice.scorer;

import com.alertbot.scraperservice.model.ScrapedProduct;
import org.springframework.stereotype.Service;

@Service
public class Scoring {

    public double calculateScore(double rating, int ratingCount) {
        double score = 0.0;

        // Rating Ajustado
        // Usamos una constante de suavizado (m = 100 valoraciones mínimas para confianza)
        double adjustedRating = calculateAdjustedRating(rating, ratingCount);
        double finalScore = adjustedRating * 2;

        return Math.round(finalScore * 100.0) / 100.0;
    }

    private double calculateAdjustedRating(double stars, int reviews) {
        int minReviewsForTrust = 50;
        // Media Bayesiana simple: (v*R + m*C) / (v+m)
        // v = reviews, R = stars, m = reviews mínimas, C = media global (asumimos 3.5)
        double globalAverage = 3.5;
        return (reviews * stars + minReviewsForTrust * globalAverage) / (reviews + minReviewsForTrust);
    }
}
