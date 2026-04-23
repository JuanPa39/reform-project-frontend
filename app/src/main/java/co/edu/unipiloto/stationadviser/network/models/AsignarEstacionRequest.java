package co.edu.unipiloto.stationadviser.network.models;

public class AsignarEstacionRequest {
    private Long estacionId;

    public AsignarEstacionRequest(Long estacionId) {
        this.estacionId = estacionId;
    }

    public Long getEstacionId() { return estacionId; }
    public void setEstacionId(Long estacionId) { this.estacionId = estacionId; }
}