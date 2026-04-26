package co.edu.unipiloto.stationadviser.Activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.VentaResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialVentasActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvVacio;
    private ApiService apiService;
    private TokenManager tokenManager;
    private VentaAdapter adapter;
    private List<VentaResponse> ventas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_ventas);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar  = findViewById(R.id.progressBar);
        tvVacio      = findViewById(R.id.tvVacio);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VentaAdapter(ventas);
        recyclerView.setAdapter(adapter);

        cargarHistorial();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarHistorial();
    }

    // ─── Carga el historial ────────────────────────────────────────────────────

    private void cargarHistorial() {
        mostrarLoading(true);
        apiService.getHistorialVentas().enqueue(new Callback<List<VentaResponse>>() {
            @Override
            public void onResponse(Call<List<VentaResponse>> call,
                                   Response<List<VentaResponse>> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ventas.clear();
                    ventas.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    tvVacio.setVisibility(ventas.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(HistorialVentasActivity.this,
                            "Error al cargar historial", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VentaResponse>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(HistorialVentasActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Descarga la factura PDF ───────────────────────────────────────────────

    private void descargarFactura(Long ventaId) {
        // Pedir permiso en Android ≤ 9
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
            return;
        }

        Toast.makeText(this, "Generando factura...", Toast.LENGTH_SHORT).show();

        apiService.descargarFacturaPdf(ventaId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    guardarYAbrirPdf(response.body(), ventaId);
                } else {
                    Toast.makeText(HistorialVentasActivity.this,
                            "Error al generar factura (código " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(HistorialVentasActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void guardarYAbrirPdf(ResponseBody body, Long ventaId) {
        String nombreArchivo = "factura_" + String.format("%08d", ventaId) + ".pdf";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ — MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    Toast.makeText(this, "No se pudo crear el archivo", Toast.LENGTH_SHORT).show();
                    return;
                }
                try (OutputStream os = getContentResolver().openOutputStream(uri);
                     InputStream is = body.byteStream()) {
                    copiarStream(is, os);
                }
                Toast.makeText(this, "Factura guardada en Descargas", Toast.LENGTH_LONG).show();
                abrirPdf(uri);

            } else {
                // Android ≤ 9 — File directo
                File destino = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS), nombreArchivo);
                try (FileOutputStream fos = new FileOutputStream(destino);
                     InputStream is = body.byteStream()) {
                    copiarStream(is, fos);
                }
                Toast.makeText(this, "Factura guardada en Descargas", Toast.LENGTH_LONG).show();
                Uri uri = FileProvider.getUriForFile(this,
                        getPackageName() + ".provider", destino);
                abrirPdf(uri);
            }

        } catch (IOException e) {
            Toast.makeText(this, "Error al guardar: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void abrirPdf(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            startActivity(Intent.createChooser(intent, "Abrir factura con..."));
        } catch (Exception e) {
            Toast.makeText(this, "No hay visor de PDF instalado", Toast.LENGTH_SHORT).show();
        }
    }

    private void copiarStream(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) os.write(buf, 0, n);
        os.flush();
    }

    private void mostrarLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ─── RecyclerView Adapter ─────────────────────────────────────────────────

    private class VentaAdapter extends RecyclerView.Adapter<VentaAdapter.VentaViewHolder> {

        private final List<VentaResponse> lista;

        VentaAdapter(List<VentaResponse> lista) { this.lista = lista; }

        @NonNull
        @Override
        public VentaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_venta_historial, parent, false);
            return new VentaViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VentaViewHolder h, int pos) {
            VentaResponse v = lista.get(pos);

            h.tvCombustible.setText(v.getCombustibleNombre() != null
                    ? v.getCombustibleNombre() : "Combustible");
            h.tvEstacion.setText(v.getEstacionNombre() != null
                    ? v.getEstacionNombre() : "Estación");
            h.tvFecha.setText(v.getFecha() != null ? v.getFecha() : "");
            h.tvCantidad.setText(String.format("%.2f L", v.getCantidad()));
            h.tvMonto.setText(String.format("$%,.0f", v.getMontoTotal()));

            h.btnFactura.setOnClickListener(view -> descargarFactura(v.getId()));
        }

        @Override
        public int getItemCount() { return lista.size(); }

        class VentaViewHolder extends RecyclerView.ViewHolder {
            TextView tvCombustible, tvEstacion, tvFecha, tvCantidad, tvMonto;
            android.widget.Button btnFactura;

            VentaViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCombustible = itemView.findViewById(R.id.tvCombustible);
                tvEstacion    = itemView.findViewById(R.id.tvEstacion);
                tvFecha       = itemView.findViewById(R.id.tvFecha);
                tvCantidad    = itemView.findViewById(R.id.tvCantidad);
                tvMonto       = itemView.findViewById(R.id.tvMonto);
                btnFactura    = itemView.findViewById(R.id.btnFactura);
            }
        }
    }
}