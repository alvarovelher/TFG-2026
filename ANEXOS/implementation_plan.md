# Plan de Mejora: Gasolineras España

Este plan detalla las acciones para corregir los problemas detectados (rendimiento, memoria y ciclo de vida) e implementar las funcionalidades solicitadas por el usuario, utilizando Java sencillo y librerías estándar.

## 1. Arquitectura y Memoria (Core)

Para evitar cierres inesperados por `OutOfMemoryError` y fugas de memoria al girar la pantalla:

*   **`MapaViewModel.java` [NEW]:** Se creará un ViewModel muy básico usando `androidx.lifecycle.ViewModel` y `LiveData`. Se encargará de ejecutar el hilo de descarga usando un `ExecutorService`. Al terminar, publicará la lista en un `LiveData`.
*   **`GeoJsonDownloader.java` [MODIFY]:** En lugar de crear un enorme `String` con todo el archivo y saturar la RAM, ahora devolverá un `InputStream` directo de internet.
*   **`GeoJsonParser.java` [MODIFY]:** Dejará de usar `org.json.JSONObject`. En su lugar, utilizará `android.util.JsonReader` (incluido en Android de base, sin añadir librerías extra). Leerá las gasolineras "como un grifo de agua", línea a línea, construyendo la lista sin cargar todo en memoria a la vez.

## 2. Rendimiento del Mapa (ClusterManager)

*   **`MapaActivity.java` [MODIFY]:** 
    *   Se eliminará por completo la lógica ineficiente de `actualizarClustersZonaVisible()`.
    *   Una vez que el `ViewModel` entregue las gasolineras, se llamará a `clusterManager.addItems(...)` **una única vez** y se delegará todo el renderizado fluido a Google Maps.
    *   Se añadirá un "listener" a la cámara para re-renderizar los clústeres cuando el mapa cruce el umbral de zoom (para cambiar entre modo "solo logo" e "icono + precio").


## 3. Detalles y Estructuración

*   **`BrandUtils.java` [MODIFY]:** Tu código actual es perfecto. Sólo añadiré documentación estándar en formato JavaDoc y me aseguraré de que encaje fácil con el nuevo Renderer tal como pides.
*   **`DetalleActivity.java` [NEW]:** Crearemos una actividad dedicada para ver toda la info:
    *   Un XML sencillo (`activity_detalle.xml`) para pintar: Rótulo, Dirección, Municipio, Horario, y lista de todos los combustibles disponibles.
    *   Tendrá un botón o banner interactivo para lanzar un `Intent` estándar (`google.navigation:q=lat,lon`) que abrirá automáticamente Google Maps Navigación para "Cómo llegar" al punto exacto.
*   **Apertura del Detalle:** En `MapaActivity`, al hacer click (sea al ítem en sí o al info window de la gasolinera), lanzaremos un `Intent` pasando los datos básicos (o el ID) hacia el `DetalleActivity`, para ver toda la información cómodamente.

## Pasos Próximos
1. Arreglar core de datos, hilos de descarga y `JsonReader`.
2. Crear `GasolineraClusterRenderer` e integrarlo en `MapaActivity` (solucionando además el gran bug de rendimiento).

Dime si todo este planteamiento encaja con lo que tienes en mente.
