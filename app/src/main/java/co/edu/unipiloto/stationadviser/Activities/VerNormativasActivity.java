package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.graphics.Typeface;
import java.util.List;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.NormativaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerNormativasActivity extends AppCompatActivity {

    private LinearLayout contenedorNormativas;
    private ProgressBar progressBar;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_normativas);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        contenedorNormativas = findViewById(R.id.contenedorNormativas);
        progressBar = findViewById(R.id.progressBar);

        cargarNormativas();
    }

    private void cargarNormativas() {
        mostrarLoading(true);
        contenedorNormativas.removeAllViews();

        Call<List<NormativaResponse>> call = apiService.getNormativas();
        call.enqueue(new Callback<List<NormativaResponse>>() {
            @Override
            public void onResponse(Call<List<NormativaResponse>> call, Response<List<NormativaResponse>> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<NormativaResponse> normativas = response.body();
                    if (normativas.isEmpty()) {
                        TextView vacio = new TextView(VerNormativasActivity.this);
                        vacio.setText("No hay normativas registradas");
                        vacio.setTextColor(0xFFAAAAAA);
                        contenedorNormativas.addView(vacio);
                        return;
                    }
                    for (NormativaResponse n : normativas) {
                        CardView card = new CardView(VerNormativasActivity.this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 12);
                        card.setLayoutParams(params);
                        card.setCardBackgroundColor(0xFF1A2D42);
                        card.setRadius(16f);
                        card.setCardElevation(4f);

                        LinearLayout layout = new LinearLayout(VerNormativasActivity.this);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(28, 20, 28, 20);

                        TextView titulo = new TextView(VerNormativasActivity.this);
                        titulo.setText(n.getNombre());
                        titulo.setTextColor(0xFFFFFFFF);
                        titulo.setTextSize(16f);
                        titulo.setTypeface(null, Typeface.BOLD);  // ← CORREGIDO

                        TextView descripcion = new TextView(VerNormativasActivity.this);
                        descripcion.setText(n.getDescripcion());
                        descripcion.setTextColor(0xFFCCCCCC);
                        descripcion.setTextSize(13f);
                        descripcion.setPadding(0, 8, 0, 8);

                        TextView fechas = new TextView(VerNormativasActivity.this);
                        fechas.setText("Vigencia: " + n.getFechaInicio() + " - " + n.getFechaFin());
                        fechas.setTextColor(0xFF2196F3);
                        fechas.setTextSize(11f);

                        layout.addView(titulo);
                        layout.addView(descripcion);
                        layout.addView(fechas);
                        card.addView(layout);
                        contenedorNormativas.addView(card);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<NormativaResponse>> call, Throwable t) {
                mostrarLoading(false);
                TextView error = new TextView(VerNormativasActivity.this);
                error.setText("Error: " + t.getMessage());
                error.setTextColor(0xFFEF5350);
                contenedorNormativas.addView(error);
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}