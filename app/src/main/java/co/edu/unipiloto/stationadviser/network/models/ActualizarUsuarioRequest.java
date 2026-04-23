package co.edu.unipiloto.stationadviser.network.models;

public class ActualizarUsuarioRequest {
    private String nombre;
    private String email;
    private String telefono;

    public ActualizarUsuarioRequest(String nombre, String email, String telefono) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
    }

    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
}