package com.example.gasolineras_espana.geojson;

import com.example.gasolineras_espana.model.GeoJsonData;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeoJsonDownloader {

    // Este metodo ahora devuelve directamente un objeto GeoJsonData procesado
    // Asi evitamos almacenar megabytes de texto en un String, protegiendo la memoria RAM.
    public GeoJsonData descargarYParsear(String urlString) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000); // 15 segundos maximo de espera de red
            connection.setReadTimeout(15000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Error HTTP: " + responseCode);
            }

            // Conseguimos el flujo de bits directamente desde internet
            InputStream inputStream = connection.getInputStream();
            
            // Le pasamos la manguera a nuestro parseador super-eficiente
            GeoJsonParser parser = new GeoJsonParser();
            return parser.parsearStream(inputStream);

        } finally {
            // Siempre aseguramos cerrar la conexion para no agotar recursos
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}