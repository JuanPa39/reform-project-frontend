package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.List;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.NotificacionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConsultarNotificacionActivity extends AppCompatActivity {

    private LinearLayout contenedorNotificaciones;
    private ProgressBar progressBar;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_notificaciones);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        contenedorNotificaciones = findViewById(R.id.contenedorNotificaciones);
        progressBar = findViewById(R.id.progressBar);

        cargarNotificaciones();
    }

    private void cargarNotificaciones() {
        mostrarLoading(true);
        contenedorNotificaciones.removeAllViews();

        Call<List<NotificacionResponse>> call = apiService.getNotificaciones();
        call.enqueue(new Callback<List<NotificacionResponse>>() {
            @Override
            public void onResponse(Call<List<NotificacionResponse>> call, Response<List<NotificacionResponse>> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<NotificacionResponse> notificaciones = response.body();

                    if (notificaciones.isEmpty()) {
                        TextView tvVacio = new TextView(ConsultarNotificacionActivity.this);
                        tvVacio.setText("No hay notificaciones.");
                        tvVacio.setTextColor(0xFFAAAAAA);
                        tvVacio.setPadding(0, 8, 0, 8);
                        contenedorNotificaciones.addView(tvVacio);
                        return;
                    }

                    for (NotificacionResponse n : notificaciones) {
                        CardView card = new CardView(ConsultarNotificacionActivity.this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 12);
                        card.setLayoutParams(params);
                        card.setCardBackgroundColor(0xFF1A2D42);
                        card.setRadius(16f);
                        card.setCardElevation(4f);

                        LinearLayout layout = new LinearLayout(ConsultarNotificacionActivity.this);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(28, 20, 28, 20);

                        TextView tvEstacion = new TextView(ConsultarNotificacionActivity.this);
                        tvEstacion.setText("Estación: " + n.getEstacionNombre());
                        tvEstacion.setTextColor(0xFFFFFFFF);
                        tvEstacion.setTextSize(14f);

                        TextView tvInconsistencia = new TextView(ConsultarNotificacionActivity.this);
                        tvInconsistencia.setText(n.getMensaje());
                        tvInconsistencia.setTextColor(0xFFAAAAAA);
                        tvInconsistencia.setTextSize(13f);
                        tvInconsistencia.setPadding(0, 6, 0, 6);

                        TextView tvEstado = new TextView(ConsultarNotificacionActivity.this);
                        tvEstado.setText("● " + n.getEstado());
                        tvEstado.setTextColor(0xFF4CAF50);
                        tvEstado.setTextSize(12f);

                        TextView tvFecha = new TextView(ConsultarNotificacionActivity.this);
                        tvFecha.setText(n.getFecha());
                        tvFecha.setTextColor(0xFF2196F3);
                        tvFecha.setTextSize(11f);

                        layout.addView(tvEstacion);
                        layout.addView(tvInconsistencia);
                        layout.addView(tvEstado);
                        layout.addView(tvFecha);
                        card.addView(layout);
                        contenedorNotificaciones.addView(card);
                    }
                } else {
                    TextView tvError = new TextView(ConsultarNotificacionActivity.this);
                    tvError.setText("Error al cargar notificaciones");
                    tvError.setTextColor(0xFFEF5350);
                    contenedorNotificaciones.addView(tvError);
                }
            }

            @Override
            public void onFailure(Call<List<NotificacionResponse>> call, Throwable t) {
                mostrarLoading(false);
                TextView tvError = new TextView(ConsultarNotificacionActivity.this);
                tvError.setText("Error de conexión: " + t.getMessage());
                tvError.setTextColor(0xFFEF5350);
                contenedorNotificaciones.addView(tvError);
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}