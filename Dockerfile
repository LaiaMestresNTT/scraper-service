# Dockerfile

# 1. IMAGEN BASE DEBIAN (JAMMY)
FROM eclipse-temurin:21-jdk-jammy

# 2. INSTALACIÓN DE CHROME y DEPENDENCIAS
# Utilizar una sola capa RUN para la instalación completa es eficiente

RUN apt-get update && \
    apt-get install -y wget unzip \
    # Instalar dependencias esenciales de renderizado que Chrome pueda necesitar
    libgbm1 libnss3 libasound2 \
    # --- DESCARGAR E INSTALAR CHROME ---
    # wget descarga el instalador de Chrome
    && wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb -O /tmp/chrome.deb \
    # dpkg instala el paquete. El comando 'apt-get -fy install' resuelve cualquier dependencia faltante automáticamente.
    && dpkg -i /tmp/chrome.deb || apt-get -fy install \
    # --- FIN INSTALACIÓN --- \
    # Limpieza
    && rm -f /tmp/chrome.deb \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# 3. CONFIGURACIÓN DEL PROYECTO
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]