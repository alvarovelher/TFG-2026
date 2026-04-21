package fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diegodealba_proyectofinal.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;

import adaptadores.QuedadasAdaptador;
import daos.QuedadaDAO;
import pojos.Quedada;
import utilities.MunicipiosManager;

public class QuedadasFragment extends Fragment {

    private RecyclerView recyclerView;
    private QuedadasAdaptador adapter;
    private ArrayList<Quedada> listaQuedadas;
    private FloatingActionButton fabAddQuedada;
    private EditText etBusqueda;
    private ImageView btnBuscar;
    private ImageView btnCalendario;

    public QuedadasFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_quedadas, container, false);

        etBusqueda = view.findViewById(R.id.etBusquedaQuedada);
        btnBuscar = view.findViewById(R.id.btnBuscarQuedada);
        btnCalendario = view.findViewById(R.id.btnFiltroFecha);

        recyclerView = view.findViewById(R.id.recyclerViewQuedadas);
        fabAddQuedada = view.findViewById(R.id.fabAddSpot);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar lista vacía
        listaQuedadas = new ArrayList<>();
        adapter = new QuedadasAdaptador(listaQuedadas);
        recyclerView.setAdapter(adapter);

        // CARGAR DESDE BD
        cargarQuedadasDesdeBD();
        btnBuscar.setOnClickListener(v -> {
            String texto = etBusqueda.getText().toString().trim();
            if (texto.isEmpty()) {
                // Si borran el texto y dan a buscar, mostramos todas de nuevo
                cargarQuedadasDesdeBD();

            } else {
                realizarBusqueda(texto);
            }
        });
        // LISTENER CALENDARIO
        btnCalendario.setOnClickListener(v -> mostrarSelectorFecha());

        fabAddQuedada.setOnClickListener(v -> mostrarFormularioCrearQuedada());

        return view;
    }
//BUSCAMOS EN BBDD Y GUARRDAMOS EN UNA LISTA
    private void realizarBusqueda(String texto) {
        daos.QuedadaDAO dao = new daos.QuedadaDAO(requireContext());
        ArrayList<pojos.Quedada> resultados = dao.buscarQuedadas(texto);

        // Actualizamos la lista del adaptador
        listaQuedadas.clear();
        listaQuedadas.addAll(resultados);
        adapter.notifyDataSetChanged();

        if (listaQuedadas.isEmpty()) {
            Toast.makeText(getContext(), "No se encontraron quedadas", Toast.LENGTH_SHORT).show();
        }
    }
    private void mostrarSelectorFecha() {
        //otener fecha actual para iniciar el calendario
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    //formatear la fecha igual que como se guarda en BD (dd/MM/yyyy)
                    // %02d asegura que ponga el cero delante (ej: 05/04/2026)
                    String fechaSeleccionada = String.format("%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year1);

                    //poner la fecha en el buscador para que el usuario vea qué ha filtrado
                    etBusqueda.setText(fechaSeleccionada);

                    //ejecutar búsqueda
                    filtrarPorFecha(fechaSeleccionada);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void filtrarPorFecha(String fecha) {
        daos.QuedadaDAO dao = new daos.QuedadaDAO(requireContext());
        ArrayList<pojos.Quedada> resultados = dao.buscarQuedadasPorFecha(fecha);

        //actualizar Recycler
        listaQuedadas.clear();
        listaQuedadas.addAll(resultados);
        adapter.notifyDataSetChanged();

        if (listaQuedadas.isEmpty()) {
            Toast.makeText(getContext(), "No hay quedadas para el " + fecha, Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarQuedadasDesdeBD() {
        QuedadaDAO dao = new QuedadaDAO(requireContext());
        listaQuedadas.clear();
        listaQuedadas.addAll(dao.obtenerTodas());
        adapter.notifyDataSetChanged();
    }

    // ==============================
    // BottomSheet crear quedada
    // ==============================
    private void mostrarFormularioCrearQuedada() {

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater()
                .inflate(R.layout.bottom_sheet_quedada, null);

        EditText inputTitulo = view.findViewById(R.id.inputTitulo);
        EditText inputDescripcion = view.findViewById(R.id.inputDescripcion);
        AutoCompleteTextView inputLocalidad = view.findViewById(R.id.inputLocalidad);
        EditText inputLugar = view.findViewById(R.id.inputLugar);
        EditText inputFecha = view.findViewById(R.id.inputFecha);
        EditText inputHora = view.findViewById(R.id.inputHora);

        //configuramos el autocomplete text con los municipios
        MunicipiosManager.configurarAutoComplete(requireContext(), inputLocalidad);

        Calendar calendario = Calendar.getInstance();

        inputFecha.setOnClickListener(v -> {

            int year = calendario.get(Calendar.YEAR);
            int month = calendario.get(Calendar.MONTH);
            int day = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    requireContext(),
                    (view1, y, m, d) -> {
                        String fecha = d + "/" + (m + 1) + "/" + y;
                        inputFecha.setText(fecha);
                    },
                    year, month, day
            );

            datePicker.show();
        });

        inputHora.setOnClickListener(v -> {

            int hour = calendario.get(Calendar.HOUR_OF_DAY);
            int minute = calendario.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (view12, h, m) -> {
                        String hora = String.format("%02d:%02d", h, m);
                        inputHora.setText(hora);
                    },
                    hour,
                    minute,
                    true
            );

            timePicker.show();
        });



        Button botonCancelar = view.findViewById(R.id.botonCancelar);
        Button botonPublicar = view.findViewById(R.id.botonPublicar);

        botonCancelar.setOnClickListener(v -> dialog.dismiss());

        botonPublicar.setOnClickListener(v -> {

            String titulo = inputTitulo.getText().toString().trim();
            String descripcion = inputDescripcion.getText().toString().trim();
            String localidad = inputLocalidad.getText().toString().trim();
            String lugar = inputLugar.getText().toString().trim();
            String fecha = inputFecha.getText().toString().trim();
            String hora = inputHora.getText().toString().trim();

            //calidamos quedada
            if (titulo.isEmpty() || descripcion.isEmpty()
                    || fecha.isEmpty() || hora.isEmpty() || localidad.isEmpty() || lugar.isEmpty()) {
                Toast.makeText(getContext(),
                        "Rellena todos los campos ",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            //creamos con el id del usuaio logeado
            Quedada nuevaQuedada = new Quedada(
                    localidad,
                    titulo,
                    descripcion,
                    lugar,
                    fecha,
                    hora,
                    com.example.diegodealba_proyectofinal.MainActivity.USUARIO_ID_ACTUAL
            );

            //guardar en BD
            QuedadaDAO dao = new QuedadaDAO(requireContext());
            if (dao.insertarQuedada(nuevaQuedada)) {
                Toast.makeText(getContext(), "Quedada creada", Toast.LENGTH_SHORT).show();
                cargarQuedadasDesdeBD(); //eecargar lista
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }
}
