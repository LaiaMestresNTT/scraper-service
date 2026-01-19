package com.alertbot.scraperservice.kafka;

import com.alertbot.scraperservice.model.ProductStatus;
import com.alertbot.scraperservice.service.ProductStatusManager;
import com.alertbot.scraperservice.service.Scraper;
import com.alertbot.avro.ExtractedProduct;
import com.alertbot.scraperservice.model.AlertProduct;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ExtractedProductConsumer {

    private final String TOPIC = "nlp_results";
    private final String GROUP_ID = "nlp_results-group";
    private final Scraper scraper;
    private final ProductStatusManager statusManager;

    public ExtractedProductConsumer(Scraper scraper, ProductStatusManager statusManager) {
        this.scraper = scraper;
        this.statusManager = statusManager;
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeExtractedProductIn(ExtractedProduct extractedProduct) {

        //CONSTRUIR OBJETO
        AlertProduct product = buildAlertProduct(extractedProduct);

        if (product != null) {
            System.out.println("✅ Mensaje Avro recibido al topic " + TOPIC + ": nombre:" + product.getName() + " marca: "+ product.getBrand()+ " precio: " + product.getPrice()+ " valoración: "+ product.getRating());

            //ACTUALIZAR STATUS
            statusManager.updateToSearching(product.getRequest_id());
            //LLAMAR AL SCRAPER
            scraper.scrapeWeb(product);
        }

    }

    private AlertProduct buildAlertProduct (ExtractedProduct extractedProduct) {
        String requestId = extractedProduct.getRequestId().toString();
        String userId = extractedProduct.getUserId().toString();

        String requestedProduct = extractedProduct.getName().toString();
        if (requestedProduct.equals("no especificado")) {
            return null;
        }

        String brand = extractedProduct.getBrand().toString();
        double price = parseDoubleSafe(extractedProduct.getPrice());
        double rating = parseDoubleSafe(extractedProduct.getRating());

        String URL_search = "https://www.amazon.es/s?k=" + requestedProduct.replace(" ", "+");

        return new AlertProduct(requestId, userId, requestedProduct, brand, price, rating, URL_search, ProductStatus.SEARCHING);
    }

    private double parseDoubleSafe(Object value) {
        if (value == null) { return 0.0; }

        try {
            String stringValue = value.toString().trim().replace(",", ".");
            stringValue = stringValue.replaceAll("[^0-9.]", "");

            return Double.parseDouble(stringValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

}
