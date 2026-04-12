package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.EstacionResponse;
import co.edu.unipiloto.stationadviser.network.models.NotificacionRequest;
import co.edu.unipiloto.stationadviser.network.models.NotificacionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificacionInconsistenciasActivity extends AppCompatActivity {

    private Spinner spinnerEstacion;
    private EditText etInconsistencia;
    private Button btnEnviar;
    private LinearLayout contenedorHistorial;
    private ProgressBar progressBar;

    private ApiService apiService;
    private TokenManager tokenManager;
    private List<EstacionResponse> listaEstaciones;
    private Long estacionSeleccionadaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacion_inconsistencias);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        spinnerEstacion = findViewById(R.id.spinnerEstacion);
        etInconsistencia = findViewById(R.id.etInconsistencia);
        btnEnviar = findViewById(R.id.btnEnviar);
        contenedorHistorial = findViewById(R.id.contenedorHistorial);
        progressBar = findViewById(R.id.progressBar);

        cargarEstacionesEnSpinner();
        cargarHistorial();

        btnEnviar.setOnClickListener(v -> enviarNotificacion());
    }

    private void cargarEstacionesEnSpinner() {
        Call<List<EstacionResponse>> call = apiService.getEstaciones();
        call.enqueue(new Callback<List<EstacionResponse>>() {
            @Override
            public void onResponse(Call<List<EstacionResponse>> call, Response<List<EstacionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaEstaciones = response.body();
                    List<String> nombresEstaciones = new ArrayList<>();

                    for (EstacionResponse e : listaEstaciones) {
                        nombresEstaciones.add(e.getNombre());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(NotificacionInconsistenciasActivity.this,
                            android.R.layout.simple_spinner_item, nombresEstaciones);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerEstacion.setAdapter(adapter);

                    spinnerEstacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            estacionSeleccionadaId = listaEstaciones.get(position).getId();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<List<EstacionResponse>> call, Throwable t) {
                Toast.makeText(NotificacionInconsistenciasActivity.this, "Error al cargar estaciones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarNotificacion() {
        String inconsistencia = etInconsistencia.getText().toString().trim();

        if (estacionSeleccionadaId == null) {
            Toast.makeText(this, "Selecciona una estación", Toast.LENGTH_SHORT).show();
            return;
        }
        if (inconsistencia.isEmpty()) {
            Toast.makeText(this, "Describe la inconsistencia", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarLoading(true);

        NotificacionRequest request = new NotificacionRequest(estacionSeleccionadaId, inconsistencia);
        Call<NotificacionResponse> call = apiService.enviarNotificacion(request);  // ← Cambiado de Void a NotificacionResponse
        call.enqueue(new Callback<NotificacionResponse>() {
            @Override
            public void onResponse(Call<NotificacionResponse> call, Response<NotificacionResponse> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(NotificacionInconsistenciasActivity.this,
                            "✅ Notificación enviada: " + response.body().getEstado(), Toast.LENGTH_SHORT).show();
                    etInconsistencia.setText("");
                    cargarHistorial();
                } else {
                    String error = response.code() + ": " + response.message();
                    Toast.makeText(NotificacionInconsistenciasActivity.this,
                            "Error al enviar: " + error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NotificacionResponse> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(NotificacionInconsistenciasActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarHistorial() {
        contenedorHistorial.removeAllViews();

        Call<List<NotificacionResponse>> call = apiService.getNotificaciones();
        call.enqueue(new Callback<List<NotificacionResponse>>() {
            @Override
            public void onResponse(Call<List<NotificacionResponse>> call, Response<List<NotificacionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NotificacionResponse> notificaciones = response.body();

                    if (notificaciones.isEmpty()) {
                        TextView tvVacio = new TextView(NotificacionInconsistenciasActivity.this);
                        tvVacio.setText("No hay notificaciones registradas.");
                        tvVacio.setTextColor(0xFFAAAAAA);
                        tvVacio.setPadding(0, 8, 0, 8);
                        contenedorHistorial.addView(tvVacio);
                        return;
                    }

                    for (NotificacionResponse n : notificaciones) {
                        CardView card = crearCardNotificacion(n);
                        contenedorHistorial.addView(card);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<NotificacionResponse>> call, Throwable t) {
                TextView tvError = new TextView(NotificacionInconsistenciasActivity.this);
                tvError.setText("Error al cargar historial");
                tvError.setTextColor(0xFFEF5350);
                contenedorHistorial.addView(tvError);
            }
        });
    }

    private CardView crearCardNotificacion(NotificacionResponse n) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 12);
        card.setLayoutParams(params);
        card.setCardBackgroundColor(0xFF1A2D42);
        card.setRadius(16f);
        card.setCardElevation(4f);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(28, 20, 28, 20);

        TextView tvEstacion = new TextView(this);
        tvEstacion.setText("Estación: " + n.getEstacionNombre());
        tvEstacion.setTextColor(0xFFFFFFFF);
        tvEstacion.setTextSize(14f);

        TextView tvInconsistencia = new TextView(this);
        tvInconsistencia.setText(n.getMensaje());
        tvInconsistencia.setTextColor(0xFFAAAAAA);
        tvInconsistencia.setTextSize(13f);
        tvInconsistencia.setPadding(0, 6, 0, 6);

        TextView tvEstado = new TextView(this);
        tvEstado.setText("● " + n.getEstado());
        tvEstado.setTextColor(0xFF4CAF50);
        tvEstado.setTextSize(12f);

        TextView tvFecha = new TextView(this);
        tvFecha.setText(n.getFecha());
        tvFecha.setTextColor(0xFF2196F3);
        tvFecha.setTextSize(11f);

        layout.addView(tvEstacion);
        layout.addView(tvInconsistencia);
        layout.addView(tvEstado);
        layout.addView(tvFecha);
        card.addView(layout);
        return card;
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? android.view.View.VISIBLE : android.view.View.GONE);
        btnEnviar.setEnabled(!mostrar);
    }
}