package com.alertbot.scraperservice.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit; // Para usar esperas implícitas (mejor que Thread.sleep)

@Service
public class Scraper {

    //public void scrapeUdemy(AlertProduct product) {
    public void scrapeUdemy() {
        // 1. CONFIGURACIÓN DEL DRIVER
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Esencial para entornos Docker/Linux sin interfaz gráfica
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        WebDriver driver = null;

        try {
            driver = new ChromeDriver(options);
            // Configura una espera implícita: Selenium esperará hasta 10 segundos
            // a que un elemento aparezca antes de lanzar un error.
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            String URL_test = "https://www.udemy.com/courses/search/?q=python&duration=long&ratings=4.0&lang=es&instructional_level=intermediate";

            // 2. NAVEGAR DIRECTAMENTE A LA URL DE RESULTADOS
            System.out.println("Navegando directamente a: " + URL_test);
            driver.get(URL_test);

            // Damos un tiempo extra para que el JavaScript cargue todos los resultados
            // Es preferible usar 'waits' explícitas, pero Thread.sleep(5000) funciona para empezar.
            Thread.sleep(5000);

            // 3. SCRAPEAR E ITERAR LOS RESULTADOS
            scrapeResults(driver);

        } catch (Exception e) {
            System.err.println("Ocurrió un error durante el scraping: " + e.getMessage());
        } finally {
            // 4. CERRAR EL NAVEGADOR
            if (driver != null) {
                driver.quit();
                System.out.println("\nScraping finalizado y navegador cerrado.");
            }
        }

    }

    private void scrapeResults(WebDriver driver) {
        // Selector CSS para la tarjeta individual del curso
        String cardSelector = ".content-grid-item-module--item--MDYzd";

        // Localizar TODOS los contenedores de curso en la página
        List<WebElement> courseCards = driver.findElements(By.cssSelector(cardSelector));

        System.out.println("\nCursos encontrados: " + courseCards.size());

        // Iterar sobre cada tarjeta encontrada
        for (int i = 0; i < courseCards.size(); i++) {
            WebElement card = courseCards.get(i);

            try {
                // Título del curso (usando data-purpose)
                String titulo = card.findElement(By.cssSelector("[data-purpose='course-title-url']")).getText();

                // Precio del curso
                String precio = card.findElement(By.cssSelector("[data-purpose='course-price-text']")).getText();

                // Extraer el enlace (opcional)
                String enlace = card.findElement(By.cssSelector("[data-purpose='course-title-url']")).getAttribute("href");

                System.out.println("----------------------------------------");
                System.out.println("Curso #" + (i + 1));
                System.out.println("Título: " + titulo);
                System.out.println("Precio: " + precio);
                System.out.println("Enlace: " + enlace);

            } catch (Exception e) {
                System.err.println("Error al extraer detalles del curso #" + (i + 1) + ". Saltando tarjeta.");
            }
        }
    }

}


