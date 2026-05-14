package co.edu.unipiloto.stationadviser.Activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.*;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.AbastecimientoResponse;
import co.edu.unipiloto.stationadviser.network.models.CombustibleResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialRecargasActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;
    private Spinner spinnerCombustible;
    private Button buttonFechaInicio, buttonFechaFin, buttonFiltrar;
    private TextView textFechaInicio, textFechaFin;

    private ApiService apiService;
    private TokenManager tokenManager;
    private ArrayAdapter<String> adapter;
    private List<String> historialList;
    private List<AbastecimientoResponse> recargasList;
    private List<CombustibleResponse> combustiblesList;

    private String fechaInicio = "";
    private String fechaFin = "";
    private Long combustibleSeleccionado = null;

    private final Calendar calendario = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_recargas);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Historial de Recargas");
        }

        tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        if (token != null && !token.isEmpty()) {
            apiService = ApiClient.getClientWithToken(token).create(ApiService.class);
        } else {
            apiService = ApiClient.getClient().create(ApiService.class);
        }

        listView = findViewById(R.id.listViewRecargas);
        progressBar = findViewById(R.id.progressBar);
        spinnerCombustible = findViewById(R.id.spinnerCombustible);
        buttonFechaInicio = findViewById(R.id.buttonFechaInicio);
        buttonFechaFin = findViewById(R.id.buttonFechaFin);
        buttonFiltrar = findViewById(R.id.buttonFiltrar);
        textFechaInicio = findViewById(R.id.textFechaInicio);
        textFechaFin = findViewById(R.id.textFechaFin);

        historialList = new ArrayList<>();
        recargasList = new ArrayList<>();
        combustiblesList = new ArrayList<>();

        cargarCombustibles();

        buttonFechaInicio.setOnClickListener(v -> mostrarDatePicker(true));
        buttonFechaFin.setOnClickListener(v -> mostrarDatePicker(false));
        buttonFiltrar.setOnClickListener(v -> cargarHistorial());

        cargarHistorial();
    }

    private void cargarCombustibles() {
        Call<List<CombustibleResponse>> call = apiService.getCombustibles();
        call.enqueue(new Callback<List<CombustibleResponse>>() {
            @Override
            public void onResponse(Call<List<CombustibleResponse>> call, Response<List<CombustibleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    combustiblesList = response.body();
                    List<String> nombres = new ArrayList<>();
                    nombres.add("Todos los combustibles");
                    for (CombustibleResponse c : combustiblesList) {
                        nombres.add(c.getNombre());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(HistorialRecargasActivity.this,
                            android.R.layout.simple_spinner_item, nombres);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCombustible.setAdapter(adapter);

                    spinnerCombustible.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                combustibleSeleccionado = null;
                            } else if (combustiblesList != null && position - 1 < combustiblesList.size()) {
                                combustibleSeleccionado = combustiblesList.get(position - 1).getId();
                            }
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                } else {
                    Toast.makeText(HistorialRecargasActivity.this, "Error al cargar combustibles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CombustibleResponse>> call, Throwable t) {
                Toast.makeText(HistorialRecargasActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDatePicker(boolean esInicio) {
        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, day) -> {
            String fecha = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", day);
            if (esInicio) {
                fechaInicio = fecha;
                textFechaInicio.setText("Inicio: " + day + "/" + (month + 1) + "/" + year);
            } else {
                fechaFin = fecha;
                textFechaFin.setText("Fin: " + day + "/" + (month + 1) + "/" + year);
            }
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void cargarHistorial() {
        mostrarLoading(true);
        historialList.clear();

        Call<List<AbastecimientoResponse>> call = apiService.getHistorialRecargas(fechaInicio, fechaFin, combustibleSeleccionado);
        call.enqueue(new Callback<List<AbastecimientoResponse>>() {
            @Override
            public void onResponse(Call<List<AbastecimientoResponse>> call, Response<List<AbastecimientoResponse>> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    recargasList = response.body();
                    if (recargasList.isEmpty()) {
                        historialList.add("No hay recargas registradas");
                    } else {
                        for (AbastecimientoResponse r : recargasList) {
                            String item = String.format("📅 %s\n⛽ %s\n📊 %.0f galones\n🏪 Distribuidor: %s",
                                    r.getFecha() != null ? r.getFecha() : "Fecha no disponible",
                                    r.getCombustibleNombre() != null ? r.getCombustibleNombre() : "N/A",
                                    r.getCantidadGalones() != null ? r.getCantidadGalones() : 0,
                                    r.getDistribuidorNombre() != null ? r.getDistribuidorNombre() : "N/A");
                            historialList.add(item);
                        }
                    }
                    adapter = new ArrayAdapter<>(HistorialRecargasActivity.this,
                            android.R.layout.simple_list_item_1, historialList);
                    listView.setAdapter(adapter);
                } else {
                    Toast.makeText(HistorialRecargasActivity.this, "Error al cargar historial", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AbastecimientoResponse>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(HistorialRecargasActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
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