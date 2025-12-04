package com.alertbot.scraperservice.model;

import lombok.Data;

@Data
public class ScrapedProduct {
    private String name;
    private String URL;
    private String level;
    private double price;
    private int duration;

    public ScrapedProduct(String name, String URL, String level, double price, int duration) {
        this.name = name;
        this.URL = URL;
        this.level = level;
        this.price = price;
        this.duration = duration;
    }
}
