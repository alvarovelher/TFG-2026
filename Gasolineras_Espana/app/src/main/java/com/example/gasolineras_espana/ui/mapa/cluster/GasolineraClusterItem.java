package com.example.gasolineras_espana.ui.mapa.cluster;

import com.example.gasolineras_espana.model.Gasolinera;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

// Cada gasolinera necesita implementar ClusterItem para que la libreria de clustering la entienda.
// Guardamos la referencia al objeto original para poder usarla cuando el usuario la toca.
public class GasolineraClusterItem implements ClusterItem {

    private final LatLng position;
    private final String title;
    private final String snippet;
    private final Gasolinera gasolinera;

    public GasolineraClusterItem(Gasolinera gasolinera) {
        this.gasolinera = gasolinera;
        this.position = new LatLng(gasolinera.getLat(), gasolinera.getLon());

        // Si no hay rotulo, ponemos un valor generico para no dejar el marker vacio
        String rotulo = gasolinera.getRotulo();
        if (rotulo == null || rotulo.trim().isEmpty()) {
            rotulo = "Gasolinera";
        }
        this.title = rotulo;

        String municipioStr = gasolinera.getMunicipio() != null ? gasolinera.getMunicipio().trim() : "Desconocido";

        // El snippet aparece justo debajo del titulo en el popup del marker (fragmento de código reutilizable)
        this.snippet = "Municipio: " + municipioStr
                + " | P95: " + gasolinera.getP95()
                + " | Diesel: " + gasolinera.getDiesel();
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    @Override
    public Float getZIndex() {
        return null;
    }

    // Necesario para recuperar los datos completos cuando el usuario hace click en el marker
    public Gasolinera getGasolinera() {
        return gasolinera;
    }
}