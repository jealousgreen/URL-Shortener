package key.project.shortener;

import java.util.List;
import java.util.Scanner;
import key.project.shortener.config.AppConfig;
import key.project.shortener.core.Link;
import key.project.shortener.core.UserContext;
import key.project.shortener.repo.FileLinkRepository;
import key.project.shortener.service.NotificationService;
import key.project.shortener.service.ShortenerService;

public class App {

    private static final Scanner scanner = new Scanner(System.in); // для меню

    public static void main(String[] args) {
        AppConfig cfg = AppConfig.load();
        String uuid = UserContext.ensureUuid();
        ShortenerService service =
                new ShortenerService(
                        new FileLinkRepository(cfg.app.dataFile), cfg, new NotificationService());

        boolean running = true;

        while (running) {
            printMenu();
            int choice = getIntInput("Выберите действие: ");

            try {
                switch (choice) {
                    case 1 -> createShortUrl(service, uuid, cfg.app.baseDomain);
                    case 2 -> openUrl(service, uuid);
                    case 3 -> showInfo(service, uuid);
                    case 4 -> listUrls(service, uuid);
                    case 5 -> setLimit(service, uuid);
                    case 6 -> deleteUrl(service, uuid);
                    case 7 -> cleanup(service);
                    case 0 -> {
                        System.out.println("Выход из программы");
                        running = false;
                    }
                    default -> System.out.println("Неверный выбор");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║   СЕРВИС СОКРАЩЕНИЯ ССЫЛОК               ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║ 1.  Создать короткую ссылку              ║");
        System.out.println("║ 2.  Открыть ссылку в браузере            ║");
        System.out.println("║ 3.  Показать информацию по коду          ║");
        System.out.println("║ 4.  Показать все мои ссылки              ║");
        System.out.println("║ 5.  Изменить лимит переходов             ║");
        System.out.println("║ 6.  Удалить ссылку                       ║");
        System.out.println("║ 7.  Очистить протухшие ссылки            ║");
        System.out.println("║ 0.  Выход                                ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }

    private static void createShortUrl(ShortenerService service, String uuid, String baseUrl) {
        System.out.print("Введите полный URL: ");
        String originalUrl = scanner.nextLine();
        System.out.print("Лимит переходов (0 — по умолчанию): ");
        int limit = getIntInput("");
        Integer max = (limit > 0) ? limit : null;

        Link l = service.create(uuid, originalUrl, max);
        System.out.println("\nКороткая ссылка создана!");
        System.out.println("Оригинальный URL: " + originalUrl);
        System.out.println("Короткая ссылка: " + baseUrl + l.shortCode);
        System.out.println("Код: " + l.shortCode);
    }

    private static void openUrl(ShortenerService service, String uuid) {
        System.out.print("Введите короткий код: ");
        String code = scanner.nextLine();
        boolean ok = service.open(uuid, code);
        System.out.println(ok ? "Ссылка открыта (если поддерживается Desktop)" : "Ссылка недоступна");
    }

    private static void showInfo(ShortenerService service, String uuid) {
        System.out.print("Введите короткий код: ");
        String code = scanner.nextLine();
        service
                .info(uuid, code)
                .ifPresentOrElse(
                        l ->
                                System.out.printf(
                                        "%s status=%s clicks=%d/%d expires=%s url=%s%n",
                                        l.shortCode, l.status, l.clickCount, l.maxClicks, l.expiresAt, l.originalUrl),
                        () -> System.out.println("Не найдено"));
    }

    private static void listUrls(ShortenerService service, String uuid) {
        List<Link> links = service.list(uuid);
        if (links.isEmpty()) {
            System.out.println("Ссылок пока нет");
        } else {
            links.forEach(
                    l ->
                            System.out.printf(
                                    "%s %s %d/%d %s%n",
                                    l.shortCode, l.status, l.clickCount, l.maxClicks, l.originalUrl));
        }
    }

    private static void setLimit(ShortenerService service, String uuid) {
        System.out.print("Введите короткий код: ");
        String code = scanner.nextLine();
        int newLimit = getIntInput("Новый лимит: ");
        boolean ok = service.setLimit(uuid, code, newLimit);
        System.out.println(ok ? "Лимит обновлён" : "Ссылка не найдена");
    }

    private static void deleteUrl(ShortenerService service, String uuid) {
        System.out.print("Введите короткий код: ");
        String code = scanner.nextLine();
        boolean ok = service.delete(uuid, code);
        System.out.println(ok ? "Ссылка помечена как удалённая" : "Ссылка не найдена");
    }

    private static void cleanup(ShortenerService service) {
        int removed = service.cleanup();
        System.out.println("Удалено истёкших ссылок: " + removed);
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                if (!prompt.isEmpty()) {
                    System.out.print(prompt);
                }
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число");
            }
        }
    }
}
