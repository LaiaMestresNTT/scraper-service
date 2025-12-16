package com.alertbot.scraperservice.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class Scraper {

    String divLink = "data-cy";
    String divContent = "title-recipe";
    String aClass = "a-link-normal s-line-clamp-4 s-link-style a-text-normal"; // buscar text de href de esta etiqueta con esta clase

    public void scrapeWeb() {
        SSLUtil.disableCertificateValidation();
        System.out.println("ADVERTENCIA: Validación SSL/TLS deshabilitada.");

        String busqueda = "aspiradora sin cable";
        // Codificar la búsqueda para la URL
        String urlBusqueda = "https://www.amazon.es/s?k=" + busqueda.replace(" ", "+");
        int maxResultados = 15;

        System.out.println("Buscando en: " + urlBusqueda);

        try {
            // 1. Conectar y obtener el Documento HTML
            Document doc = Jsoup.connect(urlBusqueda)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept-Language", "es-ES,es;q=0.9")
                    .get();

            // 2. Definir el Selector CSS
            // El selector CSS para los contenedores de resultados en Amazon suele ser una clase que empieza por 's-result-item'
            // A menudo, se combina con el atributo data-index para asegurar que solo se obtengan los resultados reales.
            String selectorContenedor = "div[data-component='s-product-result']"; // Un selector común y más estable
            // O uno más general: String selectorContenedor = ".s-result-item";

            // 3. Seleccionar todos los elementos de resultado
            Elements resultados = doc.select(selectorContenedor);

            int contador = 0;
            System.out.println("\nEnlaces de los resultados:");

            for (Element resultado : resultados) {
                if (contador >= maxResultados) {
                    break; // Detener después de los primeros 15
                }

                // 4. Buscar la etiqueta <a> dentro del contenedor actual
                // El enlace del producto suele estar en una etiqueta <a> con la clase 'a-link-normal'
                // y que contiene el título del producto.
                Element enlaceElemento = resultado.selectFirst("a.a-link-normal[href]");

                if (enlaceElemento != null) {
                    // 5. Extraer el atributo 'href'
                    String href = enlaceElemento.attr("href");

                    // 6. Construir la URL completa
                    // Amazon a menudo usa URLs relativas, por lo que las hacemos absolutas.
                    String urlCompleta = "https://www.amazon.es" + href;

                    System.out.println((contador + 1) + ". " + urlCompleta);
                    contador++;
                }
            }

            if (contador == 0) {
                System.out.println("No se encontraron resultados con el selector: " + selectorContenedor);
                System.out.println("--- NOTA IMPORTANTE ---");
                System.out.println("El selector HTML de Amazon (y de muchos sitios grandes) cambia frecuentemente. ");
                System.out.println("Es posible que necesites inspeccionar el HTML actual de la página para encontrar el selector correcto.");
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al conectar o al leer la página. Amazon podría haber bloqueado la solicitud.");
        }
    }

    private void scrapeResults() {

    }

}


