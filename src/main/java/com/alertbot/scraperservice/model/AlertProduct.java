package com.alertbot.scraperservice.model;

import lombok.Data;

@Data
public class AlertProduct {

    private String id;
    private String name;
    private String level;
    private double price_max;
    private int duration_max;
    private String lang;
    private String URL_search;

    public AlertProduct(){}

    public AlertProduct(String id, String name, String level, double price_max, int duration_max, String lang, String URL_search) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.price_max = price_max;
        this.duration_max = duration_max;
        this.lang = lang;
        this.URL_search = URL_search;
    }


}
