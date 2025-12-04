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

        System.out.println("✅ Mensaje Avro recibido al topic " + TOPIC + ": producto:" + product.getName() + " nivel: "+ product.getLevel()+ " precio max: " + product.getPrice_max()+ " duración max: "+ product.getDuration_max());

        //LLAMAR AL SCRAPER
        //scraper.scrapeUdemy(product);

    }

    private AlertProduct buildAlertProduct (ExtractedProduct extractedProduct) {
        String productChatId = extractedProduct.getId().toString();
        String requestedCourse = extractedProduct.getCourse().toString();
        String level = extractedProduct.getLevel().toString();
        double price_max = Double.parseDouble(extractedProduct.getPriceMax().toString());
        int duration_max = Integer.parseInt(extractedProduct.getDurationMax().toString());
        String lang = extractedProduct.getLang().toString();

        String duration = setDuration(duration_max);

        String URL_search = "https://www.udemy.com/courses/search/?q="+requestedCourse+"&duration="+duration+"&ratings=4.0&lang="+lang+"&instructional_level="+level;

        return new AlertProduct(productChatId, requestedCourse, level, price_max, duration_max, lang, URL_search);
    }

    private String setDuration(int duration_max) {

        if (duration_max >= 0 && duration_max <= 1) {
            return "extraShort";
        } else if (duration_max >= 1 && duration_max <= 3) {
            return "short";
        } else if (duration_max >= 3 && duration_max <= 6) {
            return "medium";
        } else if (duration_max >= 6 && duration_max <= 17) {
            return "long";
        } else if (duration_max >= 17) {
            return "extraLong";
        }
        return "";
    }
}
