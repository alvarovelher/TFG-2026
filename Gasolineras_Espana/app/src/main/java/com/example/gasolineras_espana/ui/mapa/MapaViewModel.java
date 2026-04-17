package com.example.gasolineras_espana.ui.mapa;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gasolineras_espana.geojson.GeoJsonDownloader;
import com.example.gasolineras_espana.model.GeoJsonData;
import com.example.gasolineras_espana.model.Gasolinera;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * ViewModel encargado de obtener los datos de las gasolineras desde internet (GitHub)
 * y mantenerlos en memoria mientras la aplicacion esta activa.
 *
 * Se utiliza para evitar perder los datos en cambios de configuracion
 * (como rotacion de pantalla) y para separar la logica de la UI.
 */
public class MapaViewModel extends ViewModel {

    // URL del GeoJSON alojado en GitHub
    private static final String URL_GEOJSON = "https://raw.githubusercontent.com/MigMoy93/gasolineras-espana/main/gasolineras.geojson";

    // Contiene la lista de gasolineras y permite que la UI observe cambios
    private final MutableLiveData<List<Gasolinera>> gasolinerasLiveData = new MutableLiveData<>();

    // Indica si se esta realizando la carga de datos
    private final MutableLiveData<Boolean> cargandoLiveData = new MutableLiveData<>(false);

    // Executor para ejecutar tareas en segundo plano (hilo secundario)
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Devuelve la lista de gasolineras como LiveData (solo lectura para la UI)
    public LiveData<List<Gasolinera>> getGasolineras() {
        return gasolinerasLiveData;
    }

    // Devuelve el estado de carga para mostrar indicadores en la UI
    public LiveData<Boolean> getCargando() {
        return cargandoLiveData;
    }

    // Metodo que descarga y procesa el GeoJSON
    public void cargarGasolineras() {

        // Si ya hay datos cargados, no volvemos a descargarlos
        if (gasolinerasLiveData.getValue() != null && !gasolinerasLiveData.getValue().isEmpty()) {
            return;
        }

        // Indicamos que empieza la carga
        cargandoLiveData.setValue(true);

        // Ejecutamos la tarea en segundo plano
        executorService.execute(() -> {
            try {
                // Descargamos y parseamos el GeoJSON
                GeoJsonDownloader downloader = new GeoJsonDownloader();
                GeoJsonData data = downloader.descargarYParsear(URL_GEOJSON);

                // Actualizamos los datos (postValue porque estamos fuera del hilo principal)
                gasolinerasLiveData.postValue(data.getGasolineras());

            } catch (Exception e) {
                // Mostramos error en log
                Log.e("MAPA_VM", "Error descargando geojson", e);

            } finally {
                // Indicamos que la carga ha terminado
                cargandoLiveData.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Cerramos el executor para liberar recursos cuando el ViewModel se destruye
        executorService.shutdown();
    }
}