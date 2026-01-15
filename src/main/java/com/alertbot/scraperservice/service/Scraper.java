package com.alertbot.scraperservice.service;

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

    public Scraper(ScrapedProductProducer scrapedProductProducer, ProductStatusManager statusManager) {
        this.scrapedProductProducer = scrapedProductProducer;
        this.statusManager = statusManager;
    }

    public void scrapeWeb(AlertProduct product) {
        SSLUtil.disableCertificateValidation();
        System.out.println("ADVERTENCIA: Validación SSL/TLS deshabilitada.");

        int maxResultados = 15;
        String requestID = product.getRequest_id();

        System.out.println("Buscando en: " + product.getURL_search());

        try {
            // 1. Conectar y obtener el Documento HTML
            Document doc = Jsoup.connect(product.getURL_search())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept-Language", "es-ES,es;q=0.9")
                    .get();

            // CAMBIAMOS EL STATUS A SEARCHING
            statusManager.updateToSearching(requestID);

            // 2. Definir el Selector CSS
            String selectorContenedor = ".s-result-item";// Un selector común y más estable

            // 3. Seleccionar todos los elementos de resultado
            Elements resultados = doc.select(selectorContenedor);

            int contador = 0;
            System.out.println("\nEnlaces de los resultados:");

            for (Element resultado : resultados) {
                if (contador >= maxResultados) {
                    break; // Detener después de los primeros 15
                }

                // 4. Buscar la etiqueta <a> dentro del contenedor actual
                Element enlaceElemento = resultado.selectFirst("a:has(h2)");

                if (enlaceElemento != null) {
                    // 5. Extraer el atributo 'href'
                    String href = enlaceElemento.attr("href");

                    // 6. Construir la URL completa
                    String urlCompleta = "https://www.amazon.es" + href;

                    // 7. Llamar al scraper de producto
                    scrapeProduct(product);

                    contador++;
                }

            }

            if (contador == 0) {
                System.out.println("No se encontraron resultados con el selector: " + selectorContenedor);
                statusManager.updateToFailed(requestID);
            } else {
                System.out.println("Búsqueda finalizada");
                statusManager.updateToCompleted(requestID);
            }



        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al conectar o al leer la página. Amazon podría haber bloqueado la solicitud.");
            statusManager.updateToFailed(requestID);
        }
    }

    private void scrapeProduct(AlertProduct product) {
        SSLUtil.disableCertificateValidation();

        System.out.println("\n--- Buscando producto en: " + product.getURL_search() + " ---");

        try {
            // 1. Conectar y obtener el Documento HTML
            Document doc = Jsoup.connect(product.getURL_search())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept-Language", "es-ES,es;q=0.9")
                    .get();

            // Llamadas a las nuevas funciones de extracción
            String productId = UUID.randomUUID().toString();
            String name = extractName(doc);
            String brand = extractBrand(doc);
            Double price = extractPrice(doc);
            Double rating = extractRating(doc);

            // Imprimir resultado
            System.out.println("-> Nombre: " + name + " Marca: " + brand + " Precio: " + price + "Valoración: " + rating);

            ScrapedProduct scrapedProduct = new ScrapedProduct(productId, product.getRequest_id(), product.getUser_id(), name, product.getURL_search(), brand, price, rating);

            /* MANDAR RESULTADO AL PRODUCTOR
                scrapedProductProducer.sendMessage(scrapedProduct);
            * */


        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al conectar o al leer la página. Amazon podría haber bloqueado la solicitud.");
        }


    }

    private String extractName(Document doc) {

        String selectorTitulo = "#productTitle";
        Element tituloElemento = doc.selectFirst(selectorTitulo);

        if (tituloElemento != null) {
            return tituloElemento.text().trim();
        }
        return "";
    }

    private String extractBrand(Document doc) {
        String selectorMarca = "#bylineInfo";
        Element marcaElemento = doc.selectFirst(selectorMarca);

        if (marcaElemento != null) {
            String textoCompleto = marcaElemento.text().trim();
            String prefijo = "Visita la tienda de ";

            if (textoCompleto.startsWith(prefijo)) {
                return textoCompleto.substring(prefijo.length());
            } else {
                return textoCompleto;
            }
        }
        return "";

    }

    private Double extractPrice(Document doc) {
        var priceContainer = doc.selectFirst("div#corePriceDisplay_desktop_feature_div");

        if (priceContainer != null) {
            var priceWhole = priceContainer.selectFirst("span.a-price-whole");
            var priceFraction = priceContainer.selectFirst("span.a-price-fraction");

            if (priceWhole != null && priceFraction != null) {
                try {
                    String whole = priceWhole.text().trim().replaceAll("[^0-9]", "");
                    String fraction = priceFraction.text().trim();
                    return Double.parseDouble(whole + "." + fraction);
                } catch (Exception e) {
                    return 0.0;
                }
            }
        }
        return 0.0;

    }

    private Double extractRating(Document doc) {
        Element visibleRating = doc.selectFirst("span[data-action='a-popover'] a span.a-color-base");

        if (visibleRating != null) {
            try {
                String valorStr = visibleRating.text().trim().replace(",", ".");
                return Double.parseDouble(valorStr);
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;

    }

}


