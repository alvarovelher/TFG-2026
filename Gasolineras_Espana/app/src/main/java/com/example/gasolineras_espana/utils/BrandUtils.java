package com.example.gasolineras_espana.utils;


import com.example.gasolineras_espana.R;

import java.util.Locale;


/**
 * Clase util para trabajar con marcas de gasolineras.
 *
 * FUNCIONES:
 * 1. Normaliza el nombre del rotulo (ej: "Cepsa" -> "MOEVE")
 * 2. Devuelve el drawable correspondiente segun la marca
 *
 * Se usa principalmente para asignar iconos a los markers del mapa.
 */
public class BrandUtils {

    /**
     * Devuelve el recurso drawable asociado a una gasolinera
     * segun su rotulo (marca).
     *
     * @param rotulo Nombre de la gasolinera (ej: "Repsol")
     * @return ID del drawable (ej: R.drawable.ic_repsol)
     */
    public static int getLogoResId(String rotulo) {

        String brand = normalize(rotulo);

        switch (brand) {
            case "REPSOL":
                return R.drawable.ic_repsol;

            case "MOEVE":
                return R.drawable.ic_moeve;

            case "BP":
                return R.drawable.ic_bp;

            case "SHELL":
                return R.drawable.ic_shell;

            case "GALP":
                return R.drawable.ic_galp;

            case "AVIA":
                return R.drawable.ic_avia;

            case "PLENOIL":
                return R.drawable.ic_plenoil;

            case "BALLENOIL":
                return R.drawable.ic_ballenoil;

            case "DISA":
                return R.drawable.ic_disa;

            case "PETROPRIX":
                return R.drawable.ic_petroprix;

            case "CARREFOUR":
                return R.drawable.ic_carrefour;

            case "GASEXPRESS":
                return R.drawable.ic_gasexpress;

            default:
                return R.drawable.ic_default;
        }
    }

    /**
     * Normaliza el rotulo de la gasolinera para evitar problemas
     * con mayusculas, espacios o nombres distintos de la misma marca.
     *
     * @param rotulo Texto original de la gasolinera
     * @return Marca normalizada
     */
    private static String normalize(String rotulo) {

        if (rotulo == null) return "DEFAULT";

        String r = rotulo.trim().toUpperCase(Locale.ROOT);

        if (r.contains("REPSOL")) return "REPSOL";

        if (r.contains("MOEVE")) return "MOEVE";
        if (r.contains("CEPSA")) return "MOEVE";

        if (r.equals("BP") || r.contains("BP ")) return "BP";

        if (r.contains("SHELL")) return "SHELL";

        if (r.contains("GALP")) return "GALP";

        if (r.contains("AVIA")) return "AVIA";

        if (r.contains("PLENOIL")) return "PLENOIL";
        if (r.contains("PLENERGY")) return "PLENOIL";

        if (r.contains("BALLENOIL")) return "BALLENOIL";

        if (r.contains("DISA")) return "DISA";

        if (r.contains("PETROPRIX")) return "PETROPRIX";

        if (r.contains("CARREFOUR")) return "CARREFOUR";

        if (r.contains("GASEXPRESS")) return "GASEXPRESS";

        return "DEFAULT";
    }
}
