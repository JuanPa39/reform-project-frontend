package co.edu.unipiloto.stationadviser.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.EstacionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaEstacionesActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> datos;
    private List<EstacionResponse> estacionesList;
    private ApiService apiService;
    private TokenManager tokenManager;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_estaciones);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        listView = findViewById(R.id.listViewEstaciones);
        progressBar = findViewById(R.id.progressBar);
        datos = new ArrayList<>();
        estacionesList = new ArrayList<>();

        cargarEstaciones();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            EstacionResponse estacion = estacionesList.get(position);
            Intent intent = new Intent(ListaEstacionesActivity.this, RegistrarEstacionActivity.class);
            intent.putExtra("modo_edicion", true);
            intent.putExtra("estacion_id", estacion.getId());
            intent.putExtra("estacion_nombre", estacion.getNombre());
            intent.putExtra("estacion_nit", estacion.getNit());
            intent.putExtra("estacion_ubicacion", estacion.getUbicacion());
            if (estacion.getLatitud() != 0 || estacion.getLongitud() != 0) {
                intent.putExtra("estacion_latitud", estacion.getLatitud());
                intent.putExtra("estacion_longitud", estacion.getLongitud());
            }
            startActivity(intent);
        });
    }

    private void cargarEstaciones() {
        mostrarLoading(true);

        Call<List<EstacionResponse>> call = apiService.getEstaciones();
        call.enqueue(new Callback<List<EstacionResponse>>() {
            @Override
            public void onResponse(Call<List<EstacionResponse>> call, Response<List<EstacionResponse>> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    estacionesList = response.body();
                    datos.clear();

                    if (estacionesList.isEmpty()) {
                        datos.add("No hay estaciones registradas.");
                    } else {
                        for (EstacionResponse e : estacionesList) {
                            String coordenadas = "";
                            if (e.getLatitud() != 0 || e.getLongitud() != 0) {
                                coordenadas = "\n📍 GPS: " + e.getLatitud() + ", " + e.getLongitud();
                            }
                            datos.add("Nombre: " + e.getNombre() + "\nNIT: " + e.getNit() +
                                    "\nUbicación: " + e.getUbicacion() + coordenadas + "\n(Toca para editar)");
                        }
                    }

                    adapter = new ArrayAdapter<>(ListaEstacionesActivity.this,
                            R.layout.item_estacion_fila, R.id.textEstacionItem, datos);
                    listView.setAdapter(adapter);
                } else {
                    Toast.makeText(ListaEstacionesActivity.this, "Error al cargar estaciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EstacionResponse>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(ListaEstacionesActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarEstaciones();
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}