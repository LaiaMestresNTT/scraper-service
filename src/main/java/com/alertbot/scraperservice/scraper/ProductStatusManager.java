package com.alertbot.scraperservice.scraper;

import com.alertbot.scraperservice.model.ProductStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ProductStatusManager {

    private final MongoTemplate mongoTemplate;

    public ProductStatusManager(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void updateToSearching(String requestId) {
        updateStatus(requestId, ProductStatus.SEARCHING.name());
    }

    public void updateToFailed(String requestId) {
        updateStatus(requestId, ProductStatus.FAILED.name());
    }

    public void updateToCompleted(String requestId) {
        updateStatus(requestId, ProductStatus.COMPLETED.name());
    }

    private void updateStatus(String requestId, String status) {
        Query query = new Query(Criteria.where("requestId").is(requestId));
        Update update = new Update().set("status", status);
        mongoTemplate.updateFirst(query, update, "product_requests");
    }
}
