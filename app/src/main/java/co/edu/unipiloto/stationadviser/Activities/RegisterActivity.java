package co.edu.unipiloto.stationadviser.Activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.ApiClient;
import co.edu.unipiloto.stationadviser.network.ApiService;
import co.edu.unipiloto.stationadviser.network.models.MensajeResponse;
import co.edu.unipiloto.stationadviser.network.models.RegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextNombre, editTextUsuario, editTextEmail,
            editTextPassword, editTextConfirmPassword, editTextDireccion, editTextTelefono;

    private AutoCompleteTextView spinnerRole;
    private RadioGroup radioGroupGenero;
    private MaterialButton buttonRegister, buttonFecha;
    private ProgressBar progressBar;

    private ApiService apiService;
    private Calendar fechaNacimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Inicializar vistas
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextUsuario = findViewById(R.id.editTextUsuario);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextDireccion = findViewById(R.id.editTextDireccion);
        editTextTelefono = findViewById(R.id.editTextTelefono);

        spinnerRole = findViewById(R.id.spinnerRole);
        radioGroupGenero = findViewById(R.id.radioGroupGenero);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonFecha = findViewById(R.id.buttonFecha);
        progressBar = findViewById(R.id.progressBar);

        // Configurar roles
        String[] roles = {"Cliente", "Empleado de estación", "Equipo técnico", "Entidad reguladora", "Distribuidor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, roles);
        spinnerRole.setAdapter(adapter);
        spinnerRole.setInputType(0);
        spinnerRole.setOnClickListener(v -> spinnerRole.showDropDown());

        buttonFecha.setOnClickListener(v -> mostrarDatePicker());
        buttonRegister.setOnClickListener(v -> registrarUsuario());
    }

    private void mostrarDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    fechaNacimiento = Calendar.getInstance();
                    fechaNacimiento.set(year, month, day);
                    buttonFecha.setText(day + "/" + (month + 1) + "/" + year);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dp.show();
    }

    private void registrarUsuario() {
        String nombre = getText(editTextNombre);
        String usuario = getText(editTextUsuario);
        String email = getText(editTextEmail);
        String password = getText(editTextPassword);
        String confirmPassword = getText(editTextConfirmPassword);
        String direccion = getText(editTextDireccion);
        String telefono = getText(editTextTelefono);
        String role = spinnerRole.getText().toString().trim();

        // Validaciones
        if (nombre.isEmpty() || usuario.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()
                || direccion.isEmpty() || telefono.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fechaNacimiento == null || !esMayorDeEdad(fechaNacimiento)) {
            Toast.makeText(this, "Debe ser mayor de 18 años", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = radioGroupGenero.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Seleccione un género", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarLoading(true);

        // Crear request con nombre, email, password, teléfono y rol
        RegisterRequest request = new RegisterRequest(nombre, email, password, telefono, role);
        Call<MensajeResponse> call = apiService.register(request);
        call.enqueue(new Callback<MensajeResponse>() {
            @Override
            public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isExito()) {
                    Toast.makeText(RegisterActivity.this, "Usuario registrado. Revisa tu email.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMensaje() : "Error desconocido";
                    Toast.makeText(RegisterActivity.this, "Error al registrar: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MensajeResponse> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private boolean esMayorDeEdad(Calendar fecha) {
        Calendar hoy = Calendar.getInstance();
        int edad = hoy.get(Calendar.YEAR) - fecha.get(Calendar.YEAR);
        if (hoy.get(Calendar.DAY_OF_YEAR) < fecha.get(Calendar.DAY_OF_YEAR)) {
            edad--;
        }
        return edad >= 18;
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        buttonRegister.setEnabled(!mostrar);
    }
}