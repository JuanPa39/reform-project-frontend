package co.edu.unipiloto.stationadviser.network.models;

public class InventarioRequest {
    private String tipoCombustible;
    private int cantidad;

    public InventarioRequest(String tipoCombustible, int cantidad) {
        this.tipoCombustible = tipoCombustible;
        this.cantidad = cantidad;
    }

    public String getTipoCombustible() { return tipoCombustible; }
    public void setTipoCombustible(String tipoCombustible) { this.tipoCombustible = tipoCombustible; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}