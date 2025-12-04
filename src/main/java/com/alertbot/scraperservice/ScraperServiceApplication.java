package com.alertbot.scraperservice;

import com.alertbot.scraperservice.service.Scraper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScraperServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScraperServiceApplication.class, args);

        Scraper scraper = new Scraper();
        scraper.scrapeUdemy();
    }
}
