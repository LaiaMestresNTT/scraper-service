package com.alertbot.scraperservice.scraper;

import com.alertbot.scraperservice.kafka.ConfirmationProducer;
import com.alertbot.scraperservice.model.AlertProduct;
import com.alertbot.scraperservice.model.ScrapedProduct;
import com.alertbot.scraperservice.mongo.ProductStatusManager;
import com.alertbot.scraperservice.mongo.ScrapedProductRepository;
import com.alertbot.scraperservice.scorer.Scoring;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;


@Service
public class Scraper {

    private final ConfirmationProducer confirmationProducer;
    private final ProductStatusManager statusManager;
    private final LabelExtractor labelExtractor;
    private final ScrapedProductRepository productRepository;
    private final java.util.Map<String, String> cookies = new java.util.HashMap<>();
    private final Scoring score;
    private static final Random random = new Random();
    private static final List<String> USER_AGENTS = List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
    );

    public Scraper(ConfirmationProducer confirmationProducer, ProductStatusManager statusManager, LabelExtractor labelExtractor, ScrapedProductRepository productRepository, Scoring score) {
        this.confirmationProducer = confirmationProducer;
        this.statusManager = statusManager;
        this.labelExtractor = labelExtractor;
        this.productRepository = productRepository;
        this.score = score;
    }


    private Document connect(String url) throws IOException {
        String userAgent = USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));
        try {
            org.jsoup.Connection.Response response = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .header("Sec-Ch-Ua", "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"")
                    .header("Sec-Ch-Ua-Mobile", "?0")
                    .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Referer", "https://www.google.com/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .cookies(cookies)
                    .timeout(15000)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .execute();

            if (response.statusCode() == 503) {
                throw new IOException("Amazon detectó el bot (Error 503 - Captcha)");
            }
            if (response.statusCode() == 403) {
                throw new IOException("Acceso denegado (Error 403 - Bloqueo de IP)");
            }

            // Detectar página de captcha en el HTML
            Document doc = response.parse();
            if (doc.select("form[action='/errors/validateCaptcha']").first() != null) {
                throw new IOException("Amazon mostró captcha en el HTML");
            }

            cookies.putAll(response.cookies());
            return doc;

        } catch (IOException e) {
            System.err.println("❌ Error conectando a: " + url + " | Motivo: " + e.getMessage());
            throw e;
        }
    }

    public void scrapeWeb(AlertProduct product) {

        String requestID = product.getRequestId();
        int validProd_count = 0;
        int scrapedProd_count = 0;
        boolean iscompleted = false;

        try {
            // Añadimos un pausa random antes de empezar
            long delay = 3000 + random.nextInt(5000);
            Thread.sleep(delay);

            // Documento de búsqueda
            Document searchDoc = connect(product.getURL_search());

            // Cambiar status en la base de datos del product request
            statusManager.updateToSearching(requestID);

            // Detectar bloquo de Amazon
            if (searchDoc.title().contains("Captcha") || searchDoc.title().contains("Robot Check")) {
                // Borrar cookies y cambiar IP/User-Agent
                cookies.clear();
                throw new IOException("Bloqueo por Captcha detectado");
            }

            // 1. Seleccionar todos los elementos de resultado (Selector estable)
            Elements resultados = searchDoc.select(".s-result-item");


            for (Element resultado : resultados) {
                if (validProd_count >= 5 || scrapedProd_count >= 20) break;

                // 2. Buscar la etiqueta <a> que contiene un h2 (Lógica pedida)
                Element enlaceElemento = resultado.selectFirst("a:has(h2)");

                // Verificamos que el enlace existe y no es un anuncio vacío
                if (enlaceElemento != null) {
                    // 3. Extraer el atributo 'href'
                    String href = enlaceElemento.attr("href");

                    // Evitar enlaces de publicidad externa que no empiezan por /
                    if (href.startsWith("/")) {
                        String urlCompleta = "https://www.amazon.es" + href;

                        System.out.println("🔗 Procesando: " + scrapedProd_count + "/20");

                        // Espera aleatoria para evitar el 503
                        Thread.sleep(2000 + (long)(Math.random() * 3000));

                        if (processIndividualProduct(product, urlCompleta)) {
                            validProd_count++;
                        }
                        scrapedProd_count++;
                    }
                }
            }

            System.out.println(manageResult(product, validProd_count, iscompleted, null));


        } catch (Exception e) {
            e.printStackTrace(); // añadir esto temporalmente
            manageResult(product, validProd_count, iscompleted,
                    e.getMessage() != null ? e.getMessage() : e.getClass().getName());
        }
    }

    private boolean processIndividualProduct(AlertProduct target, String url) {
        try {
            //SSLUtil.disableCertificateValidation();
            Document doc = connect(url);

            // Extracción de datos
            String name = labelExtractor.extractName(doc);
            String brand = labelExtractor.extractBrand(doc);
            double price = labelExtractor.extractPrice(doc);
            double rating = labelExtractor.extractRating(doc);
            int ratingCount = labelExtractor.extractReviewCount(doc);

            // Calculamos el score
            double finalScore = score.calculateScore(rating, ratingCount);

            // Definimos un mínimo de score para poder procesarlo
            double MIN_SCORE = 7.5;

            // Verificación de datos
            boolean matchesBrand = brand.equalsIgnoreCase(target.getBrand());
            boolean matchesPrice = target.getPrice() == 0.0 || price <= target.getPrice();
            boolean matchesRating = target.getRating() == 0.0 || rating >= target.getRating();
            boolean isHighQuality = finalScore >= MIN_SCORE;

            if (matchesBrand && matchesPrice && matchesRating && isHighQuality) {
                ScrapedProduct result = new ScrapedProduct(UUID.randomUUID().toString(), target.getRequestId(), target.getUserId(), name, url, brand, price, rating, ratingCount, finalScore);

                // Guardamos el producto encontrado en la base de datos
                productRepository.save(result);
                System.out.println("🚀 Producto guardado en la base de datos --> ID: " + result.getProductId());
                return true;
            }

        } catch (IOException e) {
            System.err.println("Error al acceder a producto: " + url);
        }
        return false;
    }

    private String manageResult(AlertProduct product, int validProd_count, boolean iscompleted, String err) {
        String log;

        if (validProd_count > 0) {
            iscompleted = true;
            log = "✅ Se han extraído un total de : " + validProd_count + " productos";
        } else if (err != null && !err.isEmpty()) {
            log = "❌ Error en proceso: " + err;
        } else {
            log = "⚠️ No se pudieron extraer enlaces válidos de los resultados.";
        }

        // ACTUALIZAMOS STATUS
        statusManager.manageScrapingResult(product, iscompleted);
        // MANDAMOS CONFIRMACIÓN AL TOPIC
        confirmationProducer.sendMessage(product, validProd_count);

        return log;
    }

}


