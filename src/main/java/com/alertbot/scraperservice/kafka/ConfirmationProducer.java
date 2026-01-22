package com.alertbot.scraperservice.kafka;

import com.alertbot.scraperservice.model.AlertProduct;
import com.alertbot.scraperservice.model.ScrapedProduct;
import com.alertbot.avro.ConfirmScraped;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationProducer {

    private final KafkaTemplate<String, ConfirmScraped> kafkaTemplate;

    public ConfirmationProducer(KafkaTemplate<String, ConfirmScraped> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Manda el producto extraido por el Scraper al topico de "scraping_finished"
    public void sendMessage(AlertProduct alertProduct, int validProd_cont) {

        ConfirmScraped confirmationMessage = ConfirmScraped.newBuilder()
                .setRequestId(alertProduct.getRequestId())
                .setUserId(alertProduct.getUserId())
                .setProductCount(validProd_cont)
                .setStatus(alertProduct.getStatus().toString())
                .build();

        try {
            String TOPIC = "scraping_finished";
            kafkaTemplate.send(TOPIC, confirmationMessage);
            System.out.println("✅ Mensaje de confirmación enviado a Kafka Topic [" + TOPIC + "]: " + confirmationMessage);
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar mensaje a Kafka: " + e.getMessage());
        }
    }

}
