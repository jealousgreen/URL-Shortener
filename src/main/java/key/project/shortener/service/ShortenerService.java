package key.project.shortener.service;

import java.awt.Desktop;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import key.project.shortener.config.AppConfig;
import key.project.shortener.core.*;
import key.project.shortener.repo.LinkRepository;
import key.project.shortener.util.Time;

/**
 * сервис для работы с короткими ссылками.
 * создание короткой ссылки с учётом TTL и лимита переходов,
 * просмотр информации,
 * изменение лимита,
 * удаление,
 * открытие ссылки в браузере,
 * очистка истёкших ссылок.
 */
public class ShortenerService {
    private final LinkRepository repo; // здесь будут ссылки
    private final AppConfig cfg;
    private final NotificationService ns;

    public ShortenerService(LinkRepository repo, AppConfig cfg, NotificationService ns) {
        this.repo = repo;
        this.cfg = cfg;
        this.ns = ns;
    }
    /**
     * Создание новой короткой ссылки.
     *
     * @param ownerUuid UUID владельца
     * @param url оригинальный URL
     * @param maxClicksOverride лимит переходов
     * @return объект Link
     */
    public Link create(String ownerUuid, String url, Integer maxClicksOverride) {
        Validation.requireValidUrl(url);
        int maxClicks =
                (maxClicksOverride != null) ? maxClicksOverride : cfg.app.defaultMaxClicks;

        String prefix = ownerUuid.substring(0, 8);  // Префикс для кода первые 8 символов UUID пользователя, для уникальности
        String code;
        do {
            code = prefix + "-" + UrlCodeGenerator.random(6);
        } while (repo.existsCode(ownerUuid, code));

        Link l = new Link();
        l.ownerUuid = ownerUuid;
        l.originalUrl = url;
        l.shortCode = code;
        l.maxClicks = maxClicks;
        l.clickCount = 0;
        l.createdAt = Time.now();
        l.expiresAt = l.createdAt.plus(Duration.ofHours(cfg.app.ttlHours));
        l.status = LinkStatus.ACTIVE;

        repo.save(l);
        return l;
    }
    /**
     * Получение информации о ссылке по короткому коду
     * возвращает Optional<Link>, чтобы можно было безопасно проверить наличие.
     */
    public Optional<Link> info(String ownerUuid, String code) {
        return repo.findByCode(ownerUuid, code);
    }
    /**
     * Возвращает все активные ссылки пользователя.
     */
    public List<Link> list(String ownerUuid) {
        return repo.findAllByOwner(ownerUuid);
    }
    /**
     * Изменяет лимит переходов по ссылке,
     * проверяет права
     *
     * @return true если найдено и обновлено, false если ссылка не найдена
     */
    public boolean setLimit(String ownerUuid, String code, int newLimit) {
        Optional<Link> o = repo.findByCode(ownerUuid, code);
        if (o.isEmpty()) return false;
        Link l = o.get();
        if (!l.ownerUuid.equals(ownerUuid)) {
            throw new SecurityException("Недостаточно прав");
        }
        l.maxClicks = newLimit;
        if (l.clickCount >= l.maxClicks) {
            l.status = LinkStatus.LIMIT_REACHED;
            if (cfg.app.notifyOnLimit) ns.notifyLimit(l);
        } else if (l.status == LinkStatus.LIMIT_REACHED) {
            l.status = LinkStatus.ACTIVE;
        }
        repo.save(l);
        return true;
    }
    /**
     * Помечает ссылку как удалённую,
     * но реально не удаляет из файла, чтобы сохранить как лог.
     */
    public boolean delete(String ownerUuid, String code) {
        Optional<Link> o = repo.findByCode(ownerUuid, code);
        if (o.isEmpty()) return false;
        Link l = o.get();
        if (!l.ownerUuid.equals(ownerUuid)) {
            throw new SecurityException("Недостаточно прав");
        }
        l.status = LinkStatus.DELETED;
        repo.save(l);
        return true;
    }
    /**
     * Очищает хранилище от протухших ссылок (TTL)
     *
     * @return количество удалённых ссылок
     */
    public int cleanup() {
        return repo.deleteExpired(Time.now());
    }

    public boolean open(String ownerUuid, String code) {
        Optional<Link> o = repo.findByCode(ownerUuid, code);
        if (o.isEmpty()) return false;
        Link l = o.get();
        Instant now = Time.now();

        if (l.isExpired(now)) {
            l.status = LinkStatus.EXPIRED;
            repo.save(l);
            if (cfg.app.notifyOnExpire) ns.notifyExpired(l);
            return false;
        }

        if (l.limitReached()) {
            l.status = LinkStatus.LIMIT_REACHED;
            repo.save(l);
            if (cfg.app.notifyOnLimit) ns.notifyLimit(l);
            return false;
        }
        // Увеличиваем счётчик переходов
        l.clickCount++;
        if (l.limitReached()) {
            l.status = LinkStatus.LIMIT_REACHED;
            if (cfg.app.notifyOnLimit) ns.notifyLimit(l);
        }
        repo.save(l);
        // Пытаемся открыть ссылку в браузере
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(l.originalUrl));
            }
        } catch (Exception ignored) {
        }

        return true;
    }
}