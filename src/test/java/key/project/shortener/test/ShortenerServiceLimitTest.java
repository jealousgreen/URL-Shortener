package key.project.shortener.test;

import key.project.shortener.config.AppConfig;
import key.project.shortener.core.Link;
import key.project.shortener.core.LinkStatus;
import key.project.shortener.repo.FileLinkRepository;
import key.project.shortener.service.NotificationService;
import key.project.shortener.service.ShortenerService;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты лимита переходов по ссылке м срока жизни (TTL).
 */
public class ShortenerServiceLimitTest {

    @Test
    void limitBlocksOpenAfterMaxClicks() {
        AppConfig cfg = AppConfig.load();
        FileLinkRepository repo = new FileLinkRepository("target/test-storage-limit.json");
        ShortenerService svc = new ShortenerService(repo, cfg, new NotificationService());

        String user = "00000000-0000-0000-0000-000000000000";

        // Создаём ссылку с лимитом 1
        Link link = svc.create(user, "https://example.com", 1);

        // Первый переход должен пройти
        assertTrue(svc.open(user, link.shortCode), "Первый переход по ссылке с лимитом 1 должен быть успешным");

        // Второй переход должен быть заблокирован
        assertFalse(svc.open(user, link.shortCode), "Второй переход по ссылке с лимитом 1 должен быть заблокирован");

        // Проверим статус
        Link fromRepo = repo.findByCode(user, link.shortCode).orElseThrow();
        assertEquals(LinkStatus.LIMIT_REACHED, fromRepo.status, "Статус должен быть LIMIT_REACHED");
        assertEquals(1, fromRepo.clickCount, "Счётчик кликов не должен увеличиваться после блока");
    }

    @Test
    void expiredLinkCannotBeOpened() {
        AppConfig cfg = AppConfig.load();
        FileLinkRepository repo = new FileLinkRepository("target/test-storage-expired.json");
        ShortenerService svc = new ShortenerService(repo, cfg, new NotificationService());

        String user = "11111111-1111-1111-1111-111111111111";

        Link link = svc.create(user, "https://example.org", null);

        // Делаем ссылку истекшей
        link.expiresAt = link.createdAt.minus(Duration.ofHours(1));
        repo.save(link);

        // Попытка открыть должна вернуть false
        assertFalse(svc.open(user, link.shortCode), "Истекшая ссылка не должна работать");

        Link fromRepo = repo.findByCode(user, link.shortCode).orElseThrow();
        assertEquals(LinkStatus.EXPIRED, fromRepo.status, "Статус должен быть EXPIRED");
    }
}
