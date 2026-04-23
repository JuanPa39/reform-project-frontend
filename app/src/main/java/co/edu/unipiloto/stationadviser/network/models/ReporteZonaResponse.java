package co.edu.unipiloto.stationadviser.network.models;

import java.util.List;

public class ReporteZonaResponse {
    private String zona;
    private double totalGalones;
    private double totalVentas;
    private List<DetalleEstacionResponse> detalleEstaciones;

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public double getTotalGalones() { return totalGalones; }
    public void setTotalGalones(double totalGalones) { this.totalGalones = totalGalones; }

    public double getTotalVentas() { return totalVentas; }
    public void setTotalVentas(double totalVentas) { this.totalVentas = totalVentas; }

    public List<DetalleEstacionResponse> getDetalleEstaciones() { return detalleEstaciones; }
    public void setDetalleEstaciones(List<DetalleEstacionResponse> detalleEstaciones) { this.detalleEstaciones = detalleEstaciones; }
}