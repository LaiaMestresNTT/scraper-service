package com.alertbot.scraperservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "scraped_products")
public class ScrapedProduct {

    @Id
    private String product_id;
    private String request_id;
    private String user_id;
    private String name;
    private String URL;
    private String brand;
    private double price;
    private double rating;

    public ScrapedProduct() {
    }

    public ScrapedProduct(String product_id, String request_id, String user_id, String name, String URL, String brand, double price, double rating) {
        this.product_id = product_id;
        this.request_id = request_id;
        this.user_id = user_id;
        this.name = name;
        this.URL = URL;
        this.brand = brand;
        this.price = price;
        this.rating = rating;
    }

}
