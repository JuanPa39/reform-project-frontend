package co.edu.unipiloto.stationadviser.network.models;

public class VentaRequest {
    private String tipoCombustible;
    private double cantidad;
    private String tipoVehiculo;  // ← AGREGAR

    public VentaRequest(String tipoCombustible, double cantidad, String tipoVehiculo) {
        this.tipoCombustible = tipoCombustible;
        this.cantidad = cantidad;
        this.tipoVehiculo = tipoVehiculo;
    }

    public String getTipoCombustible() { return tipoCombustible; }
    public double getCantidad() { return cantidad; }
    public String getTipoVehiculo() { return tipoVehiculo; }
}