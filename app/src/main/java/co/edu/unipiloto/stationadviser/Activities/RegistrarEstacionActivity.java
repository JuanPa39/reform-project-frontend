package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.EstacionRequest;
import co.edu.unipiloto.stationadviser.network.models.EstacionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarEstacionActivity extends AppCompatActivity {

    private EditText editTextNombre, editTextNit, editTextUbicacion;
    private Button buttonRegistrar;
    private TextView textViewMensaje, textViewTitulo;
    private ProgressBar progressBar;

    private ApiService apiService;
    private TokenManager tokenManager;

    private boolean modoEdicion = false;
    private Long estacionId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_estacion);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        editTextNombre = findViewById(R.id.editTextNombre);
        editTextNit = findViewById(R.id.editTextNit);
        editTextUbicacion = findViewById(R.id.editTextUbicacion);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);
        textViewMensaje = findViewById(R.id.textViewMensaje);
        textViewTitulo = findViewById(R.id.textViewTitulo);
        progressBar = findViewById(R.id.progressBar);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("modo_edicion", false)) {
            modoEdicion = true;
            estacionId = extras.getLong("estacion_id", -1L);

            textViewTitulo.setText("Editar Estación");
            buttonRegistrar.setText("Actualizar Estación");

            editTextNombre.setText(extras.getString("estacion_nombre"));
            editTextNit.setText(extras.getString("estacion_nit"));
            editTextUbicacion.setText(extras.getString("estacion_ubicacion"));
        }

        buttonRegistrar.setOnClickListener(v -> {
            if (modoEdicion) {
                actualizarEstacion();
            } else {
                registrarEstacion();
            }
        });
    }

    private void registrarEstacion() {
        String nombre = editTextNombre.getText().toString().trim();
        String nit = editTextNit.getText().toString().trim();
        String ubicacion = editTextUbicacion.getText().toString().trim();

        if (nombre.isEmpty() || nit.isEmpty() || ubicacion.isEmpty()) {
            textViewMensaje.setText("Todos los campos son obligatorios");
            return;
        }

        mostrarLoading(true);

        EstacionRequest request = new EstacionRequest(nombre, nit, ubicacion);
        Call<EstacionResponse> call = apiService.crearEstacion(request);
        call.enqueue(new Callback<EstacionResponse>() {
            @Override
            public void onResponse(Call<EstacionResponse> call, Response<EstacionResponse> response) {
                mostrarLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(RegistrarEstacionActivity.this, "Estación registrada con éxito", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                    finish();
                } else {
                    textViewMensaje.setText("Error: El NIT ya existe o hubo un problema");
                }
            }

            @Override
            public void onFailure(Call<EstacionResponse> call, Throwable t) {
                mostrarLoading(false);
                textViewMensaje.setText("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void actualizarEstacion() {
        String nombre = editTextNombre.getText().toString().trim();
        String nit = editTextNit.getText().toString().trim();
        String ubicacion = editTextUbicacion.getText().toString().trim();

        if (nombre.isEmpty() || nit.isEmpty() || ubicacion.isEmpty()) {
            textViewMensaje.setText("Todos los campos son obligatorios");
            return;
        }

        mostrarLoading(true);

        EstacionRequest request = new EstacionRequest(nombre, nit, ubicacion);
        Call<EstacionResponse> call = apiService.actualizarEstacion(estacionId, request);
        call.enqueue(new Callback<EstacionResponse>() {
            @Override
            public void onResponse(Call<EstacionResponse> call, Response<EstacionResponse> response) {
                mostrarLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(RegistrarEstacionActivity.this, "Estación actualizada con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    textViewMensaje.setText("Error al actualizar");
                }
            }

            @Override
            public void onFailure(Call<EstacionResponse> call, Throwable t) {
                mostrarLoading(false);
                textViewMensaje.setText("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void limpiarCampos() {
        editTextNombre.setText("");
        editTextNit.setText("");
        editTextUbicacion.setText("");
        textViewMensaje.setText("");
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? android.view.View.VISIBLE : android.view.View.GONE);
        buttonRegistrar.setEnabled(!mostrar);
    }
}