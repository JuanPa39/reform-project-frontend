package co.edu.unipiloto.stationadviser;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import co.edu.unipiloto.stationadviser.Activities.LoginActivity;
import co.edu.unipiloto.stationadviser.Activities.RoleBaseActivity;
import co.edu.unipiloto.stationadviser.network.TokenManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TokenManager tokenManager = new TokenManager(this);

        // Verificar si hay sesión activa
        if (tokenManager.getToken() != null && !tokenManager.getToken().isEmpty()) {
            Intent intent = new Intent(MainActivity.this, RoleBaseActivity.class);
            intent.putExtra("email", tokenManager.getUserEmail());
            intent.putExtra("role", tokenManager.getUserRole());
            startActivity(intent);
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        finish();
    }
}