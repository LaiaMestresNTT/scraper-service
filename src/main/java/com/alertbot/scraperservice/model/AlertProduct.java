package com.alertbot.scraperservice.model;

import lombok.Data;

@Data
public class AlertProduct {

    private String id;
    private String name;
    private String brand;
    private double price;
    private double rating;
    private String URL_search;

    public AlertProduct(){}

    public AlertProduct(String id, String name, String brand, double price, double rating, String URL_search) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.rating = rating;
        this.URL_search = URL_search;
    }


}
