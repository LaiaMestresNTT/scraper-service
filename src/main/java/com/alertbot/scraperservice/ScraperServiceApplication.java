package com.alertbot.scraperservice;

import com.alertbot.scraperservice.service.SSLUtil;
import com.alertbot.scraperservice.service.Scraper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScraperServiceApplication {
    public static void main(String[] args) {
        System.out.println("HOLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        SSLUtil.disableCertificateValidation();
        System.out.println("ADVERTENCIA: Validación SSL/TLS deshabilitada.");

        SpringApplication.run(ScraperServiceApplication.class, args);

    }
}
