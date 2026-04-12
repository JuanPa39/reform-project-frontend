package co.edu.unipiloto.stationadviser.network.models;

public class RegisterRequest {
    private String nombre;
    private String email;
    private String password;
    private String telefono;
    private String rol;

    public RegisterRequest(String nombre, String email, String password, String telefono, String rol) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.rol = rol;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getTelefono() { return telefono; }
    public String getRol() { return rol; }

    // Setters
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setRol(String rol) { this.rol = rol; }
}