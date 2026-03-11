package com.alertbot.scraperservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private int ratingCount;
    private double score;


}
