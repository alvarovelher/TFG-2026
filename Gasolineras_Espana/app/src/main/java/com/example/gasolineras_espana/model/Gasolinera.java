package com.example.gasolineras_espana.model;

public class Gasolinera {

    private String id;
    private String rotulo;
    private String direccion;
    private String municipio;
    private String provincia;
    private String cp;
    private String horario;

    private double lat;
    private double lon;

    private double p95;
    private double p98;
    private double diesel;
    private double dieselPremium;
    private double glp;
    private double gnc;
    private double gnl;

    public Gasolinera() {
    }

    public Gasolinera(String id, String rotulo, String direccion, String municipio, String provincia,
                      String cp, String horario, double lat, double lon,
                      double p95, double p98, double diesel, double dieselPremium,
                      double glp, double gnc, double gnl) {
        this.id = id;
        this.rotulo = rotulo;
        this.direccion = direccion;
        this.municipio = municipio;
        this.provincia = provincia;
        this.cp = cp;
        this.horario = horario;
        this.lat = lat;
        this.lon = lon;
        this.p95 = p95;
        this.p98 = p98;
        this.diesel = diesel;
        this.dieselPremium = dieselPremium;
        this.glp = glp;
        this.gnc = gnc;
        this.gnl = gnl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getP95() {
        return p95;
    }

    public void setP95(double p95) {
        this.p95 = p95;
    }

    public double getP98() {
        return p98;
    }

    public void setP98(double p98) {
        this.p98 = p98;
    }

    public double getDiesel() {
        return diesel;
    }

    public void setDiesel(double diesel) {
        this.diesel = diesel;
    }

    public double getDieselPremium() {
        return dieselPremium;
    }

    public void setDieselPremium(double dieselPremium) {
        this.dieselPremium = dieselPremium;
    }

    public double getGlp() {
        return glp;
    }

    public void setGlp(double glp) {
        this.glp = glp;
    }

    public double getGnc() {
        return gnc;
    }

    public void setGnc(double gnc) {
        this.gnc = gnc;
    }

    public double getGnl() {
        return gnl;
    }

    public void setGnl(double gnl) {
        this.gnl = gnl;
    }
}