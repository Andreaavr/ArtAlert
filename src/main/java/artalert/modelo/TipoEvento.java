package artalert.modelo;

/** Qué le ha pasado a una obra desde la última comprobación. */
public enum TipoEvento {
    /** Aparece en la colección una obra que no se conocía. */
    NUEVA,
    /** Una obra conocida pasa a estar en sala. */
    EXPUESTA,
    /** Una obra conocida deja de estar en sala. */
    RETIRADA
}
