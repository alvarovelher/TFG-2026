# Plan de Rediseño de Navegación y UI (Estructura Principal)

Me parece un salto espectacular. Vamos a preparar la aplicación para escalar y tener sección de Favoritos, Ajustes y Mapa, sentando las bases desde ya.

## 1. Debate UX (Diseño de Usuario) sobre tu propuesta

> **¿Teniendo fragmentos abajo es necesario el menú hamburguesa?**
> **No.** En el diseño moderno (Material Design 3 de Google), si pones una barra de navegación inferior (*BottomNavigationView*) con "Mapa", "Favoritas" y "Ajustes", el menú hamburguesa se considera redundante y anticuado. La regla de oro es: si tienes 3 o 4 secciones clave, van todas a la barra inferior. Adiós a la hamburguesa. 🍔❌

> **¿Qué hacemos con el Logo de la app?**
> Ponerlo de marca de agua tenue detrás del menú inferior suele quedar mal y puede dar problemas de legibilidad. Lo ideal (y lo que más espacio libera) es crear una barra superior flotante (*SearchBar/TopBar*) que tenga:
> - A la izquierda: El logo pequeñito redondeado.
> - En el centro: Un texto o buscador.
> - A la derecha: El botón central de "Filtros".

## 2. Cambios Estructurales en `activity_mapa.xml`

Vamos a reescribir el XML así:

### [MODIFY] `activity_mapa.xml`
Vamos a agrupar todo en un `ConstraintLayout` (la forma más moderna y eficiente de colocar cajas en Android).

**A. El Top (Cabecera Flotante)**
Un bloque en la parte superior que flote sobre el mapa. Contendrá el botón de Filtros y, si quieres, el espacio para el logo y buscador.

**B. El Centro (El Mapa)**
El `SupportMapFragment` ocupará todo el centro, fluyendo dinámicamente y empapando de mapa el fondo para que luzca espacioso. Ajustaremos tu botón de Ubicación para que respete el nuevo espacio.

**C. El Bottom (Hueco para Menú Inferior)**
Justo en la franja inferior clavaremos un `FrameLayout` vacío con un color temporal y un texto gigante que diga `"ZONA FUTURO BOTTOM NAV: INICIO | FAVORITAS | AJUSTES"`. 
Esto dejará la estructura rígida y preparada para que más adelante inyectemos ahí dentro un `BottomNavigationView` de verdad.

## Reflexión

Con esta estructura matamos dos pájaros de un tiro: tendrás la pantalla lista para que el mapa solo sea un "Fragment" más tarde, y el espacio de los Filtros quedará súper accesible para los pulgares.

¿Que te parecen estas anotaciones y estas mejoras en el diseño?