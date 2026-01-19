package com.alertbot.scraperservice.kafka;

import com.alertbot.scraperservice.model.ScrapedProduct;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ScrapedProductProducer {

    private final KafkaTemplate<String, com.alertbot.avro.ScrapedProduct> kafkaTemplate;

    public ScrapedProductProducer(KafkaTemplate<String, com.alertbot.avro.ScrapedProduct> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Manda el producto extraido por el Scraper al topico de "productos_raw"
    public void sendMessage(ScrapedProduct scrapedProduct) {

        com.alertbot.avro.ScrapedProduct avroProduct = com.alertbot.avro.ScrapedProduct.newBuilder()
                .setProductId(scrapedProduct.getProductId())
                .setRequestId(scrapedProduct.getRequestId())
                .setUserId(scrapedProduct.getUserId())
                .setName(scrapedProduct.getName())
                .setBrand(scrapedProduct.getBrand())
                .setPrice(scrapedProduct.getPrice())
                .setRating(scrapedProduct.getRating())
                .build();

        try {
            String TOPIC = "productos_raw";
            kafkaTemplate.send(TOPIC, avroProduct);
            System.out.println("🚀 Mensaje enviado a Kafka Topic [" + TOPIC + "]: " + avroProduct);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar mensaje a Kafka: " + e.getMessage());
        }
    }

}
