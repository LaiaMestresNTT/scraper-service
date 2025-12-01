package com.alertbot.scraperservice.kafka;

import com.alertbot.avro.ExtractedProduct;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ExtractedProductConsumer {

    private final String TOPIC = "nlp_results";
    private final String GROUP_ID = "nlp_results-group";

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeExtractedProductIn(ExtractedProduct ExtractedProduct) {
        String productChatId = ExtractedProduct.getId().toString();
        String requestedProduct = ExtractedProduct.getProduct().toString();
        String level = ExtractedProduct.getLevel().toString();
        String price_max = ExtractedProduct.getPriceMax().toString();
        String duration_max = ExtractedProduct.getDurationMax().toString();

        System.out.println("✅ Mensaje Avro recibido al topic " + TOPIC + ": producto:" + requestedProduct + " nivel: "+level+ " precio max: " +price_max+ " duración max: "+ duration_max);

        //LLAMAR AL SCRAPER....
    }
}
