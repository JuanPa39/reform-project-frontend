package co.edu.unipiloto.stationadviser.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import co.edu.unipiloto.stationadviser.R;

public class MapaRutaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng puntoInicio;
    private LatLng puntoDestino;
    private Marker markerInicio;
    private Marker markerDestino;
    private LatLng pendingMarker; // almacena temporalmente el punto antes de elegir rol
    private TextView tvEstado;
    private LatLng pendingDestino;
    private String pendingDestinoNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_ruta);
        double destLat = getIntent().getDoubleExtra("destino_lat", 0);
        double destLon = getIntent().getDoubleExtra("destino_lon", 0);
        String destNombre = getIntent().getStringExtra("destino_nombre");
        if (destLat != 0 && destLon != 0) {
            LatLng destino = new LatLng(destLat, destLon);
            // Necesitamos esperar a que el mapa esté listo
            if (mMap != null) {
                setPuntoDestino(destino);
                Toast.makeText(this, "Destino: " + destNombre, Toast.LENGTH_SHORT).show();
            } else {
                // Guardar en variable temporal
                pendingDestino = destino;
                pendingDestinoNombre = destNombre;
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        tvEstado = findViewById(R.id.tvEstado);

        Button btnMyLocation = findViewById(R.id.btnMyLocation);
        Button btnClearMarkers = findViewById(R.id.btnClearMarkers);
        Button btnPlanRoute = findViewById(R.id.btnPlanRoute);
        Button btnSetStart = findViewById(R.id.btnSetStart);
        Button btnSetDest = findViewById(R.id.btnSetDest);

        btnMyLocation.setOnClickListener(v -> obtenerUbicacionActual());
        btnClearMarkers.setOnClickListener(v -> limpiarTodo());
        btnPlanRoute.setOnClickListener(v -> planificarRuta());
        btnSetStart.setOnClickListener(v -> {
            if (pendingMarker != null) {
                setPuntoInicio(pendingMarker);
                pendingMarker = null;
                tvEstado.setText("Inicio fijado. Toca largo para otro punto o selecciona estación como destino.");
            } else {
                Toast.makeText(this, "Primero toca largo en el mapa para añadir un punto", Toast.LENGTH_SHORT).show();
            }
        });
        btnSetDest.setOnClickListener(v -> {
            if (pendingMarker != null) {
                setPuntoDestino(pendingMarker);
                pendingMarker = null;
                tvEstado.setText("Destino fijado. Puedes añadir más puntos o planificar ruta.");
            } else {
                Toast.makeText(this, "Primero toca largo en el mapa para añadir un punto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapLongClickListener(latLng -> {
            pendingMarker = latLng;
            tvEstado.setText("Punto agregado: " + latLng.latitude + ", " + latLng.longitude + ". Elige INICIO o DESTINO.");
        });

        if (pendingDestino != null) {
            setPuntoDestino(pendingDestino);
            if (pendingDestinoNombre != null)
                Toast.makeText(this, "Destino: " + pendingDestinoNombre, Toast.LENGTH_SHORT).show();
            pendingDestino = null;
        }
    }
    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng miPos = new LatLng(location.getLatitude(), location.getLongitude());
                setPuntoInicio(miPos);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miPos, 15));
                tvEstado.setText("Inicio = tu ubicación actual. Toca largo para añadir destino.");
            } else {
                Toast.makeText(this, "No se pudo obtener ubicación. Activa GPS y reinicia app.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setPuntoInicio(LatLng latLng) {
        if (markerInicio != null) markerInicio.remove();
        markerInicio = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Inicio")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        puntoInicio = latLng;
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        Toast.makeText(this, "Punto de inicio fijado", Toast.LENGTH_SHORT).show();
    }

    private void setPuntoDestino(LatLng latLng) {
        if (markerDestino != null) markerDestino.remove();
        markerDestino = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Destino")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        puntoDestino = latLng;
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        Toast.makeText(this, "Destino fijado", Toast.LENGTH_SHORT).show();
    }

    private void limpiarTodo() {
        if (markerInicio != null) markerInicio.remove();
        if (markerDestino != null) markerDestino.remove();
        puntoInicio = null;
        puntoDestino = null;
        pendingMarker = null;
        mMap.clear();
        tvEstado.setText("Marcadores borrados. Toca largo en el mapa para comenzar.");
        Toast.makeText(this, "Todo limpiado", Toast.LENGTH_SHORT).show();
    }

    private void planificarRuta() {
        if (puntoInicio == null || puntoDestino == null) {
            Toast.makeText(this, "Fija primero el inicio y el destino", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String url = "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=" + puntoInicio.latitude + "," + puntoInicio.longitude +
                        "&destination=" + puntoDestino.latitude + "," + puntoDestino.longitude +
                        "&key=TU_API_KEY_DE_GOOGLE_MAPS";

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                String json = response.body().string();
                JSONObject obj = new JSONObject(json);
                JSONArray routes = obj.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String points = overviewPolyline.getString("points");
                    List<LatLng> decodedPath = PolyUtil.decode(points);
                    runOnUiThread(() -> {
                        mMap.addPolyline(new PolylineOptions().addAll(decodedPath).width(12).color(0xFF2196F3));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(puntoInicio, 12));
                        tvEstado.setText("Ruta trazada.");
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MapaRutaActivity.this, "No se encontró ruta", Toast.LENGTH_SHORT).show();
                        tvEstado.setText("No hay ruta disponible entre esos puntos.");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MapaRutaActivity.this, "Error al planificar ruta", Toast.LENGTH_SHORT).show();
                    tvEstado.setText("Error de conexión con Directions API.");
                });
            }
        }).start();
    }

    // Método público para que otras activities puedan establecer destino (ej: al seleccionar estación)
    public void setDestinoExterno(LatLng latLng, String nombre) {
        if (mMap != null) {
            setPuntoDestino(latLng);
            if (nombre != null) {
                Toast.makeText(this, "Destino: " + nombre, Toast.LENGTH_SHORT).show();
            }
        } else {
            // guardar para cuando el mapa esté listo (podría hacerse con una variable)
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual();
        } else {
            Toast.makeText(this, "Permiso de ubicación necesario para usar 'Mi ubicación'", Toast.LENGTH_LONG).show();
        }
    }
}