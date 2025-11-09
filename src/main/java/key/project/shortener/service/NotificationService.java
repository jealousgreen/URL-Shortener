package key.project.shortener.service;
import key.project.shortener.core.*;
import org.slf4j.*;
/**
 * Сервис уведомлений
 *  информирует пользователя о событиях по его ссылкам показывая статусы:
 * срок жизни ссылки истёк (EXPIRED),
 * лимит переходов исчерпан (LIMIT_REACHED)
 */
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    public void notifyLimit(Link l) {
        log.warn("Лимит переходов исчерпан для {} ({}).", l.shortCode, l.originalUrl);
        System.out.println("[УВЕДОМЛЕНИЕ] Лимит переходов исчерпан: " + l.shortCode);
    }
    public void notifyExpired(Link l) {
        log.warn("Ссылка истекла {} ({}).", l.shortCode, l.originalUrl);
        System.out.println("[УВЕДОМЛЕНИЕ] Срок жизни истёк: " + l.shortCode);
    }
}
