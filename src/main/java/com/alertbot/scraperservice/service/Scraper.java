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


@Service
public class Scraper {

    private final ScrapedProductProducer scrapedProductProducer;

    public Scraper(ScrapedProductProducer scrapedProductProducer) {
        this.scrapedProductProducer = scrapedProductProducer;
    }

    public void scrapeWeb(AlertProduct product) {
        SSLUtil.disableCertificateValidation();
        System.out.println("ADVERTENCIA: Validación SSL/TLS deshabilitada.");

        // Codificar la búsqueda para la URL
        /*String busqueda = "aspiradora sin cable";
        String urlBusqueda = "https://www.amazon.es/s?k=" + busqueda.replace(" ", "+");*/
        int maxResultados = 15;
        String id_busqueda = product.getId();

        System.out.println("Buscando en: " + product.getURL_search());

        try {
            // 1. Conectar y obtener el Documento HTML
            Document doc = Jsoup.connect(product.getURL_search())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept-Language", "es-ES,es;q=0.9")
                    .get();

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
                    scrapeProduct(urlCompleta, id_busqueda);

                    contador++;
                }


            }

            if (contador == 0) {
                System.out.println("No se encontraron resultados con el selector: " + selectorContenedor);
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al conectar o al leer la página. Amazon podría haber bloqueado la solicitud.");
        }
    }

    private void scrapeProduct(String url, String id_busqueda) {
        SSLUtil.disableCertificateValidation();

        System.out.println("\n--- Buscando producto en: " + url + " ---");

        try {
            // 1. Conectar y obtener el Documento HTML
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept-Language", "es-ES,es;q=0.9")
                    .get();

            // Llamadas a las nuevas funciones de extracción
            String name = extractName(doc);
            String brand = extractBrand(doc);
            Double price = extractPrice(doc);
            Double rating = extractRating(doc);

            // Imprimir resultado
            System.out.println("-> Nombre: " + name + " Marca: " + brand + " Precio: " + price + "Valoración: " + rating);

            ScrapedProduct scrapedProduct = new ScrapedProduct(id_busqueda, name, url, brand, price, rating);

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


