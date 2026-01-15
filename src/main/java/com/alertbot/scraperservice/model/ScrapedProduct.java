package com.alertbot.scraperservice.model;

import lombok.Data;

@Data
public class ScrapedProduct {

    private String productId;
    private String requestId;
    private String userId;
    private String name;
    private String URL;
    private String brand;
    private double price;
    private double rating;

    public ScrapedProduct() {
    }

    public ScrapedProduct(String productId, String requestId, String userId, String name, String URL, String brand, double price, double rating) {
        this.productId = productId;
        this.requestId = requestId;
        this.userId = userId;
        this.name = name;
        this.URL = URL;
        this.brand = brand;
        this.price = price;
        this.rating = rating;
    }

}
