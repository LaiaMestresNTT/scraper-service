package com.alertbot.scraperservice.model;

import lombok.Data;

@Data
public class AlertProduct {

    private String requestId;
    private String userId;
    private String name;
    private String brand;
    private double price;
    private double rating;
    private String URL_search;
    private ProductStatus status;

    public AlertProduct(){}

    public AlertProduct(String requestId, String userId, String name, String brand, double price, double rating, String URL_search, ProductStatus status) {
        this.requestId = requestId;
        this.userId = userId;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.rating = rating;
        this.URL_search = URL_search;
        this.status = status;
    }


}
