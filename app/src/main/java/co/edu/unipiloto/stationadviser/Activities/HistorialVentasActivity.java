package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.VentaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialVentasActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;
    private ApiService apiService;
    private TokenManager tokenManager;
    private ArrayAdapter<String> adapter;
    private List<String> historialList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_ventas);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        historialList = new ArrayList<>();

        cargarHistorial();
    }

    private void cargarHistorial() {
        mostrarLoading(true);

        Call<List<VentaResponse>> call = apiService.getHistorialVentas();
        call.enqueue(new Callback<List<VentaResponse>>() {
            @Override
            public void onResponse(Call<List<VentaResponse>> call, Response<List<VentaResponse>> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    historialList.clear();
                    for (VentaResponse v : response.body()) {
                        String ventaStr = v.getFecha() + " - " +
                                v.getEstacionNombre() + " - " +
                                v.getCombustibleNombre() + ": " +
                                v.getCantidad() + "L - $" + v.getMontoTotal();
                        historialList.add(ventaStr);
                    }

                    if (historialList.isEmpty()) {
                        historialList.add("No hay ventas registradas");
                    }

                    adapter = new ArrayAdapter<>(HistorialVentasActivity.this,
                            android.R.layout.simple_list_item_1,
                            historialList);
                    listView.setAdapter(adapter);
                } else {
                    Toast.makeText(HistorialVentasActivity.this, "Error al cargar historial", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VentaResponse>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(HistorialVentasActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarHistorial();
    }
}