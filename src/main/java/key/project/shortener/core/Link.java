package key.project.shortener.core;

import java.time.Instant;
import java.util.Objects;

/*
 * Класс хранит данные, относящиеся к одной короткой ссылке,
 * владельца (UUID), оригинальный URL, короткий код, лимиты и сроки.
 * Используется в JSON-хранилище FileLinkRepository и в ShortenerService.
 */
public class Link {
    public String ownerUuid;
    public String originalUrl;
    public String shortCode;
    public int maxClicks;
    public int clickCount;
    public Instant createdAt;
    public Instant expiresAt;
    public LinkStatus status;

    public boolean isExpired(Instant now) { return expiresAt != null && now.isAfter(expiresAt); }
    public boolean limitReached() { return maxClicks > 0 && clickCount >= maxClicks; }

    // Равенство ссылок определяется по владельцу и короткому коду
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link l)) return false;
        return Objects.equals(shortCode, l.shortCode) && Objects.equals(ownerUuid, l.ownerUuid);
    }
    // Хэш по тем же полям
    @Override public int hashCode() { return Objects.hash(shortCode, ownerUuid); }
}
