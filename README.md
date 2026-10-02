# Telegram Bot Starter

Spring Boot-стартер для Telegram-ботов на Java и Kotlin. Объявляйте обработчики команд и кнопок через аннотации, возвращайте `Response`, а получение обновлений, маршрутизацию и отправку ответов берёт на себя библиотека.

```java
@BotHandler
public class StartHandler {
    @CommandHandle("start")
    public Response start(@Ctx UpdateContext ctx) {
        return ctx.send("Привет! Это мой первый бот.");
    }
}
```

Для первого бота нужны только зависимость, токен и обработчик. База данных, домен, публичный сервер, ID администратора и собственный `UserService` не обязательны.

## Содержание

- [Возможности](#возможности)
- [Требования](#требования)
- [Первый бот](#первый-бот)
- [Готовый пример](#готовый-пример)
- [Кнопки и ответы](#кнопки-и-ответы)
- [Настройки](#настройки)
- [Webhook](#webhook)
- [Расширение библиотеки](#расширение-библиотеки)
- [Частые проблемы](#частые-проблемы)
- [Разработка и обновление](#разработка-и-обновление)

## Возможности

- Обработка команд, callback-кнопок, текста, фото и добавления бота в группу.
- Передача контекста, ID чата и пользователя в аргументы обработчика через аннотации.
- Отправка и редактирование текста и фотографий, inline-клавиатуры.
- Диалоги с историей, ограничения доступа и частоты запросов.
- Асинхронные ответы с индикатором загрузки, таймаутом и обработчиками ошибок.
- Long polling по умолчанию; webhook как отдельный режим.
- Возможность заменить стандартные сервисы своими Spring-бинами.

Модули: `telegram-bot-core` содержит API и обработку обновлений; `telegram-bot-spring-boot-starter` подключает ядро и автоматическую конфигурацию Spring Boot. В приложении достаточно зависимости на стартер.

## Требования

| Компонент | Версия в текущем проекте |
| --- | --- |
| JDK | 21 или новее |
| Spring Boot | 4.1.0 |
| Maven | Wrapper библиотеки использует 3.9.16 |
| Kotlin | 2.2.0 внутри библиотеки; для Java-приложения Kotlin-плагин не нужен |
| Стартер | `ru.tggc:telegram-bot-spring-boot-starter:0.0.1-SNAPSHOT` |

Ниже используется версия из исходников и локальная установка Maven. Инструкция не предполагает публикацию артефакта в Maven Central. Совместимость с другими версиями Spring Boot здесь не заявляется.

## Первый бот

### 1. Получите токен

В Telegram откройте `@BotFather`, отправьте `/newbot`, задайте имя и username нового бота. Сохраните выданный токен: он даёт доступ к управлению ботом.

Не добавляйте токен в Git, README или исходный код. Для примера передадим его через переменную окружения.

### 2. Установите библиотеку

Скачайте исходники этого репозитория и откройте терминал **в его корне**, рядом с родительским `pom.xml`.

Windows PowerShell:

```powershell
.\mvnw.cmd install
```

Linux / macOS:

```bash
./mvnw install
```

Установка собирает оба модуля, запускает тесты и помещает артефакты в локальный Maven-репозиторий. `JAVA_HOME` должен указывать на JDK 21+. Для первой сборки нужен интернет для скачивания зависимостей.

### 3. Создайте приложение

В отдельной папке `hello-bot` создайте такую структуру:

```text
hello-bot/
  pom.xml
  src/main/java/example/hellobot/
    HelloBotApplication.java
    StartHandler.java
```

`pom.xml`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.0</version>
        <relativePath/>
    </parent>
    <groupId>example</groupId>
    <artifactId>hello-bot</artifactId>
    <version>1.0.0</version>
    <properties>
        <java.version>21</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>ru.tggc</groupId>
            <artifactId>telegram-bot-spring-boot-starter</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

`src/main/java/example/hellobot/HelloBotApplication.java`:

```java
package example.hellobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HelloBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloBotApplication.class, args);
    }
}
```

`src/main/java/example/hellobot/StartHandler.java`:

```java
package example.hellobot;

import ru.tggc.telegrambotcore.annotation.handle.BotHandler;
import ru.tggc.telegrambotcore.annotation.handle.CommandHandle;
import ru.tggc.telegrambotcore.annotation.params.Ctx;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.dto.UpdateContext;

@BotHandler
public class StartHandler {
    @CommandHandle("start")
    public Response start(@Ctx UpdateContext ctx) {
        return ctx.send("Привет! Это мой первый бот.");
    }
}
```

Обработчик находится в том же пакете, что и приложение, либо в его подпакете: так Spring обнаружит его автоматически.

### 4. Запустите

Откройте терминал **в папке приложения `hello-bot`**. Следующие команды предполагают установленный Maven в `PATH`; для готового примера ниже можно использовать wrapper библиотеки.

Windows PowerShell:

```powershell
$env:TELEGRAM_TOKEN = 'токен-от-BotFather'
mvn spring-boot:run
```

Linux / macOS:

```bash
export TELEGRAM_TOKEN='токен-от-BotFather'
mvn spring-boot:run
```

В IntelliJ IDEA можно запустить `HelloBotApplication`, указав `TELEGRAM_TOKEN` в **Run Configuration → Environment variables**. Переменная из терминала не появится автоматически в уже открытой конфигурации IDEA.

Файл `application.yml` для этого примера не нужен. Стартер выбирает long polling: бот сам получает обновления от Telegram. Нужен исходящий доступ к Telegram API, но не публичный входящий адрес.

### 5. Проверьте ответ

Откройте личный чат с созданным ботом и отправьте `/start`. Ожидаемый ответ:

```text
Привет! Это мой первый бот.
```

Остановить приложение в терминале можно через `Ctrl+C`. При остановленном приложении бот не отвечает.

Что делает пример:

- `@BotHandler` регистрирует класс как Spring-компонент с обработчиками.
- `@CommandHandle("start")` обрабатывает `/start`. В аннотации слеш не указывается.
- `@Ctx` передаёт `UpdateContext`: текущие `chatId`, `userId` и `messageId`.
- `ctx.send(...)` создаёт ответ; отправку выполняет библиотека после возврата из обработчика.
- Возвращается `Response`, а не обычный `String`. Вызывать `accept()` вручную не нужно.

## Готовый пример

Те же файлы находятся в [examples/hello-bot](examples/hello-bot). Пример является отдельным Maven-проектом и не включён в сборку модулей библиотеки.

После установки библиотеки выполните **из корня репозитория**:

```powershell
$env:TELEGRAM_TOKEN = 'токен-от-BotFather'
.\mvnw.cmd -f examples/hello-bot/pom.xml spring-boot:run
```

Для Linux / macOS замените `.\mvnw.cmd` на `./mvnw` и задайте переменную через `export`.

Сборка исполняемого JAR без запуска бота:

```powershell
.\mvnw.cmd -f examples/hello-bot/pom.xml package
```

Запуск собранного приложения с уже заданной переменной `TELEGRAM_TOKEN`:

```powershell
java -jar examples/hello-bot/target/hello-bot-1.0.0.jar
```

## Кнопки и ответы

Когда `/start` заработал, добавьте рядом со `StartHandler` файл `MenuHandler.java`:

```java
package example.hellobot;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import ru.tggc.telegrambotcore.annotation.handle.BotHandler;
import ru.tggc.telegrambotcore.annotation.handle.CallbackHandle;
import ru.tggc.telegrambotcore.annotation.handle.CommandHandle;
import ru.tggc.telegrambotcore.annotation.params.Ctx;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.dto.UpdateContext;

@BotHandler
public class MenuHandler {
    @CommandHandle("menu")
    public Response menu(@Ctx UpdateContext ctx) {
        var keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton("Поздороваться").callbackData("hello"));
        return ctx.send("Выбери действие:").keyboard(keyboard);
    }

    @CallbackHandle(value = "hello", canPrivate = true)
    public Response hello(@Ctx UpdateContext ctx) {
        return ctx.editText("Привет! Кнопка работает.");
    }
}
```

Перезапустите приложение, отправьте `/menu` и нажмите кнопку. `canPrivate = true` здесь обязателен для личного чата: у callback-обработчиков значение по умолчанию отличается от команд. Клавиатура, отправленная через `ctx`, привязывается к пользователю; по умолчанию действует `Access.OWNER_ONLY`.

Для обычного текста используйте `editText(...)`. Метод `edit(String)` меняет **подпись фотографии**, а не текстовое сообщение. У цепочек `send`, `ask`, `editText` и `sendPhoto` вызов `.build()` необязателен.

Более сложные сценарии: [ответы, фотографии, диалоги и асинхронные операции](docs/replies.md).

## Настройки

| Свойство | По умолчанию / назначение |
| --- | --- |
| `telegram.token` | Обязательный токен; можно передать как `TELEGRAM_TOKEN` |
| `telegram.mode` | Без свойства используется long polling. Для первого бота не задавайте его; для webhook задайте `webhook` |
| `telegram.admin-id` | Не задан; уведомления администратору пропускаются |
| `telegram.bot-id` | Не задан; нужен при использовании `@BotAddedHandle` |
| `telegram.base-names` | Пустой список; YAML-шаблоны не загружаются |
| `telegram.webhook.url` | Полный публичный HTTPS URL для регистрации webhook |
| `telegram.webhook.path` | Поле есть в настройках, но текущий `WebhookController` не использует его для изменения маршрута |

Если хотите назвать переменную токена `BOT_TOKEN`, создайте `src/main/resources/application.yml`:

```yaml
telegram:
  token: ${BOT_TOKEN}
```

Тогда задавайте `BOT_TOKEN` вместо `TELEGRAM_TOKEN`.

Без дополнительных бинов пользователи не сохраняются, команды без требований к ролям доступны, а команды с `requiredRoles` не получают разрешение от стандартного `NoOpUserService`. Планировщик и нейтральный обработчик ошибок предоставляются автоматически. Ограничения частоты запросов и блокировки остаются активными.

## Webhook

Для первого запуска рекомендуется long polling. Webhook нужен, если Telegram должен доставлять обновления на ваш публичный HTTPS-сервер.

В текущей версии регистрация webhook и подключение HTTP-контроллера являются отдельными действиями. Добавьте в `HelloBotApplication` импорт:

```java
import org.springframework.context.annotation.Import;
import ru.tggc.telegrambotspringbootstarter.WebhookController;
```

И аннотацию на класс приложения рядом с `@SpringBootApplication`:

```java
@Import(WebhookController.class)
```

Настройте `application.yml`:

```yaml
server:
  port: 8080
telegram:
  token: ${BOT_TOKEN}
  mode: webhook
  webhook:
    url: ${BOT_WEBHOOK_URL}
```

`BOT_WEBHOOK_URL` должен содержать полный адрес, например `https://bot.example.com/telegram/webhook`. Внешний HTTPS-прокси должен пересылать POST-запросы на порт приложения и путь `/telegram/webhook`. Не отключайте веб-сервер через `spring.main.web-application-type=none` в этом режиме.

Текущий контроллер имеет фиксированный маршрут `/telegram/webhook`; одного `telegram.webhook.path` недостаточно, чтобы его изменить. Автоконфигурация вызывает `setWebhook`, но проверяйте результат регистрации в журнале. Перед production-размещением добавьте проверку webhook secret: текущий контроллер её не выполняет.

Для возврата к long polling остановите webhook-экземпляр, удалите webhook через Telegram Bot API, уберите `telegram.mode` и перезапустите приложение. Сам polling-runner webhook не удаляет. Не запускайте два polling-экземпляра с одним токеном.

## Расширение библиотеки

Подключайте дополнительное поведение только когда оно требуется:

| Задача | Точка расширения |
| --- | --- |
| Пользователи, роли, сохранение данных | Spring-бин `ru.tggc.telegrambotcore.service.UserService` |
| Собственная обработка исключений | Spring-бин `ru.tggc.telegrambotcore.exception.ExceptionHandler` |
| Свой планировщик | Spring-бин `org.springframework.scheduling.TaskScheduler` |
| YAML-шаблоны | `telegram.base-names` и `FormatService` |
| Клавиатуры | `KeyboardFactory` и реализации создателей клавиатур |

Собственные `UserService`, `ExceptionHandler` и `TaskScheduler` заменяют стандартные реализации через `@ConditionalOnMissingBean`. JPA, PostgreSQL и Liquibase не нужны стартеру для простого бота: их подключает приложение, когда появляется хранение данных.

Для шаблонов укажите пути без расширения, например `telegram.base-names: [messages/common]`, и добавьте `src/main/resources/messages/common.yml`. Явно настроенный отсутствующий файл приводит к ошибке запуска.

## Частые проблемы

| Симптом | Что проверить |
| --- | --- |
| Maven не находит `0.0.1-SNAPSHOT` | Выполните `install` из корня библиотеки, не только из папки одного модуля. На другой машине тоже нужна установка либо настроенный Maven-репозиторий |
| `Set telegram.token` | Токен должен быть в окружении именно запускаемого процесса; проверьте Run Configuration в IDEA |
| `UnsupportedClassVersionError` | Используется слишком старая Java; проверьте `java -version`, `JAVA_HOME` и JDK проекта |
| Приложение работает, `/start` молчит | Проверьте пакет обработчика, `@BotHandler`, имя команды без `/`, возвращаемый `Response`, доступ к Telegram API и журнал ошибок |
| Кнопка не работает в личном чате | Для `@CallbackHandle` задайте `canPrivate = true` |
| Команда с ролью недоступна | Стандартный `NoOpUserService` не выдаёт роли; нужна собственная реализация |
| Telegram сообщает о конфликте получения обновлений | Проверьте другой экземпляр с тем же токеном и ранее зарегистрированный webhook |
| Webhook получает 404 | Импортируйте `WebhookController` и проверьте полный URL `/telegram/webhook` и маршрут прокси |
| Повторные сообщения временно не обрабатываются | Проверьте rate limit, активный диалог и блокировку пользователя на время обработки |

## Разработка и обновление

Из корня библиотеки:

```powershell
.\mvnw.cmd test
.\mvnw.cmd install
```

Тесты библиотеки используют подменённый Telegram-клиент и не требуют настоящего токена. `MinimalBotTests` проверяет минимальную конфигурацию и команду `/start`; остальные тесты покрывают Java/Kotlin API ответов, медиа, диалоги и асинхронное выполнение.

При обновлении существующего приложения учитывайте:

- `@CommandHandle` разрешает личные сообщения. Для команд только для групп задайте `canPrivate = false`.
- Стандартный файл `telegram/messages/messages.yml` не загружается автоматически: при необходимости укажите его через `telegram.base-names` без `.yml`.
- `UserDto.username` и `ChatDto.title` могут быть `null`.
- `edit(String)` сохраняет старое поведение редактирования подписи; для текста есть `editText(String)`.
- При изменении исходников повторите `install` и обновите Maven-зависимости в приложении-потребителе.
