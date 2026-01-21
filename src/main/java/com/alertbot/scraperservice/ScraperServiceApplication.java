package com.alertbot.scraperservice;

import com.alertbot.scraperservice.scraper.SSLUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ScraperServiceApplication {
    public static void main(String[] args) {

        //SSLUtil.disableCertificateValidation();
        //System.out.println("ADVERTENCIA: Validación SSL/TLS deshabilitada.");

        SpringApplication.run(ScraperServiceApplication.class, args);

    }

}
