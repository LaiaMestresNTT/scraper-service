package com.alertbot.scraperservice.scraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class LabelExtractor {
    public String extractName(Document doc) {
        Element titulo = doc.selectFirst("#productTitle");
        return (titulo != null) ? titulo.text().trim() : "";
    }

    public String extractBrand(Document doc) {
        Element marcaElemento = doc.selectFirst("#bylineInfo");
        if (marcaElemento == null) return "Desconocida";

        String texto = marcaElemento.text().trim();
        // Mejoramos la lógica: Buscamos palabras clave y limpiamos el ruido
        return texto.replace("Visita la tienda de ", "")
                .replace("Marca: ", "")
                .replace("Visita la Store de ", "")
                .trim();
    }

    public double extractPrice(Document doc) {
        try {
            // Buscamos primero el contenedor principal de precio
            Element priceContainer = doc.selectFirst(".a-price .a-offscreen");
            if (priceContainer != null) {
                String priceText = priceContainer.text()
                        .replace("€", "")
                        .replace(".", "")  // Quitar separador de miles
                        .replace(",", ".") // Convertir decimal a formato Java
                        .trim();
                return Double.parseDouble(priceText);
            }
        } catch (Exception e) {
            // Log de error interno si fuera necesario
        }
        return 0.0;
    }

    public double extractRating(Document doc) {
        Element ratingElement = doc.selectFirst("span[data-action='a-popover'] a span.a-color-base");
        if (ratingElement != null) {
            try {
                return Double.parseDouble(ratingElement.text().trim().replace(",", "."));
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
