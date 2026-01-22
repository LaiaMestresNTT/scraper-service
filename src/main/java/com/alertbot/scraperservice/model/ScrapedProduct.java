package com.alertbot.scraperservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "scraped_products")
public class ScrapedProduct {

    @Id
    @Field("product_id")
    private String productId;
    @Field("request_id")
    private String requestId;
    @Field("user_id")
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
