package co.edu.unipiloto.stationadviser.network.models;

public class AbastecimientoRequest {
    private Long distribuidorId;
    private Long combustibleId;
    private Double cantidadGalones;

    public AbastecimientoRequest(Long distribuidorId, Long combustibleId, Double cantidadGalones) {
        this.distribuidorId = distribuidorId;
        this.combustibleId = combustibleId;
        this.cantidadGalones = cantidadGalones;
    }

    public Long getDistribuidorId() { return distribuidorId; }
    public Long getCombustibleId() { return combustibleId; }
    public Double getCantidadGalones() { return cantidadGalones; }
}