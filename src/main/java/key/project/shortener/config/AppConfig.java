package key.project.shortener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Класс конфига.
 * Конфиг загружается из файла src/main/resources/application.yml
 * Позволяет задать:
 * время жизни ссылки (TTL),
 * лимит переходов по умолчанию,
 * базовый домен коротких ссылок,
 * путь к файлу-хранилищу,
 * настройки уведомлений.
 */
public class AppConfig {

    public static class App {
        /** Время жизни ссылки (TTL), в часах */
        public int ttlHours = 24;

        /** Лимит переходов по умолчанию */
        public int defaultMaxClicks = 50;

        /** Префикс для отображения коротких ссылок в CLI */
        public String baseDomain = "short://";

        /** Путь к JSON для хранения ссылок */
        public String dataFile = "data/storage.json";

        /** уведомление при истечении срока жизни */
        public boolean notifyOnExpire = true;

        /** уведомление при достижении лимита переходов */
        public boolean notifyOnLimit = true;
    }

    public App app = new App();

    public static AppConfig load() {
        try (InputStream is = AppConfig.class.getResourceAsStream("/application.yml")) {
            if (is == null) {
                System.out.println("Конфиг application.yml не найден, используются значения по умолчанию.");
                return new AppConfig();
            }
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(is, AppConfig.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
