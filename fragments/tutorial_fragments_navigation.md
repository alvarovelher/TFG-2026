# Tutorial: Navegación con Fragments y Menú Animado Personalizado

Este documento explica cómo está implementada la navegación inferior (Bottom Navigation) en el proyecto. Intervienen principalmente dos partes: **El contenedor principal (MainActivity + su diseño)** y **Los propios Fragments**.

> [!NOTE]
> No solamente actúan las 3 clases del paquete `fragments`. Esas clases son únicamente las "pantallas". El verdadero motor de navegación reside en el `MainActivity.java` y su configuración en el archivo `activity_main.xml`.

---

## 1. Los Fragments (Las "Pantallas")

El código tiene el paquete `fragments` que contiene tres clases principales:
- `MapFragment.java`
- `QuedadasFragment.java`
- `UsuarioFragment.java`

Estas 3 clases representan el **contenido** (lo que ves dependiendo de en qué pestaña estés). Al heredar de `Fragment`, ganan la capacidad de ser inyectadas, retiradas e intercambiadas dinámicamente dentro de una actividad "padre" sin tener que abrir una nueva _Activity_ completa cada vez. 

---

## 2. El Diseño del Menú Personalizado (`activity_main.xml`)

En lugar de usar el `BottomNavigationView` estándar que suele venir por defecto en Android, el autor se ha currado un menú inferior completamente **a medida** con un diseño muy llamativo.

El framework principal de la vista en ese archivo es el siguiente:
- **Un `FrameLayout` (`@+id/contenedorFrag`)**: Este es el "lienzo" principal. Es el espacio que ocupa toda la pantalla y que se usará para mostrar el fragment que corresponda en cada momento.
- **Un `RelativeLayout` (El menú inferior)**: Justo abajo, tiene el contenedor con el diseño del menú.
- **Una vista animada (`@+id/selector`)**: Una cápsula o pastilla que hace de fondo coloreado y que se va moviendo de botón en botón.

```xml
<!-- Hueco libre donde se cargarán los Fragments ocupando toda la pantalla -->
<FrameLayout
    android:id="@+id/contenedorFrag"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

<!-- (Ejemplo) Vista que hace de "pastilla" seleccionable que deslizará -->
<View
    android:id="@+id/selector"
    android:layout_width="50dp"
    android:layout_height="50dp"
    android:background="@drawable/pill_selector" />
```

---

## 3. El Motor del Menú (`MainActivity.java`)

La magia real del cambio de Fragments y la animación de la pastilla sucede íntegramente en `MainActivity.java`. Las rutinas clave son:

### A) Intercambio del Fragment (Cambio de Pantalla)
El método `openFragment(int index)` de `MainActivity` es el responsable de la transición. Instancia el Fragment correspondiente (en base a la pulsación de un botón) y le pide a Android, a través del `FragmentManager`, que reemplace lo que había antes.

```java
// Cambia el fragment según lo seleccionado
private void openFragment(int index) {
    Fragment fragment;

    switch (index) {
        case 0: fragment = new MapFragment(); break;
        case 1: fragment = new QuedadasFragment(); break;
        case 2: fragment = new UsuarioFragment(); break;
        default: return;
    }

    // El transaction manager quita el anterior y pone el nuevo
    getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.contenedorFrag, fragment) // Sustituye el hueco
            .commit(); // Lo aplica a la interfaz
}
```

### B) Animación del Menú Inferior
Para dar ese tacto dinámico tan orgánico de la cápsula moviéndose, la actividad usa unas animaciones bastante avanzadas de las librerías físicas de Android:

1. **Movimiento elástico (`moveIndicator`)**: En lugar de hacer una animación lineal aburrida, usa el sistema **`SpringAnimation`** (Físicas de rebote). Esto desliza la pastilla de un icono a otro aplicando físicas de muelle que lo ralentizan al llegar.
2. **Rebote del propio icono (`animateSelectedIcon`)**: El icono en sí efectúa un salto para llamar la atención del usuario usando una animación tradicional `R.anim.scale_icon_bounce`.

---

## Resumen del Flujo de Ejecución

1. El usuario arranca la app y Android lanza `MainActivity.java`.
2. Como se ha iniciado por primera vez, `MainActivity` en su método `onCreate` inyecta automáticamente por defecto el `MapFragment` en el hueco libre.
3. Al pulsar sobre el icono de "Quedadas", el método `selectButton(1)` se ejecuta.
4. Calcula en qué coordenadas "X" está el botón pulsado, y la _pastilla_ se desplaza mediante elástica hacia ahí. El icono de _Quedadas_ salta.
5. Inmediatamente invoca a `openFragment(1)`, pidiendo a Android retirar el mapa y meter ahí la clase `QuedadasFragment`.
