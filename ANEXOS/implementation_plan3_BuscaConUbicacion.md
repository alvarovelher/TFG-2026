# Añadir "Mi ubicación" en la búsqueda de rutas

¡No es nada difícil! Es una funcionalidad muy común y bastante fácil de implementar usando las herramientas que ya tienes en `MapaActivity`.

## Resumen de la solución

Vamos a añadir un pequeño icono de "GPS/Localización" dentro del cuadro de texto del origen (`etOrigen`). Cuando el usuario lo toque, el texto cambiará automáticamente a "Mi ubicación". 
Luego, al darle a "Calcular ruta", interceptamos si pone "Mi ubicación", obtenemos tus coordenadas por GPS, ¡y trazamos la ruta desde donde estás!

## Cambios Propuestos

### Componente XML
#### [MODIFY] `activity_ruta.xml`
- Añadir la propiedad `app:endIconMode="custom"` y `app:endIconDrawable` en el `TextInputLayout` del origen. Esto colocará un pequeño botón de la mirilla del GPS dentro del cuadro de texto, al lado derecho.

### Componente Java
#### [MODIFY] `RutaActivity.java`
- Añadir el `FusedLocationProviderClient` y `ActivityResultLauncher` (exactamente igual que hiciste muy bien en `MapaActivity` para pedir permisos).
- Al pulsar el nuevo botón del GPS en la interfaz, rellenar el texto origen con "Mi ubicación".
- En la función `buscarYCalcular()`, comprobar si el texto del origen es "Mi ubicación" (`textoOrigen.equalsIgnoreCase("Mi ubicación")`). Si lo es:
  1. Comprobamos los permisos de GPS.
  2. Pedimos las coordenadas actuales al GPS (`getLastLocation()`).
  3. Ejecutamos el `RoutesApi.computeRoute()` dándole tus coordenadas reales como punto de origen en lugar de mandar "Mi ubicación" al Geocoder de Google.

## Open Questions
¿Te parece bien añadirlo así? He dejado este plan estructurado, si me das el "Ok" o "Aprobado", procedo inmediatamente a escribirte e inyectar el código para que lo pruebes.
