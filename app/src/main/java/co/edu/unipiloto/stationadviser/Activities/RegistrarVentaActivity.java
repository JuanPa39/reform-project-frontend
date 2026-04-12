package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.VentaRequest;
import co.edu.unipiloto.stationadviser.network.models.VentaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarVentaActivity extends AppCompatActivity {

    private Spinner spinnerTipo;
    private EditText editGalones;
    private TextView textPrecioActual;
    private Button buttonGuardar;
    private ProgressBar progressBar;

    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_venta);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        spinnerTipo = findViewById(R.id.spinnerTipo);
        editGalones = findViewById(R.id.editGalones);
        textPrecioActual = findViewById(R.id.textPrecioActual);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        progressBar = findViewById(R.id.progressBar);

        String[] tipos = {"ACPM", "Gasolina Corriente", "Gasolina Extra"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tipos);
        spinnerTipo.setAdapter(adapter);

        buttonGuardar.setOnClickListener(v -> registrarVenta());
    }

    private void registrarVenta() {
        String tipo = spinnerTipo.getSelectedItem().toString();
        String galonesStr = editGalones.getText().toString();

        if (galonesStr.isEmpty()) {
            Toast.makeText(this, "Ingrese galones vendidos", Toast.LENGTH_SHORT).show();
            return;
        }

        double galones = Double.parseDouble(galonesStr);
        mostrarLoading(true);

        VentaRequest request = new VentaRequest(tipo, galones);
        Call<VentaResponse> call = apiService.registrarVenta(request);

        call.enqueue(new Callback<VentaResponse>() {
            @Override
            public void onResponse(Call<VentaResponse> call, Response<VentaResponse> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    VentaResponse data = response.body();
                    String mensaje = String.format("✅ Venta registrada!\n" +
                                    "Estación: %s\n" +
                                    "Combustible: %s\n" +
                                    "Galones: %.2f\n" +
                                    "Precio unitario: $%.2f\n" +
                                    "Total: $%.2f\n" +
                                    "Fecha: %s",
                            data.getEstacionNombre(), data.getCombustibleNombre(),
                            data.getCantidad(), data.getPrecioUnitario(),
                            data.getMontoTotal(), data.getFecha());

                    Toast.makeText(RegistrarVentaActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    editGalones.setText("");
                } else {
                    String error = "Error al registrar venta";
                    try {
                        if (response.errorBody() != null) {
                            error = response.errorBody().string();
                        }
                    } catch (Exception e) {}
                    Toast.makeText(RegistrarVentaActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VentaResponse> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(RegistrarVentaActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? android.view.View.VISIBLE : android.view.View.GONE);
        buttonGuardar.setEnabled(!mostrar);
    }
}