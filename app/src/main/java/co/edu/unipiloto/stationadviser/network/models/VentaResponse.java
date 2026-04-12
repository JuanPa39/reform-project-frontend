package co.edu.unipiloto.stationadviser.network.models;

public class VentaResponse {
    private Long id;
    private String estacionNombre;
    private String combustibleNombre;
    private double cantidad;
    private double precioUnitario;
    private double montoTotal;
    private String fecha;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEstacionNombre() { return estacionNombre; }
    public void setEstacionNombre(String estacionNombre) { this.estacionNombre = estacionNombre; }

    public String getCombustibleNombre() { return combustibleNombre; }
    public void setCombustibleNombre(String combustibleNombre) { this.combustibleNombre = combustibleNombre; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}