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
import co.edu.unipiloto.stationadviser.network.models.AbastecimientoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaAbastecimientosActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;
    private Spinner spinnerEstado;
    private Button buttonFiltrar;

    private ApiService apiService;
    private TokenManager tokenManager;
    private List<AbastecimientoResponse> abastecimientosList;
    private ArrayAdapter<String> adapter;
    private List<String> itemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_abastecimientos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Solicitudes de Abastecimiento");

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        listView = findViewById(R.id.listViewAbastecimientos);
        progressBar = findViewById(R.id.progressBar);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        buttonFiltrar = findViewById(R.id.buttonFiltrar);

        abastecimientosList = new ArrayList<>();
        itemsList = new ArrayList<>();

        String[] estados = {"TODOS", "SOLICITADO", "EN_PROCESO", "COMPLETADO", "RECHAZADO"};
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, estados);
        spinnerEstado.setAdapter(estadoAdapter);

        buttonFiltrar.setOnClickListener(v -> cargarAbastecimientos());
        cargarAbastecimientos();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            AbastecimientoResponse item = abastecimientosList.get(position);
            mostrarDialogoAcciones(item);
        });
    }

    private void cargarAbastecimientos() {
        mostrarLoading(true);
        itemsList.clear();

        String estadoSeleccionado = spinnerEstado.getSelectedItem().toString();
        String estado = estadoSeleccionado.equals("TODOS") ? null : estadoSeleccionado;

        Call<List<AbastecimientoResponse>> call;
        if (estado != null) {
            call = apiService.getAbastecimientosPorEstado(estado);
        } else {
            call = apiService.getAbastecimientos();
        }

        call.enqueue(new Callback<List<AbastecimientoResponse>>() {
            @Override
            public void onResponse(Call<List<AbastecimientoResponse>> call, Response<List<AbastecimientoResponse>> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    abastecimientosList = response.body();
                    if (abastecimientosList.isEmpty()) {
                        itemsList.add("No hay solicitudes de abastecimiento");
                    } else {
                        for (AbastecimientoResponse a : abastecimientosList) {
                            String item = String.format("📦 Solicitud #%d\n🏪 Estación: %s\n⛽ Combustible: %s\n📊 Cantidad: %.0f galones\n📅 Fecha: %s\n📌 Estado: %s",
                                    a.getId(), a.getEstacionNombre(), a.getCombustibleNombre(),
                                    a.getCantidadGalones(), formatearFecha(a.getFecha()), a.getEstado());
                            itemsList.add(item);
                        }
                    }
                    adapter = new ArrayAdapter<>(ListaAbastecimientosActivity.this,
                            android.R.layout.simple_list_item_1, itemsList);
                    listView.setAdapter(adapter);
                } else {
                    Toast.makeText(ListaAbastecimientosActivity.this, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AbastecimientoResponse>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(ListaAbastecimientosActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoAcciones(AbastecimientoResponse item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Solicitud #" + item.getId());

        String[] opciones;
        if ("SOLICITADO".equals(item.getEstado())) {
            opciones = new String[]{"✅ Aprobar", "❌ Rechazar"};
        } else if ("EN_PROCESO".equals(item.getEstado())) {
            opciones = new String[]{"📦 Marcar como Completado"};
        } else {
            opciones = new String[]{"Ver detalles"};
        }

        builder.setItems(opciones, (dialog, which) -> {
            if (opciones[which].contains("Aprobar")) {
                actualizarEstado(item.getId(), "aprobar");
            } else if (opciones[which].contains("Rechazar")) {
                actualizarEstado(item.getId(), "rechazar");
            } else if (opciones[which].contains("Completado")) {
                actualizarEstado(item.getId(), "completar");
            } else {
                mostrarDetalles(item);
            }
        });
        builder.show();
    }

    private void actualizarEstado(Long id, String accion) {
        mostrarLoading(true);

        Call<AbastecimientoResponse> call;
        if (accion.equals("aprobar")) {
            call = apiService.aprobarAbastecimiento(id);
        } else if (accion.equals("rechazar")) {
            call = apiService.rechazarAbastecimiento(id);
        } else {
            call = apiService.completarAbastecimiento(id);
        }

        call.enqueue(new Callback<AbastecimientoResponse>() {
            @Override
            public void onResponse(Call<AbastecimientoResponse> call, Response<AbastecimientoResponse> response) {
                mostrarLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ListaAbastecimientosActivity.this, "Estado actualizado", Toast.LENGTH_SHORT).show();
                    cargarAbastecimientos();
                } else {
                    Toast.makeText(ListaAbastecimientosActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AbastecimientoResponse> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(ListaAbastecimientosActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDetalles(AbastecimientoResponse item) {
        String detalles = String.format(
                "📋 DETALLES DE LA SOLICITUD\n\n" +
                        "ID: %d\n" +
                        "Estación: %s\n" +
                        "Combustible: %s\n" +
                        "Cantidad: %.0f galones\n" +
                        "Fecha solicitud: %s\n" +
                        "Estado: %s",
                item.getId(), item.getEstacionNombre(), item.getCombustibleNombre(),
                item.getCantidadGalones(), item.getFecha(), item.getEstado());

        new AlertDialog.Builder(this)
                .setTitle("Detalles")
                .setMessage(detalles)
                .setPositiveButton("OK", null)
                .show();
    }

    private String formatearFecha(String fecha) {
        if (fecha == null) return "N/A";
        try {
            String[] partes = fecha.split("T");
            return partes[0] + " " + (partes.length > 1 ? partes[1].substring(0, 8) : "");
        } catch (Exception e) {
            return fecha;
        }
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