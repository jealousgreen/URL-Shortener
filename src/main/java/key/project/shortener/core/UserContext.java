package key.project.shortener.core;
import java.io.*;
import java.nio.file.*;
import java.util.*;
/**
 * Класс для идентификации пользователя.
 * каждому пользователю автоматически присваивается уникальный UUID,
 * который сохраняется в файл в домашней директории.
 * Этот UUID используется для разделения ссылок между пользователями.
 */
public class UserContext {
    private static final String FILE = System.getProperty("user.home")+File.separator+".shortener.uuid";
    public static String ensureUuid() {
        try {
            Path p = Paths.get(FILE);
            if (Files.exists(p)) return Files.readString(p).trim();
            String uuid = UUID.randomUUID().toString();
            Files.writeString(p, uuid);
            return uuid;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}