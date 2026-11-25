# Платформа конвертации XML → JSON

Два сервиса Spring Boot на Java 21 обмениваются JSON-событиями через RabbitMQ. Веб-интерфейс обслуживает API gateway.

## 1. Запуск

В корне проекта выполните:

```sh
docker compose up --build
```

Дождитесь запуска обоих приложений. UI доступен на [localhost:8080](http://localhost:8080/), RabbitMQ Management — на [localhost:15672](http://localhost:15672/) (логин и пароль `admin`).

## 2. Сценарий через curl

Примеры команд рассчитаны на Bash. Отправьте XML:

```sh
curl -i -X POST http://localhost:8080/api/v1/conversions \
  -H 'Content-Type: application/json' \
  -d '{"xml":"<note><to>Alice</to><body>Hi</body></note>"}'
```

API сразу после подтверждения от RabbitMQ возвращает `202 Accepted` и заголовок `Location: /api/v1/conversions/<id>`. Пример тела:

```json
{"id":"0b39b07f-64b5-4cdb-9063-6f03c9e719c1","status":"PENDING"}
```

Подставьте полученный UUID и сразу проверьте статус:

```sh
ID=0b39b07f-64b5-4cdb-9063-6f03c9e719c1
curl "http://localhost:8080/api/v1/conversions/$ID"
```

```json
{"id":"0b39b07f-64b5-4cdb-9063-6f03c9e719c1","status":"PENDING","submittedAt":"2026-11-25T12:00:00Z","completedAt":null,"jsonResult":null}
```

Подождите около трёх секунд и повторите запрос:

```sh
sleep 3
curl "http://localhost:8080/api/v1/conversions/$ID"
```

```json
{"id":"0b39b07f-64b5-4cdb-9063-6f03c9e719c1","status":"COMPLETED","submittedAt":"2026-11-25T12:00:00Z","completedAt":"2026-11-25T12:00:02.500Z","jsonResult":"{\"to\":\"Alice\",\"body\":\"Hi\"}"}
```

Запросите сам результат:

```sh
curl -i "http://localhost:8080/api/v1/conversions/$ID/result"
```

Ответ `200 OK` с `Content-Type: application/json`:

```json
{"to":"Alice","body":"Hi"}
```

Для неизвестного UUID оба GET возвращают `404`. Запрос результата до завершения возвращает `409`. Пустой, некорректный XML, XML с DOCTYPE и XML больше 1 МиБ в UTF-8 возвращают `400`.

## 3. Сценарий через UI

Откройте [localhost:8080](http://localhost:8080/), вставьте XML или нажмите **Load example**, затем **Convert**. В таблице появится запись со статусом **PENDING**. Страница опрашивает статусы раз в секунду, пока есть незавершённые заявки. Примерно через три секунды появится **COMPLETED**. Нажмите **View JSON**, чтобы открыть отформатированный результат. История хранится в памяти открытой страницы.

## 4. Где смотреть очереди

В [RabbitMQ Management](http://localhost:15672/) откройте **Queues and Streams**. Там видны durable-очереди `xml.conversion.requests` и `json.conversion.results`. На вкладке **Connections** видны два подключения, по одному на сервис.

Контракты обмена проходят через direct exchange `converter.exchange`. Поля `id` передаются как UUID, даты — как строки ISO 8601, полезная нагрузка — как строки.

| Очередь | Routing key | Событие |
| --- | --- | --- |
| `xml.conversion.requests` | `conversion.requested` | `ConversionSubmitted(id, xmlPayload, submittedAt)` |
| `json.conversion.results` | `conversion.completed` | `ConversionCompleted(id, jsonResult, completedAt)` |

## 5. Почему POST отвечает 202

Приём и обработка разделены. Gateway валидирует XML, создаёт `ConversionRequest` в памяти и ждёт подтверждения маршрутизированной публикации от RabbitMQ. После этого он возвращает `202`. Worker конвертирует XML независимо, а клиент опрашивает URL из `Location`. При ошибке подтверждения публикации API возвращает `503`.

## 6. Гарантия доставки

RabbitMQ доставляет запросы worker'у по модели **at-least-once**. Worker отправляет `basicAck` только после того, как RabbitMQ подтвердил публикацию и маршрутизацию `ConversionCompleted`. Если worker упадёт до `basicAck`, брокер вернёт запрос в `xml.conversion.requests`. При исключении обработки worker отправляет `basicNack` с `requeue=true`. Статусы и результаты хранятся в памяти gateway и теряются с его перезапуском.

## 7. Защита от дублей

Gateway атомарно проверяет и обновляет состояние в репозитории. Если заявка уже в `COMPLETED`, он игнорирует повторный результат и пишет WARN. Worker детерминированно преобразует один и тот же XML в тот же JSON при повторной доставке.

## 8. Проверка повторной доставки после остановки worker

Откройте очередь `xml.conversion.requests` в RabbitMQ Management. Чтобы было достаточно времени увидеть счётчики и остановить worker, временно увеличьте `converter.processing-delay-ms` в конфиге worker, например до `15000`, и пересоберите его командой `docker compose up -d --build xml-to-json-converter`.

Отправьте новую конверсию и запомните UUID. Пока worker ждёт, счётчик **Unacked** увеличится. В другом терминале выполните:

```sh
docker compose kill xml-to-json-converter
```

После закрытия соединения **Unacked** уменьшится, а сообщение перейдёт в **Ready**. Запустите worker снова:

```sh
docker compose up xml-to-json-converter
```

В логах появится `received conversion <id> (redelivered=true)` с тем же UUID. После завершения GET вернёт `COMPLETED`. Верните задержку `2500` и пересоберите worker после эксперимента.

## 9. Структура проекта

```text
.
├── docker-compose.yml
├── README.md
├── conversion-api-gateway/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/converter/gateway/
│       │   ├── api/             REST, DTO ответов и обработка ошибок
│       │   ├── application/     Приём, чтение статуса и применение результата
│       │   ├── domain/          Заявка, статусы, события и порты
│       │   └── infrastructure/  Репозиторий в памяти, DOM-валидатор, RabbitMQ
│       └── resources/
│           ├── application.yml
│           └── static/          index.html, app.js, styles.css
└── xml-to-json-converter/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/
        ├── java/com/example/converter/worker/
        │   ├── api/             Входной DTO очереди
        │   ├── application/     Обработка заявки и отправка результата
        │   ├── domain/          Событие результата и порты
        │   └── infrastructure/  Jackson-конвертер и адаптеры RabbitMQ
        └── resources/
            └── application.yml
```
