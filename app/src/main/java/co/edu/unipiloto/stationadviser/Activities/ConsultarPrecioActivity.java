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
import co.edu.unipiloto.stationadviser.network.models.PrecioResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConsultarPrecioActivity extends AppCompatActivity {

    private ListView listViewPrecios;
    private ProgressBar progressBar;
    private ApiService apiService;
    private TokenManager tokenManager;
    private ArrayAdapter<String> adapter;
    private List<String> listaPrecios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_precio);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        listViewPrecios = findViewById(R.id.listViewPrecios);
        progressBar = findViewById(R.id.progressBar);
        listaPrecios = new ArrayList<>();

        cargarPrecios();
    }

    private void cargarPrecios() {
        mostrarLoading(true);

        Call<List<PrecioResponse>> call = apiService.getPrecios();
        call.enqueue(new Callback<List<PrecioResponse>>() {
            @Override
            public void onResponse(Call<List<PrecioResponse>> call, Response<List<PrecioResponse>> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    listaPrecios.clear();
                    for (PrecioResponse p : response.body()) {
                        String precioStr = p.getEstacionNombre() + " - " +
                                p.getCombustibleNombre() + ": $" +
                                p.getPrecio() + " (Vigente: " + p.getFecha() + ")";
                        listaPrecios.add(precioStr);
                    }

                    if (listaPrecios.isEmpty()) {
                        listaPrecios.add("No hay precios registrados");
                    }

                    adapter = new ArrayAdapter<>(ConsultarPrecioActivity.this,
                            R.layout.item_precio_fila,
                            R.id.textTipoCombustible,
                            listaPrecios);
                    listViewPrecios.setAdapter(adapter);
                } else {
                    Toast.makeText(ConsultarPrecioActivity.this, "Error al cargar precios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PrecioResponse>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(ConsultarPrecioActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPrecios();
    }
}