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
