# ArtAlert

Aplicación de consola en Java que avisa a cada usuario de lo que pasa con los artistas que sigue en varios museos: obras nuevas en la colección, obras que salen a sala y obras que se retiran. El usuario elige libremente qué artistas sigue y en qué museos: ninguno, uno o todos los que quiera.

Museos (APIs públicas, sin clave):

| Museo | Clase |
|---|---|
| Art Institute of Chicago | `FuenteChicago` |
| Cleveland Museum of Art | `FuenteCleveland` |
| The Metropolitan Museum of Art | `FuenteMet` |

## Patrones de diseño

- **Observador (principal).** `Artista` es el sujeto y `Usuario` el observador. Cada artista de cada museo es un sujeto distinto: "Monet en Chicago" y "Monet en The Met" se siguen por separado.
- **Estrategia.** `FuenteObras` es la estrategia para obtener obras, y cada museo es una implementación intercambiable.
- **Plantilla.** `FuenteHttpBase.buscarPorArtista()` fija los pasos (URL → petición → JSON → `Obra`). Cada museo solo implementa lo que le diferencia.
- **Decorador (opcional).** `FuenteConCache` envuelve cualquier fuente para no repetir llamadas a la API.

## Principios SOLID: normas para todo el código

| Principio | Cómo se aplica en ArtAlert |
|---|---|
| **S**: responsabilidad única | Cada clase hace una cosa. `Artista` gestiona sus suscriptores; comparar obras viejas con nuevas lo hace `ComparadorObras`; las peticiones HTTP las hace `ClienteHttp`; `Main` solo crea y conecta objetos. |
| **O**: abierto/cerrado | Un museo nuevo es una clase nueva que hereda de `FuenteHttpBase`. No se toca nada de lo que ya funciona. |
| **L**: sustitución de Liskov | Toda `FuenteObras` cumple el mismo contrato: nunca devuelve `null` (como mucho, una lista vacía) y, si el museo falla, lanza `FuenteNoDisponibleException`. El método plantilla es `final`. |
| **I**: segregación de interfaces | Interfaces pequeñas: `Observador` tiene un método; `Sujeto`, tres; `FuenteObras`, dos. |
| **D**: inversión de dependencias | Las clases dependen de interfaces (`Observador`, `FuenteObras`, `ClienteHttp`) y las reciben por constructor. Nada de `new FuenteChicago()` dentro de la lógica: eso solo se hace en `Main`. |

Reglas prácticas:
- Inyectar las dependencias por constructor, nunca crearlas dentro de la clase.
- No usar `instanceof` para distinguir museos ni usuarios: si hace falta, falta un método en la interfaz.
- Nada de `static` con estado (sin Singletons).
- Las listas que se devuelven son de solo lectura (`List.copyOf`).

## Diagrama

![UML general](docs/UML_ArtAlert_general.png)

## Estructura

```
src/main/java/artalert/
├── modelo/       Obra, EventoObra, TipoEvento                  (compartido)
├── observador/   Sujeto, Observador                            (compartido)
├── fuentes/      FuenteObras, FuenteHttpBase, ClienteHttp,
│                 ClienteHttpJava, FuenteNoDisponibleException  (compartido)
│                 FuenteChicago                                  (Persona A)
│                 FuenteCleveland                                (Persona B)
│                 FuenteMet                                      (tras el primer merge)
├── artistas/     Artista, ComparadorObras, CatalogoArtistas,
│                 Vigilante                                      (Persona A)
├── usuarios/     Usuario                                        (Persona B)
└── app/          Main, MenuConsola                              (Persona B; A añade su parte en Main)
```

## Reparto

### Persona A: rama `feature/sujeto-api`
1. `artistas/ComparadorObras`: recibe las obras conocidas y las nuevas y devuelve la lista de `EventoObra` (NUEVA, EXPUESTA, RETIRADA).
2. `artistas/Artista implements Sujeto`: suscriptores sin duplicados, `notificar()`, `tieneSeguidores()` y `actualizarObras()` (usa `ComparadorObras`). La primera carga no avisa de nada; solo guarda lo que ya existe.
3. `artistas/CatalogoArtistas`: devuelve siempre la misma instancia para cada artista y museo.
4. `artistas/Vigilante`: recorre el catálogo y, solo para los artistas con seguidores, pide las obras a su fuente. Si un museo falla, lo indica y sigue con los demás.
5. `fuentes/FuenteChicago extends FuenteHttpBase`: usa el campo `is_on_view` para saber si está en sala.
6. En `Main`: crear las fuentes, el catálogo y el vigilante.

### Persona B: rama `feature/observadores`
1. `usuarios/Usuario implements Observador`: `seguir()` y `dejarDeSeguir()` (suscribe y desuscribe en el artista), `getSeguidos()` y el panel de avisos. Seguir a cero artistas es válido.
2. `app/MenuConsola`: elegir museo, buscar artista, seguir, dejar de seguir, ver mis artistas, ver avisos y comprobar novedades.
3. `fuentes/FuenteCleveland extends FuenteHttpBase`.
4. En `Main`: crear los usuarios y lanzar el menú.

### Después del primer merge
- `fuentes/FuenteMet`: sobrescribe el gancho `obtenerResultados()` porque necesita dos llamadas, primero los ids y luego cada obra.
- Opcional: `fuentes/FuenteConCache` (Decorador).

## Cómo ejecutar

Abrir la carpeta en IntelliJ IDEA (detecta el `pom.xml`), esperar a que Maven descargue las dependencias y ejecutar `artalert.app.Main`. Las pruebas están en `src/test/java`.
