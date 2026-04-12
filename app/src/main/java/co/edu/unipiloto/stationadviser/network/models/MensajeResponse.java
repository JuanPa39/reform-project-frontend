package co.edu.unipiloto.stationadviser.network.models;

public class MensajeResponse {
    private String mensaje;
    private boolean exito;
    private String token;

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}