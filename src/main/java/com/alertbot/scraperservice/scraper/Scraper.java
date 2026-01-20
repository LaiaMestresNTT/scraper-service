package com.alertbot.scraperservice.scraper;

import com.alertbot.scraperservice.kafka.ScrapedProductProducer;
import com.alertbot.scraperservice.model.AlertProduct;
import com.alertbot.scraperservice.model.ScrapedProduct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;


@Service
public class Scraper {

    private final ScrapedProductProducer scrapedProductProducer;
    private final ProductStatusManager statusManager;
    private final LabelExtractor labelExtractor;
    private java.util.Map<String, String> cookies = new java.util.HashMap<>();

    public Scraper(ScrapedProductProducer scrapedProductProducer, ProductStatusManager statusManager, LabelExtractor labelExtractor) {
        this.scrapedProductProducer = scrapedProductProducer;
        this.statusManager = statusManager;
        this.labelExtractor = labelExtractor;
    }

    private Document connect(String url) throws IOException {
        org.jsoup.Connection.Response response = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
            .header("Accept-Language", "es-ES,es;q=0.9")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Connection", "keep-alive")
            .header("Upgrade-Insecure-Requests", "1")
            .header("Referer", "https://www.google.com/")
            .cookies(cookies)
            .method(org.jsoup.Connection.Method.GET)
            .execute();

        cookies.putAll(response.cookies());
        return response.parse();
    }

    /* Etiquetas antiguas
        String selectorContenedor = ".s-result-item";// Un selector común y más estable

        3. Seleccionar todos los elementos de resultado
            Elements resultados = doc.select(selectorContenedor);
        4. Buscar la etiqueta <a> dentro del contenedor actual
            Element enlaceElemento = resultado.selectFirst("a:has(h2)");
        5. Extraer el atributo 'href'
            String href = enlaceElemento.attr("href");
    */
    public void scrapeWeb(AlertProduct product) {
        String requestID = product.getRequest_id();
        //  CAMBIAR STATUS A SEARCHING
        statusManager.updateToSearching(requestID);
        int contador = 0;

        try {
            SSLUtil.disableCertificateValidation();
            System.out.println("ADVERTENCIA: Validación SSL/TLS deshabilitada .");
            Document searchDoc = connect(product.getURL_search());

            // Selector de todos los productos que evita anuncios (patrocinados)
            Elements links = searchDoc.select("div[data-component-type='s-search-result'] h2 a.a-link-normal");

            for (Element link : links) {
                if (contador >= 5) break;

                // Montar URL para producto específico
                String urlCompleta = "https://www.amazon.es" + link.attr("href");

                // Espera entre 1.5 y 3.5 segundos para no parecer un bot
                Thread.sleep(1500 + (long)(Math.random() * 2000));

                // Verificamos si el producto ha sido extraído
                if (processIndividualProduct(product, urlCompleta)) {
                    contador++;
                }
            }

            if (contador > 0) {
                statusManager.updateToCompleted(requestID);
            } else {
                statusManager.updateToFailed(requestID);
            }

        } catch (Exception e) {
            System.err.println("Error en proceso: " + e.getMessage());
            statusManager.updateToFailed(requestID);
        }
    }

    private boolean processIndividualProduct(AlertProduct target, String url) {
        try {
            SSLUtil.disableCertificateValidation();
            System.out.println("ADVERTENCIA: Validación SSL/TLS deshabilitada.");
            Document doc = connect(url);

            // Extracción de datos
            String name = labelExtractor.extractName(doc);
            String brand = labelExtractor.extractBrand(doc);
            double price = labelExtractor.extractPrice(doc);
            double rating = labelExtractor.extractRating(doc);

            // Verificación de datos
            boolean matchesBrand = brand.equalsIgnoreCase(target.getBrand());
            boolean matchesPrice = price > 0 && price <= target.getPrice();
            boolean matchesRating = rating >= target.getRating();

            if (matchesBrand && matchesPrice && matchesRating) {
                ScrapedProduct result = new ScrapedProduct(UUID.randomUUID().toString(), target.getRequest_id(), target.getUser_id(), name, url, brand, price, rating);

                // MANDAMOS AL TOPICO EL PRODUCTO
                scrapedProductProducer.sendMessage(result);
                return true;
            }

        } catch (IOException e) {
            System.err.println("Error al acceder a producto: " + url);
        }
        return false;
    }

}


