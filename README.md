<h1 align="center">🔗 URL Shortener </h1>

<p align="center">
  <b> Java-сервис для сокращения ссылок </b><br>
  TTL • Лимиты переходов • Уведомления • JSON-хранилище
</p>

---

## О проекте

**URL Shortener** - это учебный проект на Java для сокращения ссылок.  
Каждый пользователь получает уникальный UUID и может создавать, управлять 
 своими короткими ссылками из консольного меню.

### Основные возможности

🔐 **Уникальный UUID** для каждого пользователя
🕒 **TTL (время жизни)** ссылки
🚦 **Лимит переходов** 
🔔 **Уведомления** об истечении срока или лимите
💾 **Хранение данных** в JSON-файле (`data/storage.json`)
⚙️ **Конфигурация** через YAML (`Application.yml`)

---

## Конфигурация

Файл: `src/main/resources/application.yml`

```yaml
app:
  ttlHours: 24
  defaultMaxClicks: 50
  baseDomain: "short://"
  dataFile: "data/storage.json"
  notifyOnExpire: true
  notifyOnLimit: true
```

###  Примеры команд
Команда	Описание
```
init	                          показать UUID текущего пользователя
create <url> [-m maxClicks]	    создать короткую ссылку
open <shortCode>                открыть ссылку в браузере
info <shortCode>                показать информацию о ссылке
list                            показать все ссылки пользователя
set-limit <shortCode> <n>       изменить лимит переходов
delete <shortCode>              удалить ссылку
cleanup                         удалить истёкшие ссылки
help                            показать справку
```
###  Архитектура проекта
```text
├── data/
│   └── storage.json                              //хранилище ссылок
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── key/project/shortener/
│   │   │       ├── App.java                      //Запуск, меню
│   │   │       │
│   │   │       ├── config/
│   │   │       │   └── AppConfig.java            //загрузка настроек из application.yml
│   │   │       │
│   │   │       ├── core/
│   │   │       │   ├── Link.java           
│   │   │       │   ├── LinkStatus.java           //состояния ссылки
│   │   │       │   ├── UrlCodeGenerator.java     //генерация коротких кодов
│   │   │       │   ├── UserContext.java          //UUID пользователя
│   │   │       │   └── Validation.java           //проверки валидности URL и данных
│   │   │       │
│   │   │       ├── repo/
│   │   │       │   ├── LinkRepository.java       //интерфейс хранилища ссылок
│   │   │       │   └── FileLinkRepository.java   //реализация на JSON-файле
│   │   │       │
│   │   │       ├── service/
│   │   │       │   ├── ShortenerService.java     //создание/открытие/лимиты/TTL
│   │   │       │   └── NotificationService.java  //уведомления
│   │   │       │
│   │   │       └── util/
│   │   │           └── Time.java       
│   │   │
│   │   └── resources/
│   │       └── application.yml                  //конфигурация (TTL, лимиты, путь хранилища)
│   │
│   └── test/
│       └── java/
│           └── key/project/shortener/test/
│               ├── UrlCodeGeneratorTest.java        //тесты генерации кодов
│               ├── ShortenerServiceLimitTest.java   //тест лимита переходов и TTL
│               └── ShortenerServiceUserTest.java    //тест разные пользователи на один URL
│
├── .gitignore
├── pom.xml
└── README.md
