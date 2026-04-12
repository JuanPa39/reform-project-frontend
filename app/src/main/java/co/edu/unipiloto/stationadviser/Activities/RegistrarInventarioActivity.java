package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.DisponibilidadResponse;
import co.edu.unipiloto.stationadviser.network.models.InventarioRequest;
import co.edu.unipiloto.stationadviser.network.models.InventarioResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarInventarioActivity extends AppCompatActivity {

    private Spinner spinnerTipo;
    private EditText editCantidad;
    private Button buttonGuardar;
    private ProgressBar progressBar;
    private TextView textStockActual, textNuevoStock;

    private ApiService apiService;
    private TokenManager tokenManager;
    private double stockActual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_inventario);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        spinnerTipo = findViewById(R.id.spinnerTipo);
        editCantidad = findViewById(R.id.editCantidad);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        progressBar = findViewById(R.id.progressBar);
        textStockActual = findViewById(R.id.textStockActual);
        textNuevoStock = findViewById(R.id.textNuevoStock);

        String[] tipos = {"ACPM", "Gasolina Corriente", "Gasolina Extra"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tipos);
        spinnerTipo.setAdapter(adapter);

        // Listener para cuando cambia el combustible seleccionado
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cargarStockActual();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Listener para cuando cambia la cantidad
        editCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularNuevoStock();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonGuardar.setOnClickListener(v -> registrarInventario());

        // Cargar stock inicial
        cargarStockActual();
    }

    private void cargarStockActual() {
        String tipo = spinnerTipo.getSelectedItem().toString();

        // IMPORTANTE: Necesitas agregar este endpoint en tu backend
        // GET /inventario/disponibilidad
        Call<List<DisponibilidadResponse>> call = apiService.getDisponibilidad();
        call.enqueue(new Callback<List<DisponibilidadResponse>>() {
            @Override
            public void onResponse(Call<List<DisponibilidadResponse>> call, Response<List<DisponibilidadResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (DisponibilidadResponse d : response.body()) {
                        if (d.getCombustibleNombre().equals(tipo)) {
                            stockActual = d.getCantidadDisponible();
                            textStockActual.setText(String.format("Stock actual: %.0f galones", stockActual));
                            calcularNuevoStock();
                            break;
                        }
                    }
                } else {
                    textStockActual.setText("Error al cargar stock");
                }
            }
            @Override
            public void onFailure(Call<List<DisponibilidadResponse>> call, Throwable t) {
                textStockActual.setText("Error: " + t.getMessage());
            }
        });
    }

    private void calcularNuevoStock() {
        String cantidadStr = editCantidad.getText().toString();
        if (!cantidadStr.isEmpty()) {
            double cantidad = Double.parseDouble(cantidadStr);
            double nuevoStock = stockActual + cantidad;
            textNuevoStock.setText(String.format("Nuevo stock: %.0f galones", nuevoStock));
        } else {
            textNuevoStock.setText("Nuevo stock: -- galones");
        }
    }

    private void registrarInventario() {
        String tipo = spinnerTipo.getSelectedItem().toString();
        String cantidadStr = editCantidad.getText().toString();

        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, "Ingrese cantidad", Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidad = Integer.parseInt(cantidadStr);
        mostrarLoading(true);

        InventarioRequest request = new InventarioRequest(tipo, cantidad);
        Call<InventarioResponse> call = apiService.registrarInventario(request);

        call.enqueue(new Callback<InventarioResponse>() {
            @Override
            public void onResponse(Call<InventarioResponse> call, Response<InventarioResponse> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    InventarioResponse data = response.body();
                    String mensaje = String.format("✅ Inventario actualizado!\n%s: %.0f galones",
                            data.getCombustibleNombre(), data.getCantidadDisponible());
                    Toast.makeText(RegistrarInventarioActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    editCantidad.setText("");
                    cargarStockActual();
                } else {
                    Toast.makeText(RegistrarInventarioActivity.this,
                            "Error al guardar inventario: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InventarioResponse> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(RegistrarInventarioActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        buttonGuardar.setEnabled(!mostrar);
    }
}