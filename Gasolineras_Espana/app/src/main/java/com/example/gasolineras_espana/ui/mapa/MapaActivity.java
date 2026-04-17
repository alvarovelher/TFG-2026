package com.example.gasolineras_espana.ui.mapa;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.gasolineras_espana.R;
import com.example.gasolineras_espana.model.Gasolinera;
import java.util.List;

import com.example.gasolineras_espana.ui.mapa.cluster.GasolineraClusterItem;
import com.example.gasolineras_espana.ui.mapa.cluster.GasolineraClusterRenderer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.ClusterManager;





public class MapaActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MAPA_CLUSTER";

    private GoogleMap mMap;
    private ClusterManager<GasolineraClusterItem> clusterManager;

    private MapaViewModel viewModel;

    private FusedLocationProviderClient fusedLocationClient;
    private boolean primeraUbicacionCentrada = false;

    // Lanzador de permisos: si el usuario acepta, activamos su ubicacion en el mapa
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            activarUbicacionUsuario();
                            moverCamaraAMiUbicacionForzada();
                        } else {
                            Toast.makeText(this, "De acuerdo, no miraremos tu ubicación", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa); 

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // El ViewModel sobrevive a los giros de pantalla, asi no recargamos datos innecesariamente
        viewModel = new ViewModelProvider(this).get(MapaViewModel.class);

        // FAB de ubicacion: centra el mapa en donde esta el usuario
        FloatingActionButton btnMiUbicacion = findViewById(R.id.btnMiUbicacion);
        if (btnMiUbicacion != null) {
            btnMiUbicacion.setOnClickListener(v -> {
                comprobarPermisoUbicacion();
                moverCamaraAMiUbicacionForzada();
            });
        }

        // FAB de ruta: abre la pantalla de calculo de ruta con gasolineras
        FloatingActionButton btnRuta = findViewById(R.id.btnRuta);
        /*
        if (btnRuta != null) {
            btnRuta.setOnClickListener(v ->
                startActivity(new Intent(MapaActivity.this, RutaActivity.class)));
        }
        */
         

        // Busca el mapa en el layout y cuando esté cargado llama a onMapReady para poder usarlo
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Cuando el ViewModel tenga las gasolineras listas, las cargamos en el cluster
        viewModel.getGasolineras().observe(this, gasolineras -> {
            cargarEnCluster(gasolineras);
        });

        viewModel.cargarGasolineras();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configura la interfaz del mapa: activa zoom y brújula, y quita el botón de ubicación (Uso uno configurado, el de serie tapaba info)
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // usamos nuestro propio FAB

        // Vista inicial centrada en Espana
        LatLng espana = new LatLng(40.4168, -3.7038);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(espana, 5.5f));

        comprobarPermisoUbicacion();

        // Configurar el cluster manager con nuestro renderer personalizado
        clusterManager = new ClusterManager<>(this, mMap);
        GasolineraClusterRenderer customRenderer = new GasolineraClusterRenderer(this, mMap, clusterManager);
        clusterManager.setRenderer(customRenderer);

        // El cluster necesita escuchar los eventos de camara para reagrupar al hacer zoom
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        // Click en un marcador individual: abrir pantalla de detalle
        clusterManager.setOnClusterItemClickListener(item -> {
            Gasolinera g = item.getGasolinera();
            
            Intent intent = new Intent(MapaActivity.this, DetalleActivity.class);
            intent.putExtra(DetalleActivity.EXTRA_ROTULO, g.getRotulo());
            intent.putExtra(DetalleActivity.EXTRA_DIRECCION, g.getDireccion());
            intent.putExtra(DetalleActivity.EXTRA_MUNICIPIO, g.getMunicipio());
            intent.putExtra(DetalleActivity.EXTRA_HORARIO, g.getHorario());
            intent.putExtra(DetalleActivity.EXTRA_LAT, g.getLat());
            intent.putExtra(DetalleActivity.EXTRA_LON, g.getLon());

            // Construimos el texto de precios solo con los combustibles que tengan valor
            StringBuilder precios = new StringBuilder();
            if(g.getP95() > 0) precios.append("✅ Sin Plomo 95:\t").append(g.getP95()).append(" €\n");
            if(g.getP98() > 0) precios.append("✅ Sin Plomo 98:\t").append(g.getP98()).append(" €\n");
            if(g.getDiesel() > 0) precios.append("✅ Diesel:\t").append(g.getDiesel()).append(" €\n");
            if(g.getDieselPremium() > 0) precios.append("✅ Diesel+:\t").append(g.getDieselPremium()).append(" €\n");
            if(g.getGlp() > 0) precios.append("✅ GLP:\t").append(g.getGlp()).append(" €\n");
            if(g.getGnc() > 0) precios.append("✅ GNC:\t").append(g.getGnc()).append(" €\n");

            String misPrecios = precios.toString().trim();
            if(misPrecios.isEmpty()) misPrecios = "Precios no disponibles";

            intent.putExtra(DetalleActivity.EXTRA_PRECIOS, misPrecios);
            startActivity(intent);

            return true; // true = consumimos el evento, evitamos comportamiento por defecto del mapa
        });

        // Si las gasolineras ya estaban cargadas antes de que el mapa estuviera listo, las metemos ahora
        List<Gasolinera> yaCargadas = viewModel.getGasolineras().getValue();
        if (yaCargadas != null && !yaCargadas.isEmpty()) {
            cargarEnCluster(yaCargadas);
        }
    }

    private void cargarEnCluster(List<Gasolinera> gasolineras) {
        if (gasolineras == null || mMap == null || clusterManager == null) return;

        clusterManager.clearItems();
        for (Gasolinera g : gasolineras) {
            // Saltamos las que no tienen coordenadas validas
            if (g.getLat() != 0.0 && g.getLon() != 0.0) {
                clusterManager.addItem(new GasolineraClusterItem(g));
            }
        }
        clusterManager.cluster();
        Log.d(TAG, "Cargadas " + gasolineras.size() + " gasolineras en el cluster");
    }

    private void comprobarPermisoUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            activarUbicacionUsuario();
        } else {
            // Si no tenemos permiso, lo pedimos con el launcher registrado en el campo de arriba
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void activarUbicacionUsuario() {
        if (mMap == null) return;

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // usamos nuestro propio FAB

        // Solo centramos la camara la primera vez que abrimos el mapa
        if (!primeraUbicacionCentrada) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null && mMap != null && !primeraUbicacionCentrada) {
                    LatLng miPosicion = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 14f));
                    primeraUbicacionCentrada = true;
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void moverCamaraAMiUbicacionForzada() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && mMap != null) {
                LatLng miPosicion = new LatLng(location.getLatitude(), location.getLongitude());
                // animateCamera crentra el mapa en las coordenadas marcadas
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 15f));
            }
        });
    }
}