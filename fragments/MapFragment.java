package fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.diegodealba_proyectofinal.MainActivity;
import com.example.diegodealba_proyectofinal.R;
import com.example.diegodealba_proyectofinal.detallesSpotActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import daos.ImagenSpotDAO;
import daos.SpotDAO;
import daos.UsuarioDAO;
import pojos.Spot;
import pojos.Usuario;
import utilities.BitmapUtils;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static Map<String, Spot> spotMap = new HashMap<>();
    private FusedLocationProviderClient fusedLocationClient;
    private FloatingActionButton fab;
    private EditText inputTitulo ;
    private EditText inputDescripcion;
    private ChipGroup grupoEtiquetas;
    private RatingBar barraValoracion;
    private BottomNavigationView bottomNavigationView;

    private ImageView previewImagenRef; //referencia para actualizar la vista previa
    private Bitmap imagenSeleccionadaBitmap = null; //para guardar temporalmente la foto elegida y poer previsulizaar

    public MapFragment() {
        super(com.example.diegodealba_proyectofinal.R.layout.fragment_map);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //para centrar la ubicacion en el usuaio
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

//mostrar mapa en el fragment
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(com.example.diegodealba_proyectofinal.R.id.mapView);

        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        fab = view.findViewById(com.example.diegodealba_proyectofinal.R.id.fabAddSpot);

//metodo para el boton +
        fab.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Mantén pulsado el mapa para colocar un nuevo spot",
                    Toast.LENGTH_LONG).show();

            fab.setVisibility(View.GONE);
           // bottomNavigationView.setVisibility(View.GONE);
        });

        fab.setVisibility(View.VISIBLE);
        //bottomNavigationView.setVisibility(View.VISIBLE);
    }

    //mettodos para cargar los spots de bbdd
    private void cargarSpotsDesdeBD() {
        mMap.clear(); //impiar mapa por si es actualizar
        SpotDAO dao = new SpotDAO(requireContext());
        ArrayList<Spot> spotsBD = dao.obtenerTodosLosSpots();

        for (Spot s : spotsBD) {
            spotMap.put(String.valueOf(s.getIdDb()), s); //actualizar mapa
            addSpotMarker(s);
        }
    }

    //metodo para cargar el mapa
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //permiso para la localizacion
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        //activar la localizacion en el mapa
        mMap.setMyLocationEnabled(true);
        mMap.setPadding(0, 1500, 0, 0);

        //centrar la ubicacion en el usuaio
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng ubicacion = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16));
            }
        });
//estilo del mapa : sin los ppuntos de interes , para q no salgan tiendas , museos y demas
        String styleJson = "[ { \"featureType\": \"poi\", \"elementType\": \"all\", \"stylers\": [ { \"visibility\": \"off\" } ] } ]";
        mMap.setMapStyle(new MapStyleOptions(styleJson));

        //cargamos los spots
        cargarSpotsDesdeBD();

        //click en los marker
        mMap.setOnMarkerClickListener(marker -> {
            Spot s = spotMap.get(marker.getTag());
            mostrarBottomSheet(s);
            return true;
        });

//puulsacion laga en el mapa
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                mostrarFormularioCrearSpot(latLng);
                fab.setVisibility(View.VISIBLE);
                //bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void mostrarFormularioCrearSpot(LatLng latLng) {

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(com.example.diegodealba_proyectofinal.R.layout.bottom_sheet_formulario, null);
        inputTitulo = view.findViewById(R.id.inputTitulo);
        inputDescripcion = view.findViewById(R.id.inputDescripcion);
        grupoEtiquetas = view.findViewById(R.id.grupoEtiquetas);
        barraValoracion = view.findViewById(R.id.barraValoracion);

        ImageView previewImagen = view.findViewById(R.id.previewImagen);
        this.previewImagenRef = previewImagen; //guardamos referencia global

        ChipGroup grupoEtiquetas = view.findViewById(R.id.grupoEtiquetas);
        Chip chipPersonalizada = view.findViewById(R.id.chipPersonalizada);
        //para añadir etiquetas personlizadas
        chipPersonalizada.setOnClickListener(v -> {
            mostrarDialogoNuevaEtiqueta(grupoEtiquetas);
        });

        previewImagen.setOnClickListener(v -> {
            //abrir galería
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        Button botonCancelar = view.findViewById(com.example.diegodealba_proyectofinal.R.id.botonCancelar);
        Button botonPublicar = view.findViewById(com.example.diegodealba_proyectofinal.R.id.botonPublicar);

        botonCancelar.setOnClickListener(v -> dialog.dismiss());

        botonPublicar.setOnClickListener(v -> {
            //solo cerramos si pasa las validaciones dentro de crearSpot
            if (!inputTitulo.getText().toString().trim().isEmpty() &&
                    !inputDescripcion.getText().toString().trim().isEmpty() &&
                    !grupoEtiquetas.getCheckedChipIds().isEmpty()) {

                crearSpot(view, latLng);
                dialog.dismiss();
            } else {
                // Llamamos a crearSpot para que muestre los Toasts de error específicos
                crearSpot(view, latLng);
            }
        });

        dialog.setContentView(view);

            // --- BLOQUE ARREGLAR EL SCROLL ---
        dialog.setOnShowListener(dialogInterface -> {

            // obtenemos la referencia al propio diálogo
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;

            // bBuscamos el contenedor principal del BottomSheet (es un FrameLayout interno de Android)
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                // inicializamos el comportamiento (Behavior)
                BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(bottomSheet);

                //forzamos a que se abra totalmente expandido
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true); // Evita que se quede a medio abrir

                // forzamos la altura del contenedor a 'MATCH_PARENT'
                // Esto es para q NestedScrollView (que tiene altura 0dp) sepa cuánto expandirse.
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = getResources().getDisplayMetrics().heightPixels; // atura de pantalla completa
                bottomSheet.setLayoutParams(layoutParams);
            }
        });

        // Transparencia del fondo
        if (dialog.getWindow() != null) {
            dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet)
                    .setBackgroundResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void mostrarDialogoNuevaEtiqueta(ChipGroup grupoEtiquetas) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Nueva etiqueta");

        //Input de texto
        final EditText input = new EditText(requireContext());
        input.setHint("Ej: Escaleras 5 peldaños");
        builder.setView(input);

        //Botones
        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String texto = input.getText().toString().trim();
            if (!texto.isEmpty()) {
                anadirChipAlGrupo(grupoEtiquetas, texto);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void anadirChipAlGrupo(ChipGroup grupo, String texto) {
        Chip chip = new Chip(requireContext());
        chip.setText(texto);

        // Configuración importante para que funcione como los demás
        chip.setCheckable(true);
        chip.setChecked(true); // Lo marcamos automáticamente al crearlo
        chip.setClickable(true);

        // Sin borde
        chip.setChipStrokeWidth(0);

        // Color de fondo
        chip.setChipBackgroundColorResource(R.color.chip_backgroud_color);

        // Texto negro
        chip.setTextColor(getResources().getColor(android.R.color.black));
        chip.setTextSize(12);

        // Añadimos el chip al grupo antes de la etiqueta personalizada
        int indice = grupo.getChildCount() - 1;
        grupo.addView(chip, indice);
    }

    private void crearSpot(View view, LatLng latLng) {

        // EXTRAER VALORES
        String titulo = inputTitulo.getText().toString().trim();
        String descripcion = inputDescripcion.getText().toString().trim();
        float valoracion = barraValoracion.getRating();

        //obtener IDs de los chips seleccionados
        List<Integer> idsSeleccionados = grupoEtiquetas.getCheckedChipIds();

        // VALIDACIONES
        // Validar Título
        if (titulo.isEmpty()) {
            Toast.makeText(getContext(), "El título es obligatorio", Toast.LENGTH_SHORT).show();
            inputTitulo.setError("Campo obligatorio");
            return;
        }

        // Validar Descripción
        if (descripcion.isEmpty()) {
            Toast.makeText(getContext(), "La descripción es obligatoria", Toast.LENGTH_SHORT).show();
            inputDescripcion.setError("Campo obligatorio");
            return;
        }

        // Validar Etiquetas (Mínimo una seleccionada)
        if (idsSeleccionados.isEmpty()) {
            Toast.makeText(getContext(), "Selecciona al menos una etiqueta para el spot", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar Valoración (Que no sea 0)
        if (valoracion == 0) {
            Toast.makeText(getContext(), "Por favor, añade una valoración con las estrellas", Toast.LENGTH_SHORT).show();
            return;
        }

        // PROCESAR ETIQUETAS
        List<String> etiquetasSeleccionadas = new ArrayList<>();
        for (int chipId : idsSeleccionados) {
            Chip chip = grupoEtiquetas.findViewById(chipId);
            if (chip != null) {
                etiquetasSeleccionadas.add(chip.getText().toString());
            }
        }

        // GUARDAR IMAGEN (Opcional)
        int idImagenGuardada = -1;
        if (imagenSeleccionadaBitmap != null) {
            ImagenSpotDAO imgDao = new ImagenSpotDAO(requireContext());
            byte[] imgBytes = BitmapUtils.bitmapToBytes(imagenSeleccionadaBitmap);
            idImagenGuardada = (int) imgDao.insertarImagen(imgBytes);
        }

        //GUARDAR SPOT EN SQLITE
        SpotDAO spotDao = new SpotDAO(requireContext());
        int usuarioId = MainActivity.USUARIO_ID_ACTUAL;

        Spot nuevoSpot = new Spot(
                0,
                titulo,
                descripcion,
                (float) latLng.latitude,
                (float) latLng.longitude,
                valoracion,
                etiquetasSeleccionadas,
                usuarioId,
                idImagenGuardada
        );

        if (spotDao.insertarSpot(nuevoSpot)) {
            Toast.makeText(getContext(), "¡Spot creado con éxito!", Toast.LENGTH_SHORT).show();
            cargarSpotsDesdeBD(); //recargar los marcadores en el mapa

            // Limpiar el bitmap
            imagenSeleccionadaBitmap = null;


        } else {
            Toast.makeText(getContext(), "Error crítico al guardar en la base de datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSpotMarker(Spot spot) {
        LatLng pos = new LatLng(spot.getLatitud(), spot.getLongitud());
        Marker marker = mMap.addMarker(
                new MarkerOptions()
                        .position(pos)
                        .title(spot.getTitulo())
                        .icon(BitmapDescriptorFactory.fromResource(com.example.diegodealba_proyectofinal.R.drawable.marker_loc))
        );

        marker.setTag(String.valueOf(spot.getIdDb()));
    }
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);

                        // Cargar imagen
                        Bitmap original = BitmapFactory.decodeStream(inputStream);

                        imagenSeleccionadaBitmap = original;

                        if (previewImagenRef != null) {
                            previewImagenRef.setImageBitmap(imagenSeleccionadaBitmap);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private void mostrarBottomSheet(Spot spot) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(com.example.diegodealba_proyectofinal.R.layout.bottom_sheet_spot, null);

        TextView nivel = view.findViewById(com.example.diegodealba_proyectofinal.R.id.tvNivel);
        TextView titulo = view.findViewById(com.example.diegodealba_proyectofinal.R.id.tituloSpot);
        TextView descripcion = view.findViewById(com.example.diegodealba_proyectofinal.R.id.textoDescripcion);
        TextView usuario = view.findViewById(com.example.diegodealba_proyectofinal.R.id.usuarioSpot);
        TextView valoracion = view.findViewById(com.example.diegodealba_proyectofinal.R.id.valoracionSpot);
        ChipGroup group = view.findViewById(com.example.diegodealba_proyectofinal.R.id.cg_etiquetas);
        Button verMas = view.findViewById(com.example.diegodealba_proyectofinal.R.id.btnVerMas);
        ImageView imgProfile = view.findViewById(R.id.profile_image);


        titulo.setText(spot.getTitulo());
        descripcion.setText(spot.getDescripcion());

        UsuarioDAO dao = new UsuarioDAO(requireContext());
        Usuario u = dao.obtenerUsuarioPorId(spot.getUsuarioId());

        //verificamos que el usuario no sea null
        if (u != null) {
            usuario.setText(u.getNombre());
            nivel.setText("Nivel "+u.getNivel());

        //obtenemos el nombre del avatar (ej: "avatar_1")
            String avatarName = u.getAvatar();

        // buscamos el identificador  en la carpeta 'drawable'
        // usamos requireContext() porque estamos en un Fragment
            int resId = getResources().getIdentifier(avatarName, "drawable", requireContext().getPackageName());

        // verificamos si existe la imagen. Si resId es 0, es que no se encontró.
            if (resId != 0) {
                imgProfile.setImageResource(resId);
            } else {
                // Si no tiene avatar o el nombre está mal, cargamos el default
                imgProfile.setImageResource(R.drawable.avatar_default);
            }
        } else {
            usuario.setText("Desconocido");
        }

        valoracion.setText(String.valueOf(spot.getValoracion()));


        for (String e : spot.getEtiquetas()) {
            Chip chip = new Chip(requireContext());
            chip.setText(e);

            // Sin borde
            chip.setChipStrokeWidth(1);

            // Color de fondo
            chip.setChipBackgroundColorResource(R.color.chip_backgroud_color);

            // Texto negro
            chip.setTextColor(getResources().getColor(android.R.color.black));
            chip.setTextSize(11);


            group.addView(chip);
        }


        verMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), detallesSpotActivity.class);
                intent.putExtra("spot",  spot);

                startActivity(intent);
            }
        });

        dialog.setContentView(view);
        dialog.show();

    }

}
