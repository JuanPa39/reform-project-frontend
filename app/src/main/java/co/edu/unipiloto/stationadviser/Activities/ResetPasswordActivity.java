package co.edu.unipiloto.stationadviser.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.models.MensajeResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText editCorreo, editNuevaContrasena;
    private Button buttonCambiar;
    private ProgressBar progressBar;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        apiService = ApiClient.getClient().create(ApiService.class);

        editCorreo = findViewById(R.id.editCorreoReset);
        editNuevaContrasena = findViewById(R.id.editNuevaContrasena);
        buttonCambiar = findViewById(R.id.buttonCambiarContrasena);
        progressBar = findViewById(R.id.progressBar);

        buttonCambiar.setOnClickListener(v -> cambiarContrasena());
    }

    private void cambiarContrasena() {
        String correo = editCorreo.getText().toString().trim();
        String nuevaPass = editNuevaContrasena.getText().toString().trim();

        if (correo.isEmpty() || nuevaPass.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarLoading(true);

        Call<MensajeResponse> call = apiService.resetPassword(correo, nuevaPass);
        call.enqueue(new Callback<MensajeResponse>() {
            @Override
            public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isExito()) {
                    Toast.makeText(ResetPasswordActivity.this, "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "El correo no existe", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MensajeResponse> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(ResetPasswordActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? android.view.View.VISIBLE : android.view.View.GONE);
        buttonCambiar.setEnabled(!mostrar);
    }
}