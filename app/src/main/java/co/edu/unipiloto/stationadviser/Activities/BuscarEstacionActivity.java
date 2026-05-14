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

    // ── 1. Pedir permiso GPS ──────────────────────────────────────────────────

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

    // ── 2. Obtener coordenadas GPS ────────────────────────────────────────────

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        progressBar.setVisibility(View.VISIBLE);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    progressBar.setVisibility(View.GONE);
                    double latDetectada  = location != null ? location.getLatitude()  : 0;
                    double lonDetectada  = location != null ? location.getLongitude() : 0;
                    mostrarDialogoUbicacion(latDetectada, lonDetectada);
                });
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
    // ── 3. Resultado del diálogo de permiso ───────────────────────────────────

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

    // ── 4. Cargar estaciones y calcular distancia ─────────────────────────────

    private void cargarEstaciones() {
        apiService.getEstaciones().enqueue(new Callback<List<EstacionResponse>>() {
            @Override
            public void onResponse(Call<List<EstacionResponse>> call,
                                   Response<List<EstacionResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    estaciones.clear();
                    List<EstacionResponse> lista = response.body();

                    // Calcular distancia si tenemos GPS
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
                        // Ordenar por distancia
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

    // ── Adapter ───────────────────────────────────────────────────────────────

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
            // ... resto de textos
            h.itemView.setOnClickListener(v -> {
                // Geocodificar la dirección de la estación
                String direccion = e.getUbicacion();
                if (direccion == null || direccion.isEmpty()) {
                    Toast.makeText(v.getContext(), "La estación no tiene dirección registrada", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Añadir "Colombia" para mejorar precisión
                direccion += ", Colombia";
                Geocoder geocoder = new Geocoder(v.getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocationName(direccion, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        LatLng coord = new LatLng(addr.getLatitude(), addr.getLongitude());
                        Intent intent = new Intent(v.getContext(), MapaRutaActivity.class);
                        intent.putExtra("destino_lat", coord.latitude);
                        intent.putExtra("destino_lon", coord.longitude);
                        intent.putExtra("destino_nombre", e.getNombre());
                        v.getContext().startActivity(intent);
                    } else {
                        Toast.makeText(v.getContext(), "No se pudo geolocalizar la dirección: " + direccion, Toast.LENGTH_LONG).show();
                    }
                } catch (IOException ex) {
                    Toast.makeText(v.getContext(), "Error de geocodificación: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
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