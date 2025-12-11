# Netflix Zuul gateway example

Минимальный пример шлюза на Spring Boot с Netflix Zuul, проксирующего запросы в Kubernetes-инфраструктуру (для демонстрации — на собственный hello-эндпоинт).

## Запуск

```bash
mvn spring-boot:run
```

Сервис по умолчанию поднимается на порту `8080`.

## Проверка проксирования

1. Получить ответ самого шлюза:
   ```bash
   curl http://localhost:8080/
   ```
2. Пройти через Zuul к внутреннему hello-эндпоинту (проксирование на сам сервис):
   ```bash
   curl http://localhost:8080/proxy/hello
   ```
   Запрос с префиксом `/proxy` будет отправлен Zuul на `http://localhost:8080/internal/hello`.

В реальной k8s-среде вместо localhost можно указать адреса нужных сервисов и namespace в настройке маршрутов (`src/main/resources/application.yml`).

## Локальный запуск APISIX с Keycloak и Dashboard

Для проверки OpenID Connect можно поднять шлюз APISIX и Keycloak через Docker Compose.

```bash
docker compose up -d
```

В составе разворачиваются два контейнера:

- `keycloak` — доступен на `http://localhost:18080`, учётные данные администратора: `admin`/`admin`. Импортируется примерный realm `apisix` с клиентом `apisix` (секрет `secret`) и тестовым пользователем `user`/`password`.
- `etcd` — хранит конфигурацию APISIX и используется дашбордом.
- `apisix` — доступен на `http://localhost:9080` (админ API — `http://localhost:9180`). При старте выполняется автоматическое создание маршрута `/*` с плагином `openid-connect`, перенаправляющим запросы на Keycloak и проксирующим трафик на локально запущенный Zuul (`host.docker.internal:8080`).
- `apisix-dashboard` — панель управления APISIX с доступом через браузер на `http://localhost:9000` (логин/пароль: `admin`/`admin`).

Перед проверкой запустите сам сервис Zuul:

```bash
mvn spring-boot:run
```

Затем запросите любой маршрут через APISIX, например:

```bash
curl -i http://localhost:9080/proxy/hello
```

Браузерный запрос к тому же адресу перенаправит на страницу авторизации Keycloak; для ручной проверки используйте пользователя `user` с паролем `password`.
