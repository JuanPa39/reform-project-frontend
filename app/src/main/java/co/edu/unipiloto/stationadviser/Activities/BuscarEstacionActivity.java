package co.edu.unipiloto.stationadviser.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
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
import java.util.Collections;
import android.location.Address;
import android.location.Geocoder;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;

public class BuscarEstacionActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 44;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvVacio;
    private ApiService apiService;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<EstacionResponse> estaciones = new ArrayList<>();
    private EstacionAdapter adapter;

    private double miLatitud = 0;
    private double miLongitud = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_estacion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Estaciones Cercanas");
        }

        recyclerView = findViewById(R.id.recyclerView);
        progressBar  = findViewById(R.id.progressBar);
        tvVacio      = findViewById(R.id.tvVacio);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EstacionAdapter(estaciones);
        recyclerView.setAdapter(adapter);

        TokenManager tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken())
                .create(ApiService.class);

        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        solicitarUbicacionYCargar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void solicitarUbicacionYCargar() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        progressBar.setVisibility(View.VISIBLE);

        com.google.android.gms.location.LocationRequest locationRequest =
                com.google.android.gms.location.LocationRequest.create()
                        .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setNumUpdates(1)
                        .setInterval(1000);

        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(
                            @NonNull com.google.android.gms.location.LocationResult result) {
                        fusedLocationProviderClient.removeLocationUpdates(this);
                        progressBar.setVisibility(View.GONE);
                        double latDetectada = result.getLastLocation() != null
                                ? result.getLastLocation().getLatitude() : 4.7110;
                        double lonDetectada = result.getLastLocation() != null
                                ? result.getLastLocation().getLongitude() : -74.0721;
                        mostrarDialogoUbicacion(latDetectada, lonDetectada);
                    }
                },
                getMainLooper()
        );
    }

    private void mostrarDialogoUbicacion(double latDetectada, double lonDetectada) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        android.widget.EditText etLatitud = new android.widget.EditText(this);
        etLatitud.setHint("Latitud");
        etLatitud.setText(latDetectada != 0 ? String.valueOf(latDetectada) : "");
        etLatitud.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        layout.addView(etLatitud);

        android.widget.EditText etLongitud = new android.widget.EditText(this);
        etLongitud.setHint("Longitud");
        etLongitud.setText(lonDetectada != 0 ? String.valueOf(lonDetectada) : "");
        etLongitud.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        layout.addView(etLongitud);

        new android.app.AlertDialog.Builder(this)
                .setTitle("📍 Confirma tu ubicación")
                .setMessage("Tu ubicación detectada. Corrígela si es necesario:")
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("Buscar estaciones", (dialog, which) -> {
                    try {
                        miLatitud  = Double.parseDouble(etLatitud.getText().toString().trim());
                        miLongitud = Double.parseDouble(etLongitud.getText().toString().trim());
                        cargarEstaciones();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Ver todas sin distancia", (dialog, which) -> {
                    cargarEstaciones();
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        } else {
            Toast.makeText(this,
                    "Permiso denegado, mostrando todas las estaciones",
                    Toast.LENGTH_SHORT).show();
            cargarEstaciones();
        }
    }

    private void cargarEstaciones() {
        apiService.getEstaciones().enqueue(new Callback<List<EstacionResponse>>() {
            @Override
            public void onResponse(Call<List<EstacionResponse>> call,
                                   Response<List<EstacionResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    estaciones.clear();
                    List<EstacionResponse> lista = response.body();

                    if (miLatitud != 0 && miLongitud != 0) {
                        for (EstacionResponse e : lista) {
                            if (e.getLatitud() != 0 && e.getLongitud() != 0) {
                                float[] resultado = new float[1];
                                android.location.Location.distanceBetween(
                                        miLatitud, miLongitud,
                                        e.getLatitud(), e.getLongitud(),
                                        resultado);
                                e.setDistanciaKm(resultado[0] / 1000f);
                            }
                        }
                        Collections.sort(lista, (a, b) -> Float.compare(
                                a.getDistanciaKm() != null ? a.getDistanciaKm() : Float.MAX_VALUE,
                                b.getDistanciaKm() != null ? b.getDistanciaKm() : Float.MAX_VALUE
                        ));
                    }

                    estaciones.addAll(lista);
                    adapter.notifyDataSetChanged();
                    tvVacio.setVisibility(estaciones.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(BuscarEstacionActivity.this,
                            "Error al cargar estaciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EstacionResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BuscarEstacionActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class EstacionAdapter extends RecyclerView.Adapter<EstacionAdapter.ViewHolder> {

        private final List<EstacionResponse> lista;

        EstacionAdapter(List<EstacionResponse> lista) { this.lista = lista; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_estacion, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            EstacionResponse e = lista.get(pos);
            h.tvNombre.setText(e.getNombre());
            h.tvUbicacion.setText("📍 " + (e.getUbicacion() != null ? e.getUbicacion() : ""));
            h.tvZona.setText("Zona: " + (e.getZona() != null ? e.getZona() : ""));
            h.tvActiva.setText(e.isActiva() ? "✅ Activa" : "❌ Inactiva");

            if (e.getDistanciaKm() != null) {
                h.tvDistancia.setText(String.format("🚗 %.2f km de distancia", e.getDistanciaKm()));
                h.tvDistancia.setVisibility(View.VISIBLE);
            } else {
                h.tvDistancia.setVisibility(View.GONE);
            }

            h.itemView.setOnClickListener(v -> {
                if (e.getLatitud() == 0 && e.getLongitud() == 0) {
                    Toast.makeText(v.getContext(),
                            "La estación " + e.getNombre() + " no tiene coordenadas GPS registradas",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(v.getContext(), MapaRutaActivity.class);
                intent.putExtra("destino_lat", e.getLatitud());
                intent.putExtra("destino_lon", e.getLongitud());
                intent.putExtra("destino_nombre", e.getNombre());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return lista.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvUbicacion, tvZona, tvDistancia, tvActiva;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre    = itemView.findViewById(R.id.tvNombre);
                tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
                tvZona      = itemView.findViewById(R.id.tvZona);
                tvDistancia = itemView.findViewById(R.id.tvDistancia);
                tvActiva    = itemView.findViewById(R.id.tvActiva);
            }
        }
    }
}