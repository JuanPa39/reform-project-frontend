package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.EstacionResponse;
import co.edu.unipiloto.stationadviser.network.models.PrecioRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarPrecioCombustibleActivity extends AppCompatActivity {

    private Spinner spinnerCombustible, spinnerEstaciones;
    private EditText editPrecio;
    private Button buttonGuardar;
    private ProgressBar progressBar;

    private ApiService apiService;
    private TokenManager tokenManager;
    private List<EstacionResponse> estaciones;
    private Long estacionSeleccionadaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_precio_combustible);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        spinnerCombustible = findViewById(R.id.spinnerCombustible);
        spinnerEstaciones = findViewById(R.id.spinnerEstaciones);
        editPrecio = findViewById(R.id.editPrecio);
        buttonGuardar = findViewById(R.id.buttonGuardarPrecio);
        progressBar = findViewById(R.id.progressBar);

        cargarTiposCombustible();
        cargarEstaciones();

        buttonGuardar.setOnClickListener(v -> guardarPrecio());
    }

    private void cargarTiposCombustible() {
        String[] tipos = {"ACPM", "Gasolina Corriente", "Gasolina Extra"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCombustible.setAdapter(adapter);
    }

    private void cargarEstaciones() {
        mostrarLoading(true);

        Call<List<EstacionResponse>> call = apiService.getEstaciones();
        call.enqueue(new Callback<List<EstacionResponse>>() {
            @Override
            public void onResponse(Call<List<EstacionResponse>> call, Response<List<EstacionResponse>> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    estaciones = response.body();
                    List<String> nombres = new ArrayList<>();
                    for (EstacionResponse e : estaciones) {
                        nombres.add(e.getNombre());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(RegistrarPrecioCombustibleActivity.this,
                            android.R.layout.simple_spinner_item, nombres);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerEstaciones.setAdapter(adapter);

                    // Establecer el listener solo si hay estaciones
                    if (!estaciones.isEmpty()) {
                        estacionSeleccionadaId = estaciones.get(0).getId();
                        spinnerEstaciones.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (estaciones != null && position < estaciones.size()) {
                                    estacionSeleccionadaId = estaciones.get(position).getId();
                                }
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });
                    }
                } else {
                    Toast.makeText(RegistrarPrecioCombustibleActivity.this, "Error al cargar estaciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EstacionResponse>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(RegistrarPrecioCombustibleActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarPrecio() {
        if (spinnerCombustible.getSelectedItem() == null) {
            Toast.makeText(this, "Seleccione un combustible", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipo = spinnerCombustible.getSelectedItem().toString();
        String precioTexto = editPrecio.getText().toString();

        if (precioTexto.isEmpty()) {
            Toast.makeText(this, "Ingrese el precio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (estacionSeleccionadaId == null) {
            Toast.makeText(this, "Seleccione una estación", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio = Double.parseDouble(precioTexto);
        mostrarLoading(true);

        PrecioRequest request = new PrecioRequest(estacionSeleccionadaId, tipo, precio);
        Call<Void> call = apiService.crearPrecio(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                mostrarLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(RegistrarPrecioCombustibleActivity.this, "Precio registrado", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegistrarPrecioCombustibleActivity.this, "Error al registrar precio", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(RegistrarPrecioCombustibleActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
        if (buttonGuardar != null) {
            buttonGuardar.setEnabled(!mostrar);
        }
    }
}