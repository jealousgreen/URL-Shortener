package key.project.shortener.core;
import java.net.URI;

/**
 * класс для проверки ввода
 */
public class Validation {
    public static void requireValidUrl(String url) {
        try {
            URI u = new URI(url);
            if (u.getScheme() == null || u.getHost() == null) throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("Невалидный URL: " + url);
        }
    }
}
