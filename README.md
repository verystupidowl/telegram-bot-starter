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

## Ожидание ответа сервера по кнопке

Если сервис возвращает `CompletableFuture<Report>`, используйте `ctx.await(...)`.
Обработчик по-прежнему возвращает `Response`; дополнительных сервисов и настроек не требуется:

```java
@CallbackHandle(value = "load_report", canPrivate = true)
public Response loadReport(@Ctx UpdateContext ctx) {
    return ctx.await(() -> reportService.loadReport())
        .loading("⏳ Загружаю отчёт…")
        .timeout(Duration.ofSeconds(30))
        .onSuccess(report -> ctx.send(report.text()))
        .onError(error -> ctx.send("Не удалось получить отчёт. Попробуйте позже."));
}
```

Для этого примера нужны `java.time.Duration`, `ru.tggc.telegrambotcore.annotation.handle.CallbackHandle`,
`ru.tggc.telegrambotcore.annotation.params.Ctx`, `ru.tggc.telegrambotcore.dto.UpdateContext`
и `ru.tggc.telegrambotcore.dto.Response`. `reportService` — ваш сервис, `Report` — ваш тип результата.

Последовательность: подтверждение нажатия → сообщение загрузки → вызов сервиса → ответ или ошибка → удаление сообщения загрузки.
Итоговый ответ отправляется отдельным сообщением: это позволяет вернуть не только текст, но и фото или другой `Response`.

- `onSuccess(...)` обязателен. `loading(...)` необязателен; без него дополнительных сообщений нет.
- Таймаут ожидания сервиса по умолчанию — 30 секунд. `timeout(...)` меняет его для конкретного запроса.
- Без `onError(...)` ошибка передаётся существующему `ExceptionHandler` приложения.
- Пока ответ выполняется, повторный запрос того же пользователя не запускает ещё один сервисный вызов. Блокировка действует в пределах одного экземпляра приложения и снимается при завершении, ошибке, таймауте или отмене. Другие обновления этого пользователя также проходят существующую проверку блокировки.
- После таймаута поздний результат не отправляется. Исходный future сервиса не изменяется и не отменяется: другие его потребители не затрагиваются. Таймаут HTTP-клиента настраивается отдельно.
- Сервис должен быстро вернуть future. Не используйте внутри переданного supplier блокирующие `get()`/`join()` или длительную синхронную работу.
- Ошибка отображения загрузки не отменяет сам запрос. Ошибка удаления загрузки записывается в журнал и не заменяет результат запроса.
- Ожидание сервиса не блокирует поток обработки обновлений. Отправка и удаление сообщений ограничены сетевыми таймаутами Telegram-клиента, а не `timeout(...)` сервиса.

Существующие обработчики, `sendWithLoader(...)` и обычные `Response` сохраняют своё поведение.
Новая реализация `AsyncResponse` реализует `Response`; обработчики не нужно переводить на возврат `CompletableFuture<Response>`.
У нового ответа `andThen(...)` ждёт завершения предыдущего шага. Если новый ответ добавлен через существующий `ResponseBuilder.add(...)`
или `Response.andThen(...)`, цепочка также отслеживается до завершения. Цепочки, состоящие только из прежних `Response`, не меняются.

### Разные ответы для разных исключений

`onError` принимает класс исключения и обработчик с этим типом аргумента — приведение типов не нужно:

```java
return ctx.await(() -> reportService.loadReport())
    .loading("⏳ Загружаю отчёт…")
    .onSuccess(report -> ctx.send(report.text()))
    .onError(GeniusResponse.class, ex -> ctx.send(ex.getDTO().getMessage()))
    .onError(TimeoutException.class, ex -> ctx.send("Сервер долго отвечает. Попробуйте позже."))
    .onError(ex -> ctx.send("Не удалось получить отчёт."));
```

Здесь `GeniusResponse` — ваш класс исключения, например наследник `RuntimeException`, содержащий DTO.
Если это обычный DTO ответа, оберните его в своё исключение, например `GeniusException`, и перехватывайте это исключение.
`TimeoutException` импортируется из `java.util.concurrent`.

Правила выбора:

- Обработчик типа подходит также для его наследников.
- Выбирается самый конкретный подходящий тип: `GeniusResponse` имеет приоритет над `RuntimeException`, независимо от порядка регистрации.
- `CompletionException` и `ExecutionException` снимаются автоматически; обработчик получает исходное исключение с его полями. Произвольные бизнес-исключения не заменяются их `cause`.
- Обычный `onError(ex -> ...)` — запасной обработчик, если ни один тип не подошёл. Если его нет, исключение передаётся существующему `ExceptionHandler` приложения.
- Для одной ошибки выполняется только один обработчик. Если он сам бросит исключение, оно передаётся дальше, без повторного перебора локальных обработчиков.
- Повторный `onError` для того же класса заменяет его обработчик в новой цепочке; ранее созданная цепочка не изменяется.

Существующий `onError(ex -> ...)` сохраняет прежнее использование. Типизированные обработчики перехватывают ошибки сервиса и формирования ответа; отмена всего ответа и ошибки отправки в Telegram обрабатываются как прежде.

## Проверка библиотеки

```powershell
.\mvnw.cmd test
```

`MinimalBotTests` проверяет запуск с одним токеном, выбор транспорта, доставку `/start` из личного чата без username и названия, замену стандартных сервисов и ошибки обязательных настроек. Telegram-клиент при проверке отправки подменяется: реальные сообщения не отправляются.

`AsyncResponseTest` проверяет Java API, загрузку, ошибки, таймаут, отмену, поздний результат, очистку и совместимость цепочек.
`AsyncBotTests` проверяет нажатия кнопок через маршрутизатор, подтверждение callback, защиту от повторного запуска и освобождение блокировки.
