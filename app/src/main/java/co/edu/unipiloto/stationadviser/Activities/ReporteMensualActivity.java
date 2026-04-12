package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.VentaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReporteMensualActivity extends AppCompatActivity {

    private TextView textReporte;
    private ProgressBar progressBar;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte_mensual);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        textReporte = findViewById(R.id.textReporte);
        progressBar = findViewById(R.id.progressBar);

        cargarReporte();
    }

    private void cargarReporte() {
        mostrarLoading(true);

        Call<List<VentaResponse>> call = apiService.getHistorialVentas();
        call.enqueue(new Callback<List<VentaResponse>>() {
            @Override
            public void onResponse(Call<List<VentaResponse>> call, Response<List<VentaResponse>> response) {
                mostrarLoading(false);
                double total = 0;
                if (response.isSuccessful() && response.body() != null) {
                    for (VentaResponse v : response.body()) {
                        total += v.getMontoTotal();
                    }
                }
                textReporte.setText("Total ventas: $" + total);
            }

            @Override
            public void onFailure(Call<List<VentaResponse>> call, Throwable t) {
                mostrarLoading(false);
                textReporte.setText("Error al cargar reporte: " + t.getMessage());
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}