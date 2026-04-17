package com.example.gasolineras_espana.ui.mapa;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gasolineras_espana.R;
import com.example.gasolineras_espana.utils.BrandUtils;

public class DetalleActivity extends AppCompatActivity {

    // Etiquetas estaticas para enviarnos maletines de datos a traves del Intent
    public static final String EXTRA_ROTULO = "extra_rotulo";
    public static final String EXTRA_DIRECCION = "extra_direccion";
    public static final String EXTRA_MUNICIPIO = "extra_municipio";
    public static final String EXTRA_HORARIO = "extra_horario";
    public static final String EXTRA_PRECIOS = "extra_precios";
    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LON = "extra_lon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        // Atar los hilos entre XML (vistas) y nuestro codigo Java
        ImageView imgLogo = findViewById(R.id.imgDetalleLogo);
        TextView txtRotulo = findViewById(R.id.txtDetalleRotulo);
        TextView txtDireccion = findViewById(R.id.txtDetalleDireccion);
        TextView txtHorario = findViewById(R.id.txtDetalleHorario);
        TextView txtPrecios = findViewById(R.id.txtDetallePrecios);
        Button btnIrMaps = findViewById(R.id.btnIrMaps);

        // Recuperar el 'maletin' de datos que nos ha pasado MapaActivity al hacer click
        Intent intent = getIntent();
        String rotulo = intent.getStringExtra(EXTRA_ROTULO);
        String direccion = intent.getStringExtra(EXTRA_DIRECCION) + ", " + intent.getStringExtra(EXTRA_MUNICIPIO);
        String horario = intent.getStringExtra(EXTRA_HORARIO);
        String precios = intent.getStringExtra(EXTRA_PRECIOS);
        
        // Cuidado con coordenadas: 0.0 sera el seguro si algo falla al recibirlas
        double lat = intent.getDoubleExtra(EXTRA_LAT, 0.0);
        double lon = intent.getDoubleExtra(EXTRA_LON, 0.0);

        // Rellenamos el layout para que luzca bien
        txtRotulo.setText(rotulo != null ? rotulo : "Estación de Servicio");
        txtDireccion.setText(direccion);
        txtHorario.setText("⌚ " + (horario != null ? horario : "Sin especificar"));
        txtPrecios.setText(precios);

        // ¡Invocamos a BrandUtils para que nos de la pedazo de foto!
        if (rotulo != null) {
            int recursoLogo = BrandUtils.getLogoResId(rotulo);
            imgLogo.setImageResource(recursoLogo);
        }

        // Y la joya de la corona: "Como llegar"
        btnIrMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preparamos un link especial (schema google.navigation) que abre el modo coche
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Aseguramos que se intente abrir con la app oficial de Google Maps
                mapIntent.setPackage("com.google.android.apps.maps");

                // Verificación vital: ¿El usuario tiene la app de mapas descargada en su movil?
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Si no la tiene, un sutil aviso y no crasheamos la app
                    Toast.makeText(DetalleActivity.this, "Necesitas instalar Google Maps :)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
