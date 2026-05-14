package co.edu.unipiloto.stationadviser.network.models;

public class DistribuidorResponse {
    private Long id;
    private String nombre;
    private String zonaOperacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getZonaOperacion() { return zonaOperacion; }
    public void setZonaOperacion(String zonaOperacion) { this.zonaOperacion = zonaOperacion; }
}