package com.alertbot.scraperservice.model;

import lombok.Data;

@Data
public class AlertProduct {

    private String request_id;
    private String user_id;
    private String name;
    private String brand;
    private double price;
    private double rating;
    private String URL_search;
    private ProductStatus status;

    public AlertProduct(){}

    public AlertProduct(String request_id, String user_id, String name, String brand, double price, double rating, String URL_search, ProductStatus status) {
        this.request_id = request_id;
        this.user_id = user_id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.rating = rating;
        this.URL_search = URL_search;
        this.status = status;
    }


}
