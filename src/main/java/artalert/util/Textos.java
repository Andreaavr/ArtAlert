package artalert.util;

import java.text.Normalizer;
import java.util.Locale;

/** Utilidades de texto compartidas, para no repetir el mismo código en varias clases. */
public final class Textos {

    private Textos() {
    }

    /**
     * Minúsculas y sin tildes, para comparar nombres: "Claude MONÉT " → "claude monet".
     * Un texto null se trata como vacío.
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim();
    }
}
