package com.alertbot.scraperservice;

import com.alertbot.scraperservice.service.SSLUtil;
import com.alertbot.scraperservice.service.Scraper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class ScraperServiceApplication 
    public static void main(String[] args) {

        SSLUtil.disableCertificateValidation();
        System.out.println("ADVERTENCIA: Validación SSL/TLS deshabilitada.");

        SpringApplication.run(ScraperServiceApplication.class, args);

    }

}
