package com.example.gasolineras_espana.model;

import java.util.List;

public class GeoJsonData {

    private String fechaActualizacion;
    private List<Gasolinera> gasolineras;

    public GeoJsonData() {
    }

    public GeoJsonData(String fechaActualizacion, List<Gasolinera> gasolineras) {
        this.fechaActualizacion = fechaActualizacion;
        this.gasolineras = gasolineras;
    }

    public String getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(String fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public List<Gasolinera> getGasolineras() {
        return gasolineras;
    }

    public void setGasolineras(List<Gasolinera> gasolineras) {
        this.gasolineras = gasolineras;
    }
}