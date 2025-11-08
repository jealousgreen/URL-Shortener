package key.project.shortener.repo;

import key.project.shortener.core.Link;
import java.time.Instant;
import java.util.*;
/**
 * Интерфейс репозитория ссылок.
 * Определяет операции для работы с коллекцией коротких ссылок:
 * поиск,
 * сохранение,
 * удалениею
 * проверка уникальности,
 * очистка просроченных ссылок.
 */
public interface LinkRepository {
    Optional<Link> findByCode(String ownerUuid, String shortCode);
    List<Link> findAllByOwner(String ownerUuid);
    void save(Link link);
    void delete(String ownerUuid, String shortCode);
    boolean existsCode(String ownerUuid, String shortCode);
    int deleteExpired(Instant now);
}