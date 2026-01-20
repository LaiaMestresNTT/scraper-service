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
        statusManager.updateToSearching(requestID);
        int contador = 0;

        try {
            // Documento de búsqueda
            Document searchDoc = connect(product.getURL_search());

            // 1. Seleccionar todos los elementos de resultado (Selector estable)
            Elements resultados = searchDoc.select(".s-result-item");

            System.out.println("DEBUG: Se han encontrado " + resultados.size() + " contenedores .s-result-item");

            for (Element resultado : resultados) {
                if (contador >= 5) break;

                // 2. Buscar la etiqueta <a> que contiene un h2 (Lógica pedida)
                Element enlaceElemento = resultado.selectFirst("a:has(h2)");

                // Verificamos que el enlace existe y no es un anuncio vacío
                if (enlaceElemento != null) {
                    // 3. Extraer el atributo 'href'
                    String href = enlaceElemento.attr("href");

                    // Evitar enlaces de publicidad externa que no empiezan por /
                    if (href.startsWith("/")) {
                        String urlCompleta = "https://www.amazon.es" + href;

                        System.out.println("🔗 Procesando: " + urlCompleta);

                        // Espera aleatoria para evitar el 503
                        Thread.sleep(2000 + (long)(Math.random() * 3000));

                        if (processIndividualProduct(product, urlCompleta)) {
                            contador++;
                        }
                    }
                }
            }

            if (contador > 0) {
                statusManager.updateToCompleted(requestID);
            } else {
                // Si llegamos aquí y resultados.size() era > 0, es que el selector a:has(h2) falló
                System.out.println("⚠️ No se pudieron extraer enlaces válidos de los resultados.");
                statusManager.updateToFailed(requestID);
            }

        } catch (Exception e) {
            System.err.println("❌ Error en proceso: " + e.getMessage());
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


