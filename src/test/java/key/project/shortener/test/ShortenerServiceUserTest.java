package key.project.shortener.test;

import key.project.shortener.config.AppConfig;
import key.project.shortener.core.Link;
import key.project.shortener.repo.FileLinkRepository;
import key.project.shortener.service.NotificationService;
import key.project.shortener.service.ShortenerService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тест. 2 юзера создают короткие ссылки на один и тот же URL.
 */
public class ShortenerServiceUserTest {

    @Test
    void usersGetCodesForSameUrl() {
        // Загружаем конфиг
        AppConfig cfg = AppConfig.load();

        // Отдельный файл для теста
        FileLinkRepository repo = new FileLinkRepository("target/test-storage-multi.json");
        ShortenerService svc = new ShortenerService(repo, cfg, new NotificationService());

        String user1 = "11111111-1111-1111-1111-111111111111";
        String user2 = "00000000-0000-0000-0000-000000000000";
        String url = "https://example.com";

        Link link1 = svc.create(user1, url, null);
        Link link2 = svc.create(user2, url, null);

        assertNotEquals(link1.shortCode, link2.shortCode,
                "Разные пользователи должны получать разные короткие коды для одного и того же URL");
        assertEquals(url, link1.originalUrl);
        assertEquals(url, link2.originalUrl);
    }
}
