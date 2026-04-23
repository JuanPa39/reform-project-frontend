package co.edu.unipiloto.stationadviser.network.models;

public class EstacionResponse {
    private Long id;
    private String nombre;
    private String nit;
    private String ubicacion;
    private boolean activa;
    private double latitud;
    private double longitud;

    private String zona;
    public EstacionResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
}