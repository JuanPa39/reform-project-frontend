package co.edu.unipiloto.stationadviser.network.models;

public class PrecioResponse {
    private Long id;
    private String estacionNombre;
    private String combustibleNombre;
    private double precio;
    private String fecha;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEstacionNombre() { return estacionNombre; }
    public void setEstacionNombre(String estacionNombre) { this.estacionNombre = estacionNombre; }
    public String getCombustibleNombre() { return combustibleNombre; }
    public void setCombustibleNombre(String combustibleNombre) { this.combustibleNombre = combustibleNombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}