package co.edu.unipiloto.stationadviser.network.models;

public class DisponibilidadResponse {
    private String combustibleNombre;  // ← Este es el nombre del campo
    private double cantidadDisponible;
    private boolean aplicaSubsidio;
    private String mensajeSubsidio;

    // Getters y Setters
    public String getCombustibleNombre() { return combustibleNombre; }
    public void setCombustibleNombre(String combustibleNombre) { this.combustibleNombre = combustibleNombre; }

    public double getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(double cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }

    public boolean isAplicaSubsidio() { return aplicaSubsidio; }
    public void setAplicaSubsidio(boolean aplicaSubsidio) { this.aplicaSubsidio = aplicaSubsidio; }

    public String getMensajeSubsidio() { return mensajeSubsidio; }
    public void setMensajeSubsidio(String mensajeSubsidio) { this.mensajeSubsidio = mensajeSubsidio; }
}