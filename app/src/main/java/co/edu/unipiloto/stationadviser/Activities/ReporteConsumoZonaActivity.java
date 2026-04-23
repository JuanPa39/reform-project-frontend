package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.ReporteZonaResponse;
import co.edu.unipiloto.stationadviser.network.models.DetalleEstacionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReporteConsumoZonaActivity extends AppCompatActivity {

    private Spinner spinnerZona;
    private Button buttonConsultar;
    private TextView textResultado;
    private ProgressBar progressBar;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte_consumo_zona);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        spinnerZona = findViewById(R.id.spinnerZona);
        buttonConsultar = findViewById(R.id.buttonConsultar);
        textResultado = findViewById(R.id.textResultado);
        progressBar = findViewById(R.id.progressBar);

        String[] zonas = {"Norte", "Sur", "Centro", "Oriente", "Occidente", "Todas"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, zonas);
        spinnerZona.setAdapter(adapter);

        buttonConsultar.setOnClickListener(v -> consultarReporte());
    }

    private void consultarReporte() {
        String zonaSeleccionada = spinnerZona.getSelectedItem().toString();

        // Usar fechas simples (solo año-mes-día)
        LocalDate hoy = LocalDate.now();
        LocalDate hace30Dias = hoy.minusDays(30);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaInicio = hace30Dias.format(dateFormatter);
        String fechaFin = hoy.format(dateFormatter);

        mostrarLoading(true);

        Call<List<ReporteZonaResponse>> call = apiService.getConsumoPorZona(fechaInicio, fechaFin);
        call.enqueue(new Callback<List<ReporteZonaResponse>>() {
            @Override
            public void onResponse(Call<List<ReporteZonaResponse>> call, Response<List<ReporteZonaResponse>> response) {
                mostrarLoading(false);

                if (!response.isSuccessful()) {
                    textResultado.setText("Error " + response.code() + ": " + response.message());
                    return;
                }

                if (response.body() == null || response.body().isEmpty()) {
                    textResultado.setText("No hay datos de consumo en el período seleccionado.\n\nPosibles causas:\n- No hay ventas registradas\n- Las estaciones no tienen zona asignada");
                    return;
                }

                List<ReporteZonaResponse> reportes = response.body();
                StringBuilder sb = new StringBuilder();
                boolean encontroDatos = false;

                for (ReporteZonaResponse r : reportes) {
                    if (zonaSeleccionada.equals("Todas") || r.getZona().equals(zonaSeleccionada)) {
                        encontroDatos = true;
                        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                        sb.append("📍 ZONA: ").append(r.getZona()).append("\n");
                        sb.append("📊 Total galones: ").append(String.format("%.2f", r.getTotalGalones())).append("\n");
                        sb.append("💰 Total ventas: $").append(String.format("%.2f", r.getTotalVentas())).append("\n");
                        sb.append("\n📋 Detalle por estación:\n");

                        for (DetalleEstacionResponse d : r.getDetalleEstaciones()) {
                            sb.append("   • ").append(d.getEstacionNombre()).append("\n");
                            sb.append("     Galones: ").append(String.format("%.2f", d.getGalonesVendidos())).append("\n");
                            sb.append("     Monto: $").append(String.format("%.2f", d.getMontoTotal())).append("\n");
                        }
                    }
                }

                if (!encontroDatos) {
                    textResultado.setText("No hay datos para la zona seleccionada: " + zonaSeleccionada);
                } else {
                    textResultado.setText(sb.toString());
                }
            }

            @Override
            public void onFailure(Call<List<ReporteZonaResponse>> call, Throwable t) {
                mostrarLoading(false);
                textResultado.setText("Error de conexión: " + t.getMessage() + "\n\nVerifica que el backend esté corriendo en http://10.0.2.2:8080");
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        buttonConsultar.setEnabled(!mostrar);
    }
}