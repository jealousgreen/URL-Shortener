package key.project.shortener.core;
import java.security.SecureRandom;
/**
 * Генератор коротких кодов для сокращённых ссылок.
 *
 * Каждый короткий код состоит из рандомных символов.
 * (цифры 0–9, буквы A–Z и a–z)
 * Используется при создании новой ссылки
 */
public class UrlCodeGenerator {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RND = new SecureRandom();
    public static String random(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(ALPHABET.charAt(RND.nextInt(ALPHABET.length())));
        return sb.toString();
    }
}
