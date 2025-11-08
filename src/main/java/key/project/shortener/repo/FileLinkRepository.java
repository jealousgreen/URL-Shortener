package key.project.shortener.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import key.project.shortener.core.Link;
import key.project.shortener.core.LinkStatus;
/**
 * Реализация интерфейса LinkRepository, использующая JSON-файл как хранилище ссылок.
 * Хранит все ссылки пользователя в одном JSON-файле data/storage.json,
 * поддерживает операции создание, поиск, обновление, удаление.
 * Автоматически создаёт нужные каталоги.
 * Использует библиотеку Jackson для объектов Link
 */
public class FileLinkRepository implements LinkRepository {

    private final Path file;
    private final ObjectMapper m =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Конструктор.
     * @param path путь к JSON-файлу из конфига
     */
    public FileLinkRepository(String path) {
        this.file = Paths.get(path);
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            if (!Files.exists(file)) {
                Files.writeString(file, "[]");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    /**
     * Читает все ссылки из JSON.
     * Возвращает изменяемый список ArrayList.
     * Если файл повреждён или пуст, автоматом сбрасывает его в пустой массив.
     */
    private synchronized List<Link> read() {
        try {
            if (!Files.exists(file)) {
                return new ArrayList<>();
            }
            byte[] data = Files.readAllBytes(file);
            if (data.length == 0) {
                return new ArrayList<>();
            }
            Link[] arr = m.readValue(data, Link[].class);
            return new ArrayList<>(Arrays.asList(arr)); // изменяемый список
        } catch (IOException e) {
            // если файл поврежден, сбрасываем его в пустой массив
            try {
                Files.writeString(file, "[]");
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return new ArrayList<>();
        }
    }
    /**
     * Записывает весь список ссылок обратно в JSON.
     * Используется PrettyPrinter для удобства чтения.
     */
    private synchronized void write(List<Link> list) {
        try {
            m.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), list);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Поиск ссылки по владельцу и короткому коду.
     *
     * @param ownerUuid UUID владельца
     * @param code короткий код ссылки
     * @return Optional<Link> найденная ссылка или пусто
     */
    @Override
    public Optional<Link> findByCode(String ownerUuid, String code) {
        return read().stream()
                .filter(l -> l.ownerUuid.equals(ownerUuid) && l.shortCode.equals(code))
                .findFirst();
    }
    /**
     * Возвращает все ссылки пользователя, кроме удалённых.
     *
     * @param ownerUuid UUID владельца
     * @return список активных ссылок
     */
    @Override
    public List<Link> findAllByOwner(String ownerUuid) {
        return read().stream()
                .filter(l -> l.ownerUuid.equals(ownerUuid) && l.status != LinkStatus.DELETED)
                .collect(Collectors.toList());
    }
    /**
     * Сохраняет ссылку в хранилище.
     * если уже есть запись с таким кодом и владельцем, то заменяет,
     * иначе добавляет новую.
     */
    @Override
    public void save(Link link) {
        List<Link> all = read();
        all.remove(link);
        all.add(link);
        write(all);
    }
    /**
     * Помечает ссылку как удалённую
     */
    @Override
    public void delete(String ownerUuid, String code) {
        List<Link> all = read();
        all.stream()
                .filter(l -> l.ownerUuid.equals(ownerUuid) && l.shortCode.equals(code))
                .forEach(l -> l.status = LinkStatus.DELETED);
        write(all);
    }
    /**
     * Проверяет, существование короткого кода у пользователя.
     * Нужно для предотвращения коллизий при генерации новых ссылок.
     */
    @Override
    public boolean existsCode(String ownerUuid, String code) {
        return read().stream()
                .anyMatch(l -> l.ownerUuid.equals(ownerUuid) && l.shortCode.equals(code));
    }
    /**
     * Удаляет ссылки у которых истёк срок жизни (TTL)
     *
     * @param now текущее время
     * @return количество удалённых записей
     */
    @Override
    public int deleteExpired(Instant now) {
        List<Link> all = read();
        int before = all.size();
        all.removeIf(l -> l.isExpired(now));
        write(all);
        return before - all.size();
    }
}
