package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.PrecioResponse;
import co.edu.unipiloto.stationadviser.network.models.VentaRequest;
import co.edu.unipiloto.stationadviser.network.models.VentaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarVentaActivity extends AppCompatActivity {

    private Spinner spinnerTipo, spinnerTipoVehiculo;
    private EditText editGalones;
    private TextView textPrecioActual;
    private Button buttonGuardar;
    private ProgressBar progressBar;

    private ApiService apiService;
    private TokenManager tokenManager;

    // ← NUEVO: guarda los nombres reales de la BD
    private List<String> nombresCombustible = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_venta);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        spinnerTipo         = findViewById(R.id.spinnerTipo);
        spinnerTipoVehiculo = findViewById(R.id.spinnerTipoVehiculo);
        editGalones         = findViewById(R.id.editGalones);
        textPrecioActual    = findViewById(R.id.textPrecioActual);
        buttonGuardar       = findViewById(R.id.buttonGuardar);
        progressBar         = findViewById(R.id.progressBar);

        // Tipos de vehículo — igual que antes
        String[] tiposVehiculo = {"Particular", "Taxi", "Servicio Público (Bus)",
                "Camión de carga", "Oficial", "Diplomático", "Moto"};
        ArrayAdapter<String> adapterVehiculo = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tiposVehiculo);
        spinnerTipoVehiculo.setAdapter(adapterVehiculo);

        // ← CAMBIO: carga combustibles desde el backend en vez de hardcodear
        cargarCombustiblesDesdePrecios();

        buttonGuardar.setOnClickListener(v -> registrarVenta());
    }

    // ← NUEVO método
    private void cargarCombustiblesDesdePrecios() {
        apiService.getPrecios().enqueue(new Callback<List<PrecioResponse>>() {
            @Override
            public void onResponse(Call<List<PrecioResponse>> call,
                                   Response<List<PrecioResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    nombresCombustible.clear();
                    for (PrecioResponse precio : response.body()) {
                        String nombre = precio.getCombustibleNombre();
                        if (nombre != null && !nombresCombustible.contains(nombre)) {
                            nombresCombustible.add(nombre);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            RegistrarVentaActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            nombresCombustible);
                    spinnerTipo.setAdapter(adapter);
                } else {
                    Toast.makeText(RegistrarVentaActivity.this,
                            "Error al cargar combustibles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PrecioResponse>> call, Throwable t) {
                Toast.makeText(RegistrarVentaActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registrarVenta() {
        // ← CAMBIO: toma el nombre exacto de la lista dinámica
        String tipo = nombresCombustible.isEmpty()
                ? spinnerTipo.getSelectedItem().toString()
                : nombresCombustible.get(spinnerTipo.getSelectedItemPosition());

        String tipoVehiculo = spinnerTipoVehiculo.getSelectedItem().toString();
        String galonesStr = editGalones.getText().toString();

        if (galonesStr.isEmpty()) {
            Toast.makeText(this, "Ingrese galones vendidos", Toast.LENGTH_SHORT).show();
            return;
        }

        double galones = Double.parseDouble(galonesStr);
        mostrarLoading(true);

        VentaRequest request = new VentaRequest(tipo, galones, tipoVehiculo);
        Call<VentaResponse> call = apiService.registrarVenta(request);

        // — igual que antes desde aquí —
        call.enqueue(new Callback<VentaResponse>() {
            @Override
            public void onResponse(Call<VentaResponse> call, Response<VentaResponse> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    VentaResponse data = response.body();
                    String mensaje = String.format("✅ Venta registrada!\n" +
                                    "Estación: %s\nCombustible: %s\n" +
                                    "Galones: %.2f\nPrecio unitario: $%.2f\n" +
                                    "Total: $%.2f\nFecha: %s",
                            data.getEstacionNombre(), data.getCombustibleNombre(),
                            data.getCantidad(), data.getPrecioUnitario(),
                            data.getMontoTotal(), data.getFecha());
                    Toast.makeText(RegistrarVentaActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    editGalones.setText("");
                } else {
                    String error = "Error al registrar venta";
                    try {
                        if (response.errorBody() != null) error = response.errorBody().string();
                    } catch (Exception e) {}
                    Toast.makeText(RegistrarVentaActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VentaResponse> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(RegistrarVentaActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        buttonGuardar.setEnabled(!mostrar);
    }
}