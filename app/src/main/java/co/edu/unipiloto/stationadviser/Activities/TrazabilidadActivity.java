package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.TokenManager;
import co.edu.unipiloto.stationadviser.network.models.AuditoriaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrazabilidadActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;
    private ApiService apiService;
    private TokenManager tokenManager;
    private ArrayAdapter<String> adapter;
    private List<String> auditoriaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trazabilidad);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClientWithToken(tokenManager.getToken()).create(ApiService.class);

        listView = findViewById(R.id.listViewAuditoria);
        progressBar = findViewById(R.id.progressBar);
        auditoriaList = new ArrayList<>();

        cargarAuditoria();
    }

    private void cargarAuditoria() {
        mostrarLoading(true);

        Call<List<AuditoriaResponse>> call = apiService.getAuditoria();
        call.enqueue(new Callback<List<AuditoriaResponse>>() {
            @Override
            public void onResponse(Call<List<AuditoriaResponse>> call, Response<List<AuditoriaResponse>> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    auditoriaList.clear();
                    for (AuditoriaResponse a : response.body()) {
                        String item = String.format("📌 [%s]\n👤 %s\n⚡ %s\n📝 %s\n──────────────────",
                                a.getFecha(), a.getUsuarioEmail(), a.getAccion(), a.getDetalles());
                        auditoriaList.add(item);
                    }
                    if (auditoriaList.isEmpty()) {
                        auditoriaList.add("No hay registros de auditoría");
                    }
                    adapter = new ArrayAdapter<>(TrazabilidadActivity.this,
                            android.R.layout.simple_list_item_1, auditoriaList);
                    listView.setAdapter(adapter);
                } else {
                    Toast.makeText(TrazabilidadActivity.this, "Error al cargar trazabilidad", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AuditoriaResponse>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(TrazabilidadActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }
}