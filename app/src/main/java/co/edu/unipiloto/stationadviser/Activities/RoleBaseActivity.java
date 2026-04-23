package co.edu.unipiloto.stationadviser.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.stationadviser.R;
import co.edu.unipiloto.stationadviser.network.TokenManager;

public class RoleBaseActivity extends AppCompatActivity {

    private static final String TAG = "RoleBaseActivity";
    private Button button1, button2, button3, button4, button5, button6, button7, button8, button9, button10, button11, buttonLogout;
    private TextView textViewEmail;
    private String userEmail;
    private String userRole;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_base);

        tokenManager = new TokenManager(this);

        Intent intent = getIntent();
        if (intent != null) {
            userEmail = intent.getStringExtra("email");
            userRole = intent.getStringExtra("role");
        }

        if (userEmail == null) {
            userEmail = tokenManager.getUserEmail();
            userRole = tokenManager.getUserRole();
        }

        Log.d(TAG, "Email: " + userEmail);
        Log.d(TAG, "Rol: " + userRole);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);
        button10 = findViewById(R.id.button10);
        button11 = findViewById(R.id.button11);
        buttonLogout = findViewById(R.id.buttonLogout);
        textViewEmail = findViewById(R.id.textViewEmail);

        if (userEmail != null) {
            textViewEmail.setText("Correo: " + userEmail);
        } else {
            textViewEmail.setText("Correo: No disponible");
        }

        if (userRole == null) {
            userRole = "Cliente";
        }

        configurarBotonesPorRol();

        buttonLogout.setOnClickListener(v -> {
            tokenManager.clearTokens();
            Intent intentLogout = new Intent(RoleBaseActivity.this, LoginActivity.class);
            intentLogout.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intentLogout);
            finish();
            Toast.makeText(RoleBaseActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        });
    }

    private void configurarBotonesPorRol() {
        button1.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        button3.setVisibility(View.GONE);
        button4.setVisibility(View.GONE);
        button5.setVisibility(View.GONE);
        button6.setVisibility(View.GONE);
        button7.setVisibility(View.GONE);
        button8.setVisibility(View.GONE);
        button9.setVisibility(View.GONE);
        button10.setVisibility(View.GONE);
        button11.setVisibility(View.GONE);

        Log.d(TAG, "Configurando botones para rol: " + userRole);

        if (userRole != null) {
            // Convertir a minúsculas para comparación case-insensitive
            String roleLower = userRole.toLowerCase();

            switch (roleLower) {
                case "cliente":
                    button1.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.VISIBLE);
                    button1.setText("Consultar precio combustible");
                    button2.setText("Consultar disponibilidad");
                    button1.setOnClickListener(v -> startActivity(new Intent(this, ConsultarPrecioActivity.class)));
                    button2.setOnClickListener(v -> startActivity(new Intent(this, ConsultarDisponibilidadActivity.class)));
                    break;

                case "empleado de estación":
                case "empleado de estacion":
                    button1.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.VISIBLE);
                    button3.setVisibility(View.VISIBLE);
                    button4.setVisibility(View.VISIBLE);
                    button5.setVisibility(View.VISIBLE);
                    button6.setVisibility(View.VISIBLE);
                    button7.setVisibility(View.VISIBLE);
                    button8.setVisibility(View.VISIBLE);
                    button9.setVisibility(View.VISIBLE);
                    button10.setVisibility(View.VISIBLE);

                    button1.setText("Registrar estación");
                    button2.setText("Consultar notificaciones");
                    button3.setText("Ver estaciones");
                    button4.setText("Registrar precio combustible");
                    button5.setText("Ver precios combustible");
                    button6.setText("Registrar inventario");
                    button7.setText("Registrar venta");
                    button8.setText("Historial ventas");
                    button9.setText("Reporte mensual");
                    button10.setText("📊 Reporte consumo por zona");  // ← CAMBIADO

                    button1.setOnClickListener(v -> startActivity(new Intent(this, RegistrarEstacionActivity.class)));
                    button2.setOnClickListener(v -> startActivity(new Intent(this, ConsultarNotificacionActivity.class)));
                    button3.setOnClickListener(v -> startActivity(new Intent(this, ListaEstacionesActivity.class)));
                    button4.setOnClickListener(v -> startActivity(new Intent(this, RegistrarPrecioCombustibleActivity.class)));
                    button5.setOnClickListener(v -> startActivity(new Intent(this, ConsultarPrecioActivity.class)));
                    button6.setOnClickListener(v -> startActivity(new Intent(this, RegistrarInventarioActivity.class)));
                    button7.setOnClickListener(v -> startActivity(new Intent(this, RegistrarVentaActivity.class)));
                    button8.setOnClickListener(v -> startActivity(new Intent(this, HistorialVentasActivity.class)));
                    button9.setOnClickListener(v -> startActivity(new Intent(this, ReporteMensualActivity.class)));
                    button10.setOnClickListener(v -> startActivity(new Intent(this, ReporteConsumoZonaActivity.class)));  // ← NUEVO
                    break;

                case "equipo técnico":
                case "equipo tecnico":
                    button1.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.VISIBLE);
                    button3.setVisibility(View.VISIBLE);
                    button1.setText("Diagnosticar surtidor");
                    button2.setText("Reparar equipo");
                    button3.setText("Mantenimiento preventivo");
                    button1.setOnClickListener(v -> Toast.makeText(this, "Diagnóstico (próximamente)", Toast.LENGTH_SHORT).show());
                    button2.setOnClickListener(v -> Toast.makeText(this, "Reparación (próximamente)", Toast.LENGTH_SHORT).show());
                    button3.setOnClickListener(v -> Toast.makeText(this, "Mantenimiento (próximamente)", Toast.LENGTH_SHORT).show());
                    break;

                case "entidad reguladora":
                    button1.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.VISIBLE);
                    button3.setVisibility(View.VISIBLE);
                    button4.setVisibility(View.VISIBLE);
                    button5.setVisibility(View.VISIBLE);  // ← AGREGAR
                    button1.setText("Ver normativas");
                    button2.setText("Enviar notificación de inconsistencias");
                    button3.setText("Registrar multa");
                    button4.setText("Generar reporte mensual");
                    button5.setText("📊 Reporte consumo por zona");  // ← NUEVO
                    button1.setOnClickListener(v -> startActivity(new Intent(this, VerNormativasActivity.class)));
                    button2.setOnClickListener(v -> startActivity(new Intent(this, NotificacionInconsistenciasActivity.class)));
                    button3.setOnClickListener(v -> Toast.makeText(this, "Multas (próximamente)", Toast.LENGTH_SHORT).show());
                    button4.setOnClickListener(v -> startActivity(new Intent(this, ReporteMensualActivity.class)));
                    button5.setOnClickListener(v -> startActivity(new Intent(this, ReporteConsumoZonaActivity.class)));  // ← NUEVO
                    break;

                case "distribuidor":
                    button1.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.VISIBLE);
                    button1.setText("Ver precios combustible");
                    button2.setText("Consultar disponibilidad");
                    button1.setOnClickListener(v -> startActivity(new Intent(this, ConsultarPrecioActivity.class)));
                    button2.setOnClickListener(v -> startActivity(new Intent(this, ConsultarDisponibilidadActivity.class)));
                    break;

                case "admin":
                    button1.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.VISIBLE);
                    button3.setVisibility(View.VISIBLE);
                    button4.setVisibility(View.VISIBLE);
                    button5.setVisibility(View.VISIBLE);
                    button6.setVisibility(View.VISIBLE);
                    button7.setVisibility(View.VISIBLE);
                    button8.setVisibility(View.VISIBLE);
                    button9.setVisibility(View.VISIBLE);
                    button10.setVisibility(View.VISIBLE);
                    button11.setVisibility(View.VISIBLE);

                    button1.setText("👤 Asignar estación a empleado");  // ← NUEVO
                    button2.setText("Registrar estación");
                    button3.setText("Ver estaciones");
                    button4.setText("Registrar precio combustible");
                    button5.setText("Ver precios combustible");
                    button6.setText("Registrar inventario");
                    button7.setText("Registrar venta");
                    button8.setText("Historial ventas");
                    button9.setText("Reporte mensual");
                    button10.setText("📋 TRAZABILIDAD / AUDITORÍA");
                    button11.setText("📊 Reporte consumo por zona");

                    button1.setOnClickListener(v -> startActivity(new Intent(this, AsignarEstacionActivity.class)));  // ← NUEVO
                    button2.setOnClickListener(v -> startActivity(new Intent(this, RegistrarEstacionActivity.class)));
                    button3.setOnClickListener(v -> startActivity(new Intent(this, ListaEstacionesActivity.class)));
                    button4.setOnClickListener(v -> startActivity(new Intent(this, RegistrarPrecioCombustibleActivity.class)));
                    button5.setOnClickListener(v -> startActivity(new Intent(this, ConsultarPrecioActivity.class)));
                    button6.setOnClickListener(v -> startActivity(new Intent(this, RegistrarInventarioActivity.class)));
                    button7.setOnClickListener(v -> startActivity(new Intent(this, RegistrarVentaActivity.class)));
                    button8.setOnClickListener(v -> startActivity(new Intent(this, HistorialVentasActivity.class)));
                    button9.setOnClickListener(v -> startActivity(new Intent(this, ReporteMensualActivity.class)));
                    button10.setOnClickListener(v -> startActivity(new Intent(this, TrazabilidadActivity.class)));
                    button11.setOnClickListener(v -> startActivity(new Intent(this, ReporteConsumoZonaActivity.class)));  // ← NUEVO

                    break;

                default:
                    Log.e(TAG, "Rol desconocido: " + userRole);
                    button1.setVisibility(View.VISIBLE);
                    button1.setText("Rol no reconocido");
                    break;
            }
        }
    }
}