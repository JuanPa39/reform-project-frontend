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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Buscarfacturaactivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_STORAGE = 113;

    private EditText etFacturaId;
    private Button btnBuscar;
    private ProgressBar progressBar;
    private TextView tvEstado;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_factura);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        etFacturaId  = findViewById(R.id.etFacturaId);
        btnBuscar    = findViewById(R.id.btnBuscar);
        progressBar  = findViewById(R.id.progressBar);
        tvEstado     = findViewById(R.id.tvEstado);

        btnBuscar.setOnClickListener(v -> buscarYDescargar());
    }

    private void buscarYDescargar() {
        String idStr = etFacturaId.getText().toString().trim();
        if (idStr.isEmpty()) {
            etFacturaId.setError("Ingresa el número de factura");
            return;
        }

        long ventaId;
        try {
            ventaId = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            etFacturaId.setError("Solo números");
            return;
        }

        // Permiso en Android ≤ 9
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            return;
        }

        mostrarLoading(true);
        tvEstado.setText("Buscando factura N° " + String.format("%08d", ventaId) + "...");

        apiService.descargarFacturaPdf(ventaId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    guardarYAbrirPdf(response.body(), ventaId);
                } else if (response.code() == 404) {
                    tvEstado.setText("❌ No se encontró la factura N° " + idStr);
                } else {
                    tvEstado.setText("❌ Error al obtener la factura (código " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mostrarLoading(false);
                tvEstado.setText("❌ Error de conexión: " + t.getMessage());
            }
        });
    }

    private void guardarYAbrirPdf(ResponseBody body, long ventaId) {
        String nombreArchivo = "factura_" + String.format("%08d", ventaId) + ".pdf";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    tvEstado.setText("❌ No se pudo crear el archivo");
                    return;
                }
                try (OutputStream os = getContentResolver().openOutputStream(uri);
                     InputStream is = body.byteStream()) {
                    copiarStream(is, os);
                }
                tvEstado.setText("✅ Factura guardada en Descargas");
                Toast.makeText(this, "Factura guardada", Toast.LENGTH_SHORT).show();
                abrirPdf(uri);

            } else {
                File destino = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS), nombreArchivo);
                try (FileOutputStream fos = new FileOutputStream(destino);
                     InputStream is = body.byteStream()) {
                    copiarStream(is, fos);
                }
                tvEstado.setText("✅ Factura guardada en Descargas");
                Uri uri = FileProvider.getUriForFile(this,
                        getPackageName() + ".provider", destino);
                abrirPdf(uri);
            }

        } catch (IOException e) {
            tvEstado.setText("❌ Error al guardar: " + e.getMessage());
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
        btnBuscar.setEnabled(!show);
    }
}