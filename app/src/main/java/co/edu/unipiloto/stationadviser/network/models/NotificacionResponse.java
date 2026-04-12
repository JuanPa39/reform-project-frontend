package co.edu.unipiloto.stationadviser.network.models;

public class NotificacionResponse {
    private Long id;
    private String estacionNombre;
    private String inconsistencia;
    private String estado;
    private String fecha;
    private String mensaje;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEstacionNombre() { return estacionNombre; }
    public void setEstacionNombre(String estacionNombre) { this.estacionNombre = estacionNombre; }

    public String getInconsistencia() { return inconsistencia; }
    public void setInconsistencia(String inconsistencia) { this.inconsistencia = inconsistencia; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getMensaje() {
        // Si mensaje no existe, devolver inconsistencia
        return mensaje != null ? mensaje : inconsistencia;
    }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}