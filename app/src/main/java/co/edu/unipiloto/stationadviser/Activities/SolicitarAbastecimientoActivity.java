package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.AbastecimientoRequest;
import co.edu.unipiloto.stationadviser.network.models.AbastecimientoResponse;
import co.edu.unipiloto.stationadviser.network.models.CombustibleResponse;
import co.edu.unipiloto.stationadviser.network.models.DistribuidorResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolicitarAbastecimientoActivity extends AppCompatActivity {

    private Spinner spinnerDistribuidor, spinnerCombustible;
    private EditText editCantidad;
    private Button buttonSolicitar;
    private ProgressBar progressBar;
    private TextView textViewMensaje;

    private ApiService apiService;
    private TokenManager tokenManager;
    private List<DistribuidorResponse> distribuidoresList;
    private List<CombustibleResponse> combustiblesList;
    private Long distribuidorSeleccionadoId;
    private Long combustibleSeleccionadoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitar_abastecimiento);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Solicitar Abastecimiento");

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        spinnerDistribuidor = findViewById(R.id.spinnerDistribuidor);
        spinnerCombustible = findViewById(R.id.spinnerCombustible);
        editCantidad = findViewById(R.id.editCantidad);
        buttonSolicitar = findViewById(R.id.buttonSolicitar);
        progressBar = findViewById(R.id.progressBar);
        textViewMensaje = findViewById(R.id.textViewMensaje);

        cargarDistribuidores();
        cargarCombustibles();

        buttonSolicitar.setOnClickListener(v -> solicitarAbastecimiento());
    }

    private void cargarDistribuidores() {
        mostrarLoading(true);
        Call<List<DistribuidorResponse>> call = apiService.getDistribuidores();
        call.enqueue(new Callback<List<DistribuidorResponse>>() {
            @Override
            public void onResponse(Call<List<DistribuidorResponse>> call, Response<List<DistribuidorResponse>> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    distribuidoresList = response.body();
                    List<String> nombres = new ArrayList<>();
                    for (DistribuidorResponse d : distribuidoresList) {
                        nombres.add(d.getNombre());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(SolicitarAbastecimientoActivity.this,
                            android.R.layout.simple_spinner_item, nombres);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDistribuidor.setAdapter(adapter);

                    spinnerDistribuidor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            distribuidorSeleccionadoId = distribuidoresList.get(position).getId();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                } else {
                    textViewMensaje.setText("No hay distribuidores disponibles");
                }
            }

            @Override
            public void onFailure(Call<List<DistribuidorResponse>> call, Throwable t) {
                mostrarLoading(false);
                textViewMensaje.setText("Error: " + t.getMessage());
            }
        });
    }

    private void cargarCombustibles() {
        Call<List<CombustibleResponse>> call = apiService.getCombustibles();
        call.enqueue(new Callback<List<CombustibleResponse>>() {
            @Override
            public void onResponse(Call<List<CombustibleResponse>> call, Response<List<CombustibleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    combustiblesList = response.body();
                    List<String> nombres = new ArrayList<>();
                    for (CombustibleResponse c : combustiblesList) {
                        nombres.add(c.getNombre());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(SolicitarAbastecimientoActivity.this,
                            android.R.layout.simple_spinner_item, nombres);
                    spinnerCombustible.setAdapter(adapter);

                    spinnerCombustible.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            combustibleSeleccionadoId = combustiblesList.get(position).getId();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<List<CombustibleResponse>> call, Throwable t) {
                Toast.makeText(SolicitarAbastecimientoActivity.this, "Error al cargar combustibles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void solicitarAbastecimiento() {
        String cantidadStr = editCantidad.getText().toString().trim();

        if (distribuidorSeleccionadoId == null) {
            textViewMensaje.setText("Seleccione un distribuidor");
            return;
        }
        if (combustibleSeleccionadoId == null) {
            textViewMensaje.setText("Seleccione un combustible");
            return;
        }
        if (cantidadStr.isEmpty()) {
            textViewMensaje.setText("Ingrese la cantidad de galones");
            return;
        }

        double cantidad = Double.parseDouble(cantidadStr);
        mostrarLoading(true);

        AbastecimientoRequest request = new AbastecimientoRequest(distribuidorSeleccionadoId, combustibleSeleccionadoId, cantidad);
        Call<AbastecimientoResponse> call = apiService.solicitarAbastecimiento(request);
        call.enqueue(new Callback<AbastecimientoResponse>() {
            @Override
            public void onResponse(Call<AbastecimientoResponse> call, Response<AbastecimientoResponse> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    new AlertDialog.Builder(SolicitarAbastecimientoActivity.this)
                            .setTitle("✅ Solicitud Enviada")
                            .setMessage("Su solicitud de abastecimiento ha sido enviada al distribuidor.\n\n" +
                                    "ID: " + response.body().getId() + "\n" +
                                    "Estado: " + response.body().getEstado())
                            .setPositiveButton("OK", (dialog, which) -> finish())
                            .show();
                } else {
                    textViewMensaje.setText("Error al enviar la solicitud");
                }
            }

            @Override
            public void onFailure(Call<AbastecimientoResponse> call, Throwable t) {
                mostrarLoading(false);
                textViewMensaje.setText("Error: " + t.getMessage());
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        buttonSolicitar.setEnabled(!mostrar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}