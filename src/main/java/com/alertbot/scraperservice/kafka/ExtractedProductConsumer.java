package com.alertbot.scraperservice.kafka;

import com.alertbot.scraperservice.service.Scraper;
import com.alertbot.avro.ExtractedProduct;
import com.alertbot.scraperservice.model.AlertProduct;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ExtractedProductConsumer {

    private final String TOPIC = "nlp_results";
    private final String GROUP_ID = "nlp_results-group";

    public ExtractedProductConsumer(Scraper scraper) {
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeExtractedProductIn(ExtractedProduct extractedProduct) {

        //CONSTRUIR OBJETO
        AlertProduct product = buildAlertProduct(extractedProduct);

        System.out.println("✅ Mensaje Avro recibido al topic " + TOPIC + ": producto:" + product.getName() + " marca: "+ product.getBrand()+ " precio: " + product.getPrice()+ " valoracion: "+ product.getRating());

        //LLAMAR AL SCRAPER
        //scraper.scrapeUdemy(product);

    }

    private AlertProduct buildAlertProduct (ExtractedProduct extractedProduct) {
        String productChatId = extractedProduct.getId().toString();
        String requestedProduct = extractedProduct.getName().toString();
        String brand = extractedProduct.getBrand().toString();
        double price = Double.parseDouble(extractedProduct.getPrice().toString());
        double rating = Double.parseDouble(extractedProduct.getRating().toString());

        String URL_search = "https://www.amazon.es/s?k=" + requestedProduct.replace(" ", "+");

        return new AlertProduct(productChatId, requestedProduct, brand, price, rating, URL_search);
    }

}
