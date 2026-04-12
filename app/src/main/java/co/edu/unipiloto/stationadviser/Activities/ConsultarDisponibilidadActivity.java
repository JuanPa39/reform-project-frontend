package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.DisponibilidadResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConsultarDisponibilidadActivity extends AppCompatActivity {

    private Spinner spinnerTipoCombustible, spinnerTipoVehiculo;
    private EditText editGalones;  // Cambiado de litros a galones
    private Button buttonConsultar;
    private TextView textResultado;
    private ProgressBar progressBar;

    private ApiService apiService;
    private TokenManager tokenManager;
    private static final double GALON_A_LITROS = 3.78541;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_disponibilidad);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        spinnerTipoCombustible = findViewById(R.id.spinnerTipo);
        spinnerTipoVehiculo = findViewById(R.id.spinnerTipoVehiculo);
        editGalones = findViewById(R.id.editGalones);
        buttonConsultar = findViewById(R.id.buttonConsultar);
        textResultado = findViewById(R.id.textResultado);
        progressBar = findViewById(R.id.progressBar);

        // Tipos de combustible
        String[] tiposCombustible = {"ACPM", "Gasolina Corriente", "Gasolina Extra"};
        ArrayAdapter<String> adapterCombustible = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tiposCombustible);
        spinnerTipoCombustible.setAdapter(adapterCombustible);

        // Tipos de vehículo para subsidio
        String[] tiposVehiculo = {"Particular", "Taxi", "Servicio Público (Bus)", "Camión de carga", "Oficial", "Diplomático", "Moto"};
        ArrayAdapter<String> adapterVehiculo = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tiposVehiculo);
        spinnerTipoVehiculo.setAdapter(adapterVehiculo);

        buttonConsultar.setOnClickListener(v -> consultarDisponibilidad());
    }

    private void consultarDisponibilidad() {
        String combustible = spinnerTipoCombustible.getSelectedItem().toString();
        String tipoVehiculo = spinnerTipoVehiculo.getSelectedItem().toString();
        String galonesStr = editGalones.getText().toString();

        if (galonesStr.isEmpty()) {
            Toast.makeText(this, "Ingrese galones", Toast.LENGTH_SHORT).show();
            return;
        }

        double galonesSolicitados = Double.parseDouble(galonesStr);
        mostrarLoading(true);

        // Enviar también tipoVehiculo y cantidad al backend
        Call<DisponibilidadResponse> call = apiService.getDisponibilidad(combustible, tipoVehiculo, galonesSolicitados);
        call.enqueue(new Callback<DisponibilidadResponse>() {
            @Override
            public void onResponse(Call<DisponibilidadResponse> call, Response<DisponibilidadResponse> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    double galonesDisponibles = response.body().getCantidadDisponible();
                    boolean subsidioAplica = response.body().isAplicaSubsidio();
                    String mensajeSubsidio = response.body().getMensajeSubsidio();

                    String resultado = "📊 DISPONIBILIDAD\n";
                    resultado += "Galones disponibles: " + String.format("%.0f", galonesDisponibles) + " gal\n";
                    resultado += "───────────────────\n";

                    if (galonesDisponibles >= galonesSolicitados) {
                        resultado += "✅ Stock suficiente para " + galonesSolicitados + " galones\n";
                    } else {
                        resultado += "❌ Stock insuficiente. Solo hay " + String.format("%.0f", galonesDisponibles) + " galones\n";
                    }

                    resultado += "───────────────────\n";
                    resultado += "💰 SUBSIDIO: " + (subsidioAplica ? "APLICA" : "NO APLICA") + "\n";
                    resultado += mensajeSubsidio;

                    textResultado.setText(resultado);
                } else {
                    textResultado.setText("❌ Error al consultar disponibilidad");
                }
            }

            @Override
            public void onFailure(Call<DisponibilidadResponse> call, Throwable t) {
                mostrarLoading(false);
                textResultado.setText("❌ Error de conexión: " + t.getMessage());
            }
        });
    }

    private boolean verificarSubsidio(String tipoVehiculo, String combustible, double galones) {
        // Reglas de subsidio según el Decreto 1428/2025
        if (combustible.equals("Gasolina Corriente")) {
            // Gasolina Corriente tiene subsidio solo hasta 10 galones
            if (tipoVehiculo.equals("Particular") || tipoVehiculo.equals("Oficial") || tipoVehiculo.equals("Diplomático")) {
                return galones <= 10;
            } else if (tipoVehiculo.equals("Taxi") || tipoVehiculo.equals("Servicio Público (Bus)") || tipoVehiculo.equals("Camión de carga")) {
                return galones <= 50;
            } else if (tipoVehiculo.equals("Moto")) {
                return galones <= 5;
            }
        } else if (combustible.equals("ACPM")) {
            // ACPM solo tiene subsidio para transporte público y carga
            if (tipoVehiculo.equals("Taxi") || tipoVehiculo.equals("Servicio Público (Bus)") || tipoVehiculo.equals("Camión de carga")) {
                return galones <= 30;
            }
            // Particulares, Oficiales, Diplomáticos NO tienen subsidio para ACPM
            return false;
        } else if (combustible.equals("Gasolina Extra")) {
            // Gasolina Extra NO tiene subsidio
            return false;
        }
        return false;
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        buttonConsultar.setEnabled(!mostrar);
    }
}