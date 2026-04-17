package com.example.gasolineras_espana.geojson;

import android.util.JsonReader;
import android.util.JsonToken;

import com.example.gasolineras_espana.model.Gasolinera;
import com.example.gasolineras_espana.model.GeoJsonData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GeoJsonParser {

    /**
     * Parseador "fuga de tubería". Lee bit a bit el InputStream de forma super ligera.
     * Cero fallos de memoria RAM.
     */
    public GeoJsonData parsearStream(InputStream in) throws Exception {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        
        String fechaActualizacion = "";
        List<Gasolinera> listaGasolineras = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("fechaActualizacion")) {
                fechaActualizacion = reader.nextString();
            } else if (name.equals("features")) {
                listaGasolineras = leerFeaturesArray(reader);
            } else {
                reader.skipValue(); // Nos saltamos las basuras que no nos interesan
            }
        }
        reader.endObject();
        reader.close();

        return new GeoJsonData(fechaActualizacion, listaGasolineras);
    }

    private List<Gasolinera> leerFeaturesArray(JsonReader reader) throws Exception {
        List<Gasolinera> gasolineras = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            Gasolinera gas = leerFeature(reader);
            if (gas != null) {
                gasolineras.add(gas);
            }
        }
        reader.endArray();
        return gasolineras;
    }

    private Gasolinera leerFeature(JsonReader reader) throws Exception {
        Gasolinera g = new Gasolinera();
        
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("properties") && reader.peek() != JsonToken.NULL) {
                leerProperties(reader, g);
            } else if (name.equals("geometry") && reader.peek() != JsonToken.NULL) {
                leerGeometry(reader, g);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return g;
    }

    private void leerProperties(JsonReader reader, Gasolinera g) throws Exception {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (reader.peek() == JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            // Mapeamos los datos como si fueramos nosotros!
            switch (name) {
                case "id": g.setId(reader.nextString()); break;
                case "rotulo": g.setRotulo(reader.nextString()); break;
                case "direccion": g.setDireccion(reader.nextString()); break;
                case "municipio": g.setMunicipio(reader.nextString()); break;
                case "provincia": g.setProvincia(reader.nextString()); break;
                case "cp": g.setCp(reader.nextString()); break;
                case "horario": g.setHorario(reader.nextString()); break;
                case "p95": g.setP95(leerDouble(reader)); break;
                case "p98": g.setP98(leerDouble(reader)); break;
                case "diesel": g.setDiesel(leerDouble(reader)); break;
                case "dieselPremium": g.setDieselPremium(leerDouble(reader)); break;
                case "glp": g.setGlp(leerDouble(reader)); break;
                case "gnc": g.setGnc(leerDouble(reader)); break;
                case "gnl": g.setGnl(leerDouble(reader)); break;
                default: reader.skipValue(); break; // si aparece un json key nuevo, se esquiva automagicamente
            }
        }
        reader.endObject();
    }

    private void leerGeometry(JsonReader reader, Gasolinera g) throws Exception {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("coordinates") && reader.peek() != JsonToken.NULL) {
                reader.beginArray();
                if (reader.hasNext()) g.setLon(reader.nextDouble());
                if (reader.hasNext()) g.setLat(reader.nextDouble());
                // Consumimos el resto de lat-lons por si tuvieran Z (altitud) 
                while (reader.hasNext()) reader.skipValue();
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private double leerDouble(JsonReader reader) throws Exception {
        JsonToken token = reader.peek();
        if (token == JsonToken.NUMBER) {
            return reader.nextDouble();
        } else if (token == JsonToken.STRING) {
            String texto = reader.nextString().trim();
            if (texto.isEmpty()) return -1.0;
            texto = texto.replace(",", "."); // Controlamos posibles comas españolas
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                return -1.0;
            }
        } else {
            reader.skipValue();
            return -1.0;
        }
    }
}