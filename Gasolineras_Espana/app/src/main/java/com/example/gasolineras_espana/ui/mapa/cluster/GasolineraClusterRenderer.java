package com.example.gasolineras_espana.ui.mapa.cluster;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.gasolineras_espana.R;
import com.example.gasolineras_espana.model.Gasolinera;
import com.example.gasolineras_espana.utils.BrandUtils;

import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class GasolineraClusterRenderer extends DefaultClusterRenderer<GasolineraClusterItem> {

    // Umbral de accion: mas grande que este numero indica que estamos cerca "ZOOM IN"
    public static final float ZOOM_THRESHOLD = 11.5f;

    private final Context context;
    private final IconGenerator iconGenerator;
    private final View markerView;
    private final ImageView imgLogo;
    private final TextView txtPrecios;

    // Tenemos que guardar la referencia al mapa para preguntarle en todo momento el zoom
    private final GoogleMap map;

    // Cache en memoria para guardar iconos de marcadores ya generados y evitar recalcularlos constantemente
    private final LruCache<String, BitmapDescriptor> markerCache = new LruCache<>(300);

    public GasolineraClusterRenderer(Context context, GoogleMap map, ClusterManager<GasolineraClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        this.map = map;

        // Preparamos nuestro generador de imagenes virtuales (IconGenerator)
        this.iconGenerator = new IconGenerator(context);
        
        // Lo ponemos transparente por defecto para que no dibuje texturas feas de fondo globo
        this.iconGenerator.setBackground(null);

        // Inflamos (abrimos) nuestro XML UNA SOLA VEZ para reciclarlo y no saturar la CPU
        this.markerView = LayoutInflater.from(context).inflate(R.layout.item_marker_precio, null);
        this.imgLogo = markerView.findViewById(R.id.imgLogo);
        this.txtPrecios = markerView.findViewById(R.id.txtPreciosMarker);

        // Le pegamos nuestro XML al IconGenerator
        this.iconGenerator.setContentView(markerView);
        
        // Configuramos para que no agrupe en clúster si hay menos de 5 muy cerquita
        setMinClusterSize(5); 
    }

    // Genera el icono del marcador según el zoom: completo con precios si estás cerca o logo pequeño si estás lejos, reutilizando caché para mejorar rendimiento
    private BitmapDescriptor getDynamicMarkerIcon(Gasolinera gas, int logoRes, float zoomActual) {
        if (zoomActual >= ZOOM_THRESHOLD) {
            String cacheKey = "full_" + gas.getId(); // Clave unica por gasolinera
            BitmapDescriptor cachedIcon = markerCache.get(cacheKey);

            if (cachedIcon != null) {
                // Si ya existe en cache, lo reutilizamos
                return cachedIcon;
            }

            // FALLO DE CACHÉ: Nos toca dibujar el layout complejo sincrónicamente.
            this.imgLogo.setImageResource(logoRes);
            String precio95 = gas.getP95() > 0 ? String.valueOf(gas.getP95()) : "--";
            String precioD = gas.getDiesel() > 0 ? String.valueOf(gas.getDiesel()) : "--";
            this.txtPrecios.setText("95: " + precio95 + "\nD: " + precioD);

            Bitmap iconBitmap = iconGenerator.makeIcon();
            BitmapDescriptor nuevoIcono = BitmapDescriptorFactory.fromBitmap(iconBitmap);

            // Guardamos el resultado en cache para reutilizarlo cuando s emueva la pantalla
            markerCache.put(cacheKey, nuevoIcono);
            return nuevoIcono;

        } else {
            // ZOOM LEJANO -> usamos un icono mas pequeño (solo logo)
            String cacheKey = "small_" + logoRes; // Compartido por todas las gasolineras de la misma marca
            BitmapDescriptor cachedIcon = markerCache.get(cacheKey);

            if (cachedIcon != null) {
                return cachedIcon;             // Reutilizamos si ya esta creado
            }

            // Calculamos el tamaño en px segun la densidad de pantalla
            int sizePx = (int) (32 * context.getResources().getDisplayMetrics().density);

            // Creamos un bitmap (lienzo) y dibujamos el logo reducido
            Drawable drawable = androidx.core.content.ContextCompat.getDrawable(context, logoRes);
            Bitmap smallBitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(smallBitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            BitmapDescriptor nuevoIconoPequeño = BitmapDescriptorFactory.fromBitmap(smallBitmap);

            // Guardamos en cache para no volver a generarlo en el resto de gasolineras
            markerCache.put(cacheKey, nuevoIconoPequeño);
            return nuevoIconoPequeño;
        }
    }

    // Este metodo se llama para dibujar un ítem individual ANTES de ponerlo en el mapa (no un grupo)
    @Override
    protected void onBeforeClusterItemRendered(@NonNull GasolineraClusterItem item, @NonNull MarkerOptions markerOptions) {
        Gasolinera gas = item.getGasolinera();
        int logoRes = BrandUtils.getLogoResId(gas.getRotulo());
        float zoomActual = map.getCameraPosition().zoom;

        // Generamos el icono segun el zoom y los datos de la gasolinera
        markerOptions.icon(getDynamicMarkerIcon(gas, logoRes, zoomActual));

        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    // Si la posición ya existía pero DEBE ACTUALIZARSE visualmente porque movimos el zoom del mapa
    @Override
    protected void onClusterItemUpdated(@NonNull GasolineraClusterItem item, @NonNull com.google.android.gms.maps.model.Marker marker) {
        Gasolinera gas = item.getGasolinera();
        int logoRes = BrandUtils.getLogoResId(gas.getRotulo());
        float zoomActual = map.getCameraPosition().zoom;

        // Actualizamos el icono reutilizando el mismo metodo
        marker.setIcon(getDynamicMarkerIcon(gas, logoRes, zoomActual));
    }
}
