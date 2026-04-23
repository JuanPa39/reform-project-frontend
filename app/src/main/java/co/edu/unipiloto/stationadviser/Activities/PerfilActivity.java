package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.ActualizarUsuarioRequest;
import co.edu.unipiloto.stationadviser.network.models.UsuarioResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etEmail, etTelefono;
    private Button btnGuardar;
    private ProgressBar progressBar;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Toolbar con flecha de regreso
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil");
        }

        etNombre   = findViewById(R.id.etNombre);
        etEmail    = findViewById(R.id.etEmail);
        etTelefono = findViewById(R.id.etTelefono);
        btnGuardar = findViewById(R.id.btnGuardar);
        progressBar = findViewById(R.id.progressBar);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken())
                .create(ApiService.class);

        cargarPerfil();
        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void cargarPerfil() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getMiPerfil().enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    UsuarioResponse u = response.body();
                    etNombre.setText(u.getNombre());
                    etEmail.setText(u.getEmail());
                    etTelefono.setText(u.getTelefono() != null ? u.getTelefono() : "");
                } else {
                    Toast.makeText(PerfilActivity.this,
                            "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PerfilActivity.this,
                        "Sin conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarCambios() {
        String nombre   = etNombre.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nombre y email son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        apiService.actualizarMiPerfil(new ActualizarUsuarioRequest(nombre, email, telefono))
                .enqueue(new Callback<UsuarioResponse>() {
                    @Override
                    public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        btnGuardar.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            tokenManager.saveTokens(
                                    tokenManager.getToken(),
                                    tokenManager.getRefreshToken(),
                                    response.body().getEmail(),
                                    response.body().getNombre(),
                                    response.body().getRol()
                            );
                            Toast.makeText(PerfilActivity.this,
                                    "Perfil actualizado", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(PerfilActivity.this,
                                    "Error al guardar cambios", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnGuardar.setEnabled(true);
                        Toast.makeText(PerfilActivity.this,
                                "Sin conexión con el servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}