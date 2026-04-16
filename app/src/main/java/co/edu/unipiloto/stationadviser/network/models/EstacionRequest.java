package co.edu.unipiloto.stationadviser.network.models;

public class EstacionRequest {
    private String nombre;
    private String nit;
    private String ubicacion;
    private double latitud;
    private double longitud;

    public EstacionRequest(String nombre, String nit, String ubicacion) {
        this.nombre = nombre;
        this.nit = nit;
        this.ubicacion = ubicacion;
        this.latitud = 0;
        this.longitud = 0;
    }

    public EstacionRequest(String nombre, String nit, String ubicacion, double latitud, double longitud) {
        this.nombre = nombre;
        this.nit = nit;
        this.ubicacion = ubicacion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
}