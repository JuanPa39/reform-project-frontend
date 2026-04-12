package co.edu.unipiloto.stationadviser.network.models;

public class NotificacionRequest {
    private Long estacionId;
    private String inconsistencia;

    public NotificacionRequest(Long estacionId, String inconsistencia) {
        this.estacionId = estacionId;
        this.inconsistencia = inconsistencia;
    }

    public Long getEstacionId() { return estacionId; }
    public void setEstacionId(Long estacionId) { this.estacionId = estacionId; }

    public String getInconsistencia() { return inconsistencia; }
    public void setInconsistencia(String inconsistencia) { this.inconsistencia = inconsistencia; }
}