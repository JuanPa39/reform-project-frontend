package co.edu.unipiloto.stationadviser.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private RadioGroup radioGroupModo;
    private RadioButton radioInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_ruta);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        Button btnMyLocation = findViewById(R.id.btnMyLocation);
        Button btnClearMarkers = findViewById(R.id.btnClearMarkers);
        Button btnPlanRoute = findViewById(R.id.btnPlanRoute);
        radioGroupModo = findViewById(R.id.radioGroupModo);
        radioInicio = findViewById(R.id.radioInicio);

        btnMyLocation.setOnClickListener(v -> obtenerUbicacionActual());
        btnClearMarkers.setOnClickListener(v -> limpiarMarcadores());
        btnPlanRoute.setOnClickListener(v -> planificarRuta());

        // Recibir datos de estación seleccionada desde otra activity
        double destLat = getIntent().getDoubleExtra("destino_lat", 0);
        double destLon = getIntent().getDoubleExtra("destino_lon", 0);
        String destNombre = getIntent().getStringExtra("destino_nombre");
        if (destLat != 0 && destLon != 0) {
            puntoDestino = new LatLng(destLat, destLon);
            // El marcador se pondrá cuando el mapa esté listo
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Si hay destino precargado, agregar marcador
        if (puntoDestino != null) {
            markerDestino = mMap.addMarker(new MarkerOptions().position(puntoDestino).title("Destino: " + getIntent().getStringExtra("destino_nombre")));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoDestino, 14));
        }

        // Configurar click largo para agregar marcadores según el modo seleccionado
        mMap.setOnMapLongClickListener(latLng -> {
            int selectedId = radioGroupModo.getCheckedRadioButtonId();
            if (selectedId == R.id.radioInicio) {
                setPuntoInicio(latLng);
            } else {
                setPuntoDestino(latLng);
            }
        });
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
            } else {
                Toast.makeText(this, "No se pudo obtener ubicación. Activa el GPS.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setPuntoInicio(LatLng latLng) {
        if (markerInicio != null) markerInicio.remove();
        markerInicio = mMap.addMarker(new MarkerOptions().position(latLng).title("Inicio"));
        puntoInicio = latLng;
        Toast.makeText(this, "Punto de inicio fijado", Toast.LENGTH_SHORT).show();
    }

    private void setPuntoDestino(LatLng latLng) {
        if (markerDestino != null) markerDestino.remove();
        markerDestino = mMap.addMarker(new MarkerOptions().position(latLng).title("Destino"));
        puntoDestino = latLng;
        Toast.makeText(this, "Destino fijado", Toast.LENGTH_SHORT).show();
    }

    private void limpiarMarcadores() {
        if (markerInicio != null) {
            markerInicio.remove();
            markerInicio = null;
            puntoInicio = null;
        }
        if (markerDestino != null) {
            markerDestino.remove();
            markerDestino = null;
            puntoDestino = null;
        }
        // Opcional: limpiar las rutas dibujadas
        mMap.clear();
        // Si había destino precargado por intent, restaurarlo
        double destLat = getIntent().getDoubleExtra("destino_lat", 0);
        double destLon = getIntent().getDoubleExtra("destino_lon", 0);
        String destNombre = getIntent().getStringExtra("destino_nombre");
        if (destLat != 0 && destLon != 0) {
            puntoDestino = new LatLng(destLat, destLon);
            markerDestino = mMap.addMarker(new MarkerOptions().position(puntoDestino).title("Destino: " + destNombre));
        }
        Toast.makeText(this, "Marcadores borrados", Toast.LENGTH_SHORT).show();
    }

    private void planificarRuta() {
        if (puntoInicio == null || puntoDestino == null) {
            Toast.makeText(this, "Debes fijar el punto de inicio y el destino", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/" +
                puntoInicio.latitude + "," + puntoInicio.longitude +
                "/" + puntoDestino.latitude + "," + puntoDestino.longitude +
                "/?travelmode=driving");

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Si no tiene Google Maps, abrir en navegador
            startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual();
        } else {
            Toast.makeText(this, "Permiso de ubicación necesario para usar 'Mi ubicación'", Toast.LENGTH_SHORT).show();
        }
    }
}