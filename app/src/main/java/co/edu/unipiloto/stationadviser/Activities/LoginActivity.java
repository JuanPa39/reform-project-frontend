package co.edu.unipiloto.stationadviser.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.LoginRequest;
import co.edu.unipiloto.stationadviser.network.models.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText editTextCorreo, editTextContrasena;
    private Button buttonLogin;
    private TextView textViewMensaje, textRegistro, textRecuperar;
    private ProgressBar progressBar;

    private TokenManager tokenManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar TokenManager
        tokenManager = new TokenManager(this);

        // Inicializar ApiService
        apiService = ApiClient.getClient().create(ApiService.class);

        editTextCorreo    = findViewById(R.id.editTextCorreo);
        editTextContrasena = findViewById(R.id.editTextContrasena);
        buttonLogin       = findViewById(R.id.buttonLogin);
        textViewMensaje   = findViewById(R.id.textViewMensaje);
        textRegistro      = findViewById(R.id.textRegistro);
        textRecuperar     = findViewById(R.id.textRecuperar);
        progressBar       = findViewById(R.id.progressBar); // Asegúrate de tener ProgressBar en tu layout

        // Si ya hay sesión activa, ir directamente a RoleBaseActivity
        if (tokenManager.isLoggedIn()) {
            irARoleBaseActivity(tokenManager.getUserEmail(), tokenManager.getUserRole());
            return;
        }

        buttonLogin.setOnClickListener(v -> iniciarSesion());

        textRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        textRecuperar.setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class)));
    }

    private void iniciarSesion() {
        String correo    = editTextCorreo.getText() != null
                ? editTextCorreo.getText().toString().trim() : "";
        String contrasena = editTextContrasena.getText() != null
                ? editTextContrasena.getText().toString().trim() : "";

        if (correo.isEmpty() || contrasena.isEmpty()) {
            textViewMensaje.setText("Ingrese correo y contraseña");
            return;
        }

        // Mostrar loading
        mostrarLoading(true);
        textViewMensaje.setText("");

        // Crear request
        LoginRequest loginRequest = new LoginRequest(correo, contrasena);

        // Llamada al backend
        Call<LoginResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.isEnabled()) {
                        // Login exitoso - guardar tokens y datos
                        tokenManager.saveTokens(
                                loginResponse.getToken(),
                                loginResponse.getRefreshToken(),
                                loginResponse.getEmail(),
                                loginResponse.getNombre(),
                                loginResponse.getRol()
                        );

                        Toast.makeText(LoginActivity.this,
                                "Bienvenido " + loginResponse.getNombre(),
                                Toast.LENGTH_SHORT).show();

                        irARoleBaseActivity(loginResponse.getEmail(), loginResponse.getRol());
                    } else {
                        // Cuenta no confirmada
                        textViewMensaje.setText("Cuenta no confirmada. Revisa tu email.");
                    }
                } else {
                    // Error en la respuesta
                    try {
                        String errorMsg = response.errorBody() != null
                                ? response.errorBody().string()
                                : "Error en el servidor";
                        Log.e(TAG, "Error response: " + errorMsg);
                        textViewMensaje.setText("Credenciales incorrectas o error en el servidor");
                    } catch (Exception e) {
                        textViewMensaje.setText("Error de conexión");
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                mostrarLoading(false);
                Log.e(TAG, "Error de red: " + t.getMessage());
                textViewMensaje.setText("Error de conexión. ¿El servidor está corriendo?");
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        buttonLogin.setEnabled(!mostrar);
        buttonLogin.setText(mostrar ? "" : "Iniciar Sesión");
    }

    private void irARoleBaseActivity(String email, String rol) {
        Intent intent = new Intent(this, RoleBaseActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("role", rol);
        startActivity(intent);
        finish();
    }
}