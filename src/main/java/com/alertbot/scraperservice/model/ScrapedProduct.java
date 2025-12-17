package com.alertbot.scraperservice.model;

import lombok.Data;

@Data
public class ScrapedProduct {
    private String id_busqueda;
    private String name;
    private String URL;
    private String brand;
    private double price;
    private double rating;

    public ScrapedProduct () {}

    public ScrapedProduct(String id_busqueda, String name, String URL, String brand, double price, double rating) {
        this.id_busqueda = id_busqueda;
        this.name = name;
        this.URL = URL;
        this.brand = brand;
        this.price = price;
        this.rating = rating;
    }

    public String getId_busqueda() {
        return id_busqueda;
    }

    public void setId_busqueda(String id_busqueda) {
        this.id_busqueda = id_busqueda;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
