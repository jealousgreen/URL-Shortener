package key.project.shortener.test;

import key.project.shortener.core.UrlCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты генератора коротких кодов.
 */
public class UrlCodeGeneratorTest {

    @Test
    void randomGeneratesCodeOfGivenLength() {
        int len = 8;
        String code = UrlCodeGenerator.random(len);
        assertEquals(len, code.length(), "Длина кода ссылки должна совпадать с запрошенной");
    }

    @Test
    void randomGeneratesDifferentCodes() {
        String a = UrlCodeGenerator.random(8);
        String b = UrlCodeGenerator.random(8);
        assertNotEquals(a, b, "Два кода подряд не должны совпадать");
    }
}
