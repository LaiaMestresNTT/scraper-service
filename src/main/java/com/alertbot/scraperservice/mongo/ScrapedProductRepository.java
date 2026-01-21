package com.alertbot.scraperservice.mongo;

import com.alertbot.scraperservice.model.ScrapedProduct;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapedProductRepository extends MongoRepository<ScrapedProduct, String> {
    List<ScrapedProduct> findByRequestId(String requestId);
}
