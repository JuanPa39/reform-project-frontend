package co.edu.unipiloto.stationadviser.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private EditText editTextNombre, editTextNit, editTextZona, editTextUbicacion, editTextLatitud, editTextLongitud;
    private Button buttonObtenerUbicacion, buttonRegistrar;
    private TextView textViewMensaje, textViewTitulo;
    private ProgressBar progressBar;

    private ApiService apiService;
    private TokenManager tokenManager;
    private FusedLocationProviderClient fusedLocationClient;

    private boolean modoEdicion = false;
    private Long estacionId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_estacion);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        editTextNombre = findViewById(R.id.editTextNombre);
        editTextNit = findViewById(R.id.editTextNit);
        editTextZona = findViewById(R.id.editTextZona);
        editTextUbicacion = findViewById(R.id.editTextUbicacion);
        editTextLatitud = findViewById(R.id.editTextLatitud);
        editTextLongitud = findViewById(R.id.editTextLongitud);
        buttonObtenerUbicacion = findViewById(R.id.buttonObtenerUbicacion);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);
        textViewMensaje = findViewById(R.id.textViewMensaje);
        textViewTitulo = findViewById(R.id.textViewTitulo);
        progressBar = findViewById(R.id.progressBar);

        buttonObtenerUbicacion.setOnClickListener(v -> obtenerUbicacionActual());

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("modo_edicion", false)) {
            modoEdicion = true;
            estacionId = extras.getLong("estacion_id", -1L);
            textViewTitulo.setText("Editar Estación");
            buttonRegistrar.setText("Actualizar Estación");
            editTextNombre.setText(extras.getString("estacion_nombre"));
            editTextNit.setText(extras.getString("estacion_nit"));
            editTextUbicacion.setText(extras.getString("estacion_ubicacion"));
            if (extras.containsKey("estacion_latitud")) {
                editTextLatitud.setText(String.valueOf(extras.getDouble("estacion_latitud")));
                editTextLongitud.setText(String.valueOf(extras.getDouble("estacion_longitud")));
            }
        }

        buttonRegistrar.setOnClickListener(v -> {
            if (modoEdicion) {
                actualizarEstacion();
            } else {
                registrarEstacion();
            }
        });
    }

    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            editTextLatitud.setText(String.valueOf(location.getLatitude()));
                            editTextLongitud.setText(String.valueOf(location.getLongitude()));
                            Toast.makeText(RegistrarEstacionActivity.this,
                                    "Ubicación obtenida", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegistrarEstacionActivity.this,
                                    "Activa el GPS y vuelve a intentar", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registrarEstacion() {
        String nombre = editTextNombre.getText().toString().trim();
        String nit = editTextNit.getText().toString().trim();
        String zona = editTextZona.getText().toString().trim();
        String ubicacion = editTextUbicacion.getText().toString().trim();
        String latitudStr = editTextLatitud.getText().toString().trim();
        String longitudStr = editTextLongitud.getText().toString().trim();

        if (nombre.isEmpty() || nit.isEmpty() || zona.isEmpty() || ubicacion.isEmpty()) {
            textViewMensaje.setText("Todos los campos son obligatorios");
            return;
        }

        double latitud = 0, longitud = 0;
        if (!latitudStr.isEmpty() && !longitudStr.isEmpty()) {
            latitud = Double.parseDouble(latitudStr);
            longitud = Double.parseDouble(longitudStr);
        }

        mostrarLoading(true);

        EstacionRequest request = new EstacionRequest(nombre, nit, ubicacion, latitud, longitud);
        request.setZona(zona);  // ← IMPORTANTE: asignar zona

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
                    String error = "Error: ";
                    try {
                        if (response.errorBody() != null) {
                            error += response.errorBody().string();
                        } else {
                            error += "El NIT ya existe o hubo un problema";
                        }
                    } catch (Exception e) {
                        error += "Error desconocido";
                    }
                    textViewMensaje.setText(error);
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
        String zona = editTextZona.getText().toString().trim();
        String ubicacion = editTextUbicacion.getText().toString().trim();
        String latitudStr = editTextLatitud.getText().toString().trim();
        String longitudStr = editTextLongitud.getText().toString().trim();

        if (nombre.isEmpty() || nit.isEmpty() || zona.isEmpty() || ubicacion.isEmpty()) {
            textViewMensaje.setText("Todos los campos son obligatorios");
            return;
        }

        double latitud = 0, longitud = 0;
        if (!latitudStr.isEmpty() && !longitudStr.isEmpty()) {
            latitud = Double.parseDouble(latitudStr);
            longitud = Double.parseDouble(longitudStr);
        }

        mostrarLoading(true);

        EstacionRequest request = new EstacionRequest(nombre, nit, ubicacion, latitud, longitud);
        request.setZona(zona);

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
        editTextZona.setText("");
        editTextUbicacion.setText("");
        editTextLatitud.setText("");
        editTextLongitud.setText("");
        textViewMensaje.setText("");
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        buttonRegistrar.setEnabled(!mostrar);
    }
}