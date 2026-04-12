package co.edu.unipiloto.stationadviser.network.models;

public class VentaRequest {
    private String tipoCombustible;
    private double cantidad;  // En galones

    public VentaRequest(String tipoCombustible, double cantidad) {
        this.tipoCombustible = tipoCombustible;
        this.cantidad = cantidad;
    }

    public String getTipoCombustible() { return tipoCombustible; }
    public void setTipoCombustible(String tipoCombustible) { this.tipoCombustible = tipoCombustible; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
}