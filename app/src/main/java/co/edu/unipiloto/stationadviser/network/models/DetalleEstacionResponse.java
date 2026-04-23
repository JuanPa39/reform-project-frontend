package co.edu.unipiloto.stationadviser.network.models;

public class DetalleEstacionResponse {
    private Long estacionId;
    private String estacionNombre;
    private double galonesVendidos;
    private double montoTotal;

    public Long getEstacionId() { return estacionId; }
    public void setEstacionId(Long estacionId) { this.estacionId = estacionId; }

    public String getEstacionNombre() { return estacionNombre; }
    public void setEstacionNombre(String estacionNombre) { this.estacionNombre = estacionNombre; }

    public double getGalonesVendidos() { return galonesVendidos; }
    public void setGalonesVendidos(double galonesVendidos) { this.galonesVendidos = galonesVendidos; }

    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }
}