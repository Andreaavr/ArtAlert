package artalert.fuentes;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Implementación de {@link ClienteHttp} con el HttpClient que trae Java. */
public class ClienteHttpJava implements ClienteHttp {

    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(15);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(TIEMPO_MAXIMO)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public String get(String url) {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIEMPO_MAXIMO)
                .header("Accept", "application/json")
                .header("User-Agent", "ArtAlert/1.0 (proyecto educativo)")
                .GET()
                .build();
        try {
            HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                throw new FuenteNoDisponibleException(
                        "Respuesta " + respuesta.statusCode() + " al pedir " + url);
            }
            return respuesta.body();
        } catch (IOException e) {
            throw new FuenteNoDisponibleException("No se pudo conectar con " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FuenteNoDisponibleException("Petición interrumpida: " + url, e);
        }
    }
}
