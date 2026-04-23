package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.AsignarEstacionRequest;
import co.edu.unipiloto.stationadviser.network.models.EstacionResponse;
import co.edu.unipiloto.stationadviser.network.models.UsuarioResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class AsignarEstacionActivity extends AppCompatActivity {

    private static final String TAG = "AsignarEstacion";
    private Spinner spinnerEmpleados, spinnerEstaciones;
    private Button btnAsignar;
    private ProgressBar progressBar;
    private TokenManager tokenManager;
    private ApiService apiService;

    private List<UsuarioResponse> listaEmpleados = new ArrayList<>();
    private List<EstacionResponse> listaEstaciones = new ArrayList<>();
    private Long empleadoIdSeleccionado;
    private Long estacionIdSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar_estacion);

        tokenManager = new TokenManager(this);

        String token = tokenManager.getToken();
        Log.d(TAG, "Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "NULL"));

        apiService = ApiClient.getClientWithToken(token).create(ApiService.class);

        spinnerEmpleados = findViewById(R.id.spinnerEmpleados);
        spinnerEstaciones = findViewById(R.id.spinnerEstaciones);
        btnAsignar = findViewById(R.id.btnAsignar);
        progressBar = findViewById(R.id.progressBar);

        cargarEmpleados();
        cargarEstaciones();

        spinnerEmpleados.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!listaEmpleados.isEmpty() && position < listaEmpleados.size()) {
                    empleadoIdSeleccionado = listaEmpleados.get(position).getId();
                    Log.d(TAG, "Empleado seleccionado ID: " + empleadoIdSeleccionado);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerEstaciones.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!listaEstaciones.isEmpty() && position < listaEstaciones.size()) {
                    estacionIdSeleccionada = listaEstaciones.get(position).getId();
                    Log.d(TAG, "Estación seleccionada ID: " + estacionIdSeleccionada);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAsignar.setOnClickListener(v -> asignarEstacion());
    }

    private void cargarEmpleados() {
        Log.d(TAG, "Cargando empleados...");
        apiService.getUsuarios().enqueue(new Callback<List<UsuarioResponse>>() {
            @Override
            public void onResponse(Call<List<UsuarioResponse>> call, Response<List<UsuarioResponse>> response) {
                Log.d(TAG, "Respuesta empleados - Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    listaEmpleados.clear();
                    for (UsuarioResponse u : response.body()) {
                        Log.d(TAG, "Usuario: " + u.getNombre() + " - Rol: " + u.getRol());
                        if (u.getRol() != null && u.getRol().toLowerCase().contains("empleado")) {
                            listaEmpleados.add(u);
                        }
                    }

                    Log.d(TAG, "Total empleados filtrados: " + listaEmpleados.size());

                    List<String> nombres = new ArrayList<>();
                    for (UsuarioResponse u : listaEmpleados) {
                        nombres.add(u.getNombre() + " (" + u.getEmail() + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AsignarEstacionActivity.this,
                            android.R.layout.simple_spinner_item,
                            nombres);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerEmpleados.setAdapter(adapter);

                    if (listaEmpleados.isEmpty()) {
                        Toast.makeText(AsignarEstacionActivity.this,
                                "No hay empleados disponibles", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Error respuesta empleados: " + response.code());
                    Toast.makeText(AsignarEstacionActivity.this,
                            "Error al cargar empleados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioResponse>> call, Throwable t) {
                Log.e(TAG, "Fallo cargando empleados: " + t.getMessage(), t);
                Toast.makeText(AsignarEstacionActivity.this,
                        "Error de conexión al cargar empleados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarEstaciones() {
        Log.d(TAG, "Cargando estaciones...");
        apiService.getEstaciones().enqueue(new Callback<List<EstacionResponse>>() {
            @Override
            public void onResponse(Call<List<EstacionResponse>> call, Response<List<EstacionResponse>> response) {
                Log.d(TAG, "Respuesta estaciones - Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    listaEstaciones = response.body();
                    Log.d(TAG, "Total estaciones: " + listaEstaciones.size());

                    List<String> nombres = new ArrayList<>();
                    for (EstacionResponse e : listaEstaciones) {
                        String zona = e.getZona() != null ? e.getZona() : "Sin zona";
                        nombres.add(e.getNombre() + " (" + zona + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AsignarEstacionActivity.this,
                            android.R.layout.simple_spinner_item,
                            nombres);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerEstaciones.setAdapter(adapter);
                } else {
                    Log.e(TAG, "Error respuesta estaciones: " + response.code());
                    Toast.makeText(AsignarEstacionActivity.this,
                            "Error al cargar estaciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EstacionResponse>> call, Throwable t) {
                Log.e(TAG, "Fallo cargando estaciones: " + t.getMessage(), t);
                Toast.makeText(AsignarEstacionActivity.this,
                        "Error de conexión al cargar estaciones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void asignarEstacion() {
        if (empleadoIdSeleccionado == null || estacionIdSeleccionada == null) {
            Toast.makeText(this, "Seleccione un empleado y una estación", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Asignando estación " + estacionIdSeleccionada + " a empleado " + empleadoIdSeleccionado);

        progressBar.setVisibility(View.VISIBLE);
        btnAsignar.setEnabled(false);

        AsignarEstacionRequest request = new AsignarEstacionRequest(estacionIdSeleccionada);
        apiService.asignarEstacion(empleadoIdSeleccionado, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                btnAsignar.setEnabled(true);

                Log.d(TAG, "Respuesta asignación - Código: " + response.code());

                if (response.isSuccessful()) {
                    Toast.makeText(AsignarEstacionActivity.this,
                            "✅ Estación asignada correctamente", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Log.e(TAG, "Error asignación: " + response.code());
                    Toast.makeText(AsignarEstacionActivity.this,
                            "❌ Error al asignar estación (Código: " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnAsignar.setEnabled(true);
                Log.e(TAG, "Fallo asignación: " + t.getMessage(), t);
                Toast.makeText(AsignarEstacionActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}