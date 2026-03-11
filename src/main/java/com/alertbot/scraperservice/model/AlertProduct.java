package com.alertbot.scraperservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertProduct {

    private String requestId;
    private String userId;
    private String name;
    private String brand;
    private double price;
    private double rating;
    private String URL_search;
    private ProductStatus status;


}
