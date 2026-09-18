# Telegram Bot Starter

Для простого бота нужны зависимость, токен и обработчик команды.
Собственные сервисы, база данных, ID администратора, ID бота и файлы сообщений не нужны.

## Бот, который отвечает «привет» на `/start`

Требуются Java 21+ и Maven. Сначала установите текущую версию библиотеки в локальный Maven-репозиторий, выполнив в её корне:

```powershell
.\mvnw.cmd install
```

В отдельном проекте создайте `pom.xml`:

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

Создайте `src/main/java/example/HelloBotApplication.java`:

```java
package example;

import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.tggc.telegrambotcore.annotation.handle.BotHandler;
import ru.tggc.telegrambotcore.annotation.handle.CommandHandle;
import ru.tggc.telegrambotcore.annotation.params.ChatId;
import ru.tggc.telegrambotcore.dto.Response;

@SpringBootApplication(proxyBeanMethods = false)
@BotHandler
public class HelloBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloBotApplication.class, args);
    }

    @CommandHandle("start")
    public Response start(@ChatId Long chatId) {
        return Response.of(new SendMessage(chatId, "привет"));
    }
}
```

Получите токен у BotFather и задайте его при запуске. Не сохраняйте настоящий токен в исходниках:

```powershell
$env:TELEGRAM_TOKEN = 'токен-бота'
mvn spring-boot:run
```

Файл `application.yml` не обязателен. Если удобнее использовать другую переменную окружения, можно создать `src/main/resources/application.yml`:

```yaml
telegram:
  token: ${BOT_TOKEN}
```

В этом случае задайте `BOT_TOKEN` вместо `TELEGRAM_TOKEN`.
Библиотека сама запускает long polling. Отправьте боту `/start` в личном чате: он ответит «привет».
В `@CommandHandle` имя команды записывается без `/`. Сейчас обработчик возвращает `Response`; возврат обычной строки пока не поддерживается.

## Что происходит без дополнительных настроек

| Возможность | Поведение по умолчанию |
| --- | --- |
| Получение сообщений | Long polling; `telegram.mode` не требуется |
| Пользователи | Данные не сохраняются; база данных не требуется |
| Роли | Команды без требований к ролям доступны; требующие роль получают отказ |
| Ошибки обработчика | Ошибка записывается в журнал; пользователю отправляется нейтральное сообщение |
| Отложенная отправка | Планировщик создаётся автоматически, если своего нет |
| Уведомления администратору | Без `telegram.admin-id` отправка администратору пропускается |
| YAML-шаблоны сообщений | По умолчанию файлы не загружаются |
| Личные сообщения | `@CommandHandle` разрешает их по умолчанию |

Ограничение частоты запросов и блокировка пользователя, уже существующие в библиотеке, пока остаются включёнными.

## Подключайте по необходимости

- **Сохранение пользователей и свои роли:** зарегистрируйте Spring-бин `UserService`. Стандартная реализация автоматически уступит ему место.
- **Своя обработка исключений:** зарегистрируйте бин `ExceptionHandler`.
- **Свой планировщик:** зарегистрируйте бин `TaskScheduler`.
- **Сообщения администратору:** укажите `telegram.admin-id`.
- **Обработчик добавления бота в группу:** при использовании `@BotAddedHandle` укажите `telegram.bot-id`. Для обычных команд он не нужен.
- **Шаблоны:** укажите `telegram.base-names`, например `[telegram/messages/messages]`, и добавьте соответствующий `.yml` в ресурсы. Явно заданный отсутствующий файл считается ошибкой настройки.
- **Только групповые команды:** используйте `@CommandHandle(value = "start", canPrivate = false)`.

## Изменения для существующих приложений

Собственные `UserService`, `ExceptionHandler` и `TaskScheduler` продолжают использоваться.
Есть два изменения значений по умолчанию, которые следует проверить при обновлении:

1. `@CommandHandle` теперь разрешает личные сообщения. Для команд только для групп явно задайте `canPrivate = false`. Требования к ролям сохраняются.
2. Стандартный путь `telegram/messages/messages.yml` больше не загружается автоматически. Если приложение использовало его без настройки, добавьте `telegram.base-names: [telegram/messages/messages]`.

`UserDto.username` и `ChatDto.title` теперь допускают `null`, как и соответствующие данные Telegram. Собственные сервисы должны учитывать отсутствие этих полей.

## Проверка библиотеки

```powershell
.\mvnw.cmd test
```

`MinimalBotTests` проверяет запуск с одним токеном, выбор транспорта, доставку `/start` из личного чата без username и названия, замену стандартных сервисов и ошибки обязательных настроек. Telegram-клиент при проверке отправки подменяется: реальные сообщения не отправляются.
