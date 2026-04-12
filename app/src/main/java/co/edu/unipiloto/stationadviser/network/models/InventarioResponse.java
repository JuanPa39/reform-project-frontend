package co.edu.unipiloto.stationadviser.network.models;

public class InventarioResponse {
    private Long id;
    private String estacionNombre;
    private String combustibleNombre;
    private double cantidadDisponible;
    private String fechaActualizacion;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEstacionNombre() { return estacionNombre; }
    public void setEstacionNombre(String estacionNombre) { this.estacionNombre = estacionNombre; }
    public String getCombustibleNombre() { return combustibleNombre; }
    public void setCombustibleNombre(String combustibleNombre) { this.combustibleNombre = combustibleNombre; }
    public double getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(double cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    public String getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(String fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}