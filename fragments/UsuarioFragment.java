package fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import daos.UsuarioDAO; // Importar tu DAO

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.diegodealba_proyectofinal.MisPublicaciones;
import com.example.diegodealba_proyectofinal.PreferenciasActivity;
import com.example.diegodealba_proyectofinal.R;

import de.hdodenhof.circleimageview.CircleImageView;
import pojos.Usuario;
import utilities.MunicipiosManager;

public class UsuarioFragment extends Fragment {

    // Vistas
    private CircleImageView imgPerfil;
    private ImageView btnEditarFoto;
    private EditText etNombre, etCorreo, etEdad;
    private TextView tvInfoTitulo;
    private Spinner spinnerNivel;
    private Button btnGuardar, btnAportaciones;
    private AutoCompleteTextView etLocalidad;

    private Usuario usuarioOriginal;
    private String nombreImagenActual;

    // Arrays de datos
    private final String[] misAvataresNombres = {"avatar_default", "avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5"};
    private final String[] listaNiveles = {"Principiante", "Intermedio", "Avanzado", "Experto"};

    public UsuarioFragment() {
    }

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuario, container, false);

        //vincular Vistas
        imgPerfil = view.findViewById(R.id.profile_image);
        btnEditarFoto = view.findViewById(R.id.imageView2);
        etNombre = view.findViewById(R.id.editTextText);
        etCorreo = view.findViewById(R.id.editTextText2);
        etLocalidad = view.findViewById(R.id.editTextText3);
        etEdad = view.findViewById(R.id.editTextText4);
        spinnerNivel = view.findViewById(R.id.spinner); // Vinculamos Spinner
        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnAportaciones= view.findViewById(R.id.btnAportaciones);
        tvInfoTitulo = view.findViewById(R.id.tvInfoTitulo);


        // configurar Spinner (Antes de cargar datos)
        ArrayAdapter<String> adapterNivel = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listaNiveles);
        spinnerNivel.setAdapter(adapterNivel);
        MunicipiosManager.configurarAutoComplete(requireContext(), etLocalidad);


        // cargar Datos Simulados
        cargarDatosReales();

        //configurar Listeners
        configurarListeners();

        ImageView btnAjustes = view.findViewById(R.id.btnAjustes);

        btnAportaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MisPublicaciones.class);
                startActivity(intent);
            }
        });

        btnAjustes.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PreferenciasActivity.class);
            startActivity(intent);
        });
        return view;

    }
    private void cargarDatosReales() {
        // instanciamos el DAO
        daos.UsuarioDAO dao = new daos.UsuarioDAO(requireContext());

        // buscamos el usuario usando el ID estático que guardamos al hacer Login
        usuarioOriginal = dao.obtenerUsuarioPorId(com.example.diegodealba_proyectofinal.MainActivity.USUARIO_ID_ACTUAL);

        // si el usuario existe, rellenamos los campos
        if (usuarioOriginal != null) {
            etNombre.setText(usuarioOriginal.getNombre());
            etCorreo.setText(usuarioOriginal.getCorreo());
            etLocalidad.setText(usuarioOriginal.getLocalidad());
            etEdad.setText(String.valueOf(usuarioOriginal.getEdad()));
            tvInfoTitulo.setText(usuarioOriginal.getNombre() + ", " + usuarioOriginal.getEdad());

            //crgar Avatar
            nombreImagenActual = usuarioOriginal.getAvatar();
            if (nombreImagenActual == null || nombreImagenActual.isEmpty()) {
                nombreImagenActual = "avatar_default";
            }
            actualizarImagenEnPantalla(nombreImagenActual);

            //cargar Spinner (Nivel)
            String nivelUser = usuarioOriginal.getNivel();
            if (nivelUser != null) {
                for (int i = 0; i < listaNiveles.length; i++) {
                    if (listaNiveles[i].equalsIgnoreCase(nivelUser)) {
                        spinnerNivel.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            //si entra aquí es que USUARIO_ID_ACTUAL es -1 o el usuario se borró
            Toast.makeText(getContext(), "Error: No se encontró el usuario logueado", Toast.LENGTH_SHORT).show();
        }

        //deshabilitar botón de guardar al inicio (solo se activa si tocas algo)
        btnGuardar.setEnabled(false);
        btnGuardar.setAlpha(0.5f);
    }

    private void configurarListeners() {
        // watcher para los EditText
        TextWatcher vigilante = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                verificarCambios();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etNombre.addTextChangedListener(vigilante);
        etCorreo.addTextChangedListener(vigilante);
        etLocalidad.addTextChangedListener(vigilante);
        etEdad.addTextChangedListener(vigilante);
        tvInfoTitulo.addTextChangedListener(vigilante);


        // listener para el Spinner
        spinnerNivel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                verificarCambios(); // Chequear cambios al seleccionar otro nivel
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //listener de imagen
        View.OnClickListener listenerImagen = v -> mostrarDialogoImagenesVisual();
        imgPerfil.setOnClickListener(listenerImagen);
        btnEditarFoto.setOnClickListener(listenerImagen);

        // botón Guardar
        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    //metodo para el dialogo de la imagen de perfil
    private void mostrarDialogoImagenesVisual() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Selecciona tu Avatar");

        // usamos nuestro adaptador personalizado
        AvatarAdapter adapter = new AvatarAdapter(getContext(), misAvataresNombres);

        builder.setAdapter(adapter, (dialog, which) -> {
            nombreImagenActual = misAvataresNombres[which];
            actualizarImagenEnPantalla(nombreImagenActual);
            verificarCambios();
        });

        builder.show();
    }

    //actulizamos la imagen del usuario
    private void actualizarImagenEnPantalla(String nombreDrawable) {
        int resId = getResources().getIdentifier(nombreDrawable, "drawable", requireActivity().getPackageName());
        if (resId != 0) imgPerfil.setImageResource(resId);
        else imgPerfil.setImageResource(R.drawable.avatar_default);
    }

    private void verificarCambios() {
        boolean hayCambios = false;

        // Textos
        if (!etNombre.getText().toString().equals(usuarioOriginal.getNombre())) hayCambios = true;
        if (!etCorreo.getText().toString().equals(usuarioOriginal.getCorreo())) hayCambios = true;
        if (!etLocalidad.getText().toString().equals(usuarioOriginal.getLocalidad())) hayCambios = true;
        if (!etEdad.getText().toString().equals(String.valueOf(usuarioOriginal.getEdad()))) hayCambios = true;

        // Imagen
        if (!nombreImagenActual.equals(usuarioOriginal.getAvatar())) hayCambios = true;

        // Spinner (Nivel)
        String nivelSeleccionado = spinnerNivel.getSelectedItem().toString();

        if (usuarioOriginal.getNivel() != null && !nivelSeleccionado.equals(usuarioOriginal.getNivel())) {
            hayCambios = true;
        }

        if (hayCambios) {
            btnGuardar.setEnabled(true);
            btnGuardar.setAlpha(1.0f);
        } else {
            btnGuardar.setEnabled(false);
            btnGuardar.setAlpha(0.5f);
        }
    }
    private void guardarCambios() {
        if (usuarioOriginal == null) return;

        // xctualizamos el objeto en memoria con lo que escribió el usuario
        usuarioOriginal.setNombre(etNombre.getText().toString());
        usuarioOriginal.setCorreo(etCorreo.getText().toString());
        usuarioOriginal.setLocalidad(etLocalidad.getText().toString());
        usuarioOriginal.setAvatar(nombreImagenActual); // El avatar seleccionado
        usuarioOriginal.setNivel(spinnerNivel.getSelectedItem().toString());

        try {
            usuarioOriginal.setEdad(Integer.parseInt(etEdad.getText().toString()));
        } catch (NumberFormatException e) {
            usuarioOriginal.setEdad(0);
        }

        // XONECTAMOS CON LA BASE DE DATOS
        UsuarioDAO dao = new UsuarioDAO(requireContext());

        boolean exito = dao.actualizarUsuario(usuarioOriginal);

        if (exito) {
            Toast.makeText(getContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
            // colvemos a deshabilitar el botón porque ya está guardado
            btnGuardar.setEnabled(false);
            btnGuardar.setAlpha(0.5f);
            //colvemos a verificar para asegurar que el estado es consistente
            verificarCambios();
        } else {
            Toast.makeText(getContext(), "Error al guardar en base de datos", Toast.LENGTH_SHORT).show();
        }
    }

    // --- CLASE INTERNA: ADAPTADOR PARA EL SELECTOR VISUAL ---
    private class AvatarAdapter extends ArrayAdapter<String> {

        public AvatarAdapter(Context context, String[] avatares) {
            super(context, 0, avatares);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_avatar_lista, parent, false);
            }

            //satos actuales
            String nombreAvatar = getItem(position);

            //referencias
            ImageView img = convertView.findViewById(R.id.img_avatar_item);
            TextView txt = convertView.findViewById(R.id.txt_avatar_nombre);

            //setear Texto
            txt.setText(nombreAvatar);

            //setear Imagen Dinámicamente
            int resId = getResources().getIdentifier(nombreAvatar, "drawable", requireActivity().getPackageName());
            if (resId != 0) {
                img.setImageResource(resId);
            } else {
                img.setImageResource(R.drawable.avatar_default);
            }

            return convertView;
        }
    }
}