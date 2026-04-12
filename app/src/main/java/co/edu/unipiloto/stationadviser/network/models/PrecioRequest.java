package co.edu.unipiloto.stationadviser.network.models;

public class PrecioRequest {
    private Long estacionId;
    private String tipoCombustible;
    private double precio;

    public PrecioRequest(Long estacionId, String tipoCombustible, double precio) {
        this.estacionId = estacionId;
        this.tipoCombustible = tipoCombustible;
        this.precio = precio;
    }

    public Long getEstacionId() { return estacionId; }
    public void setEstacionId(Long estacionId) { this.estacionId = estacionId; }
    public String getTipoCombustible() { return tipoCombustible; }
    public void setTipoCombustible(String tipoCombustible) { this.tipoCombustible = tipoCombustible; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
}