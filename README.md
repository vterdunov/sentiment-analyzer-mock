# Sentiment Analyzer Mock

REST API приложение на Java (Spring Boot) для mock-анализа тональности текста, развёртываемое в Kubernetes.

## Описание

Приложение предоставляет REST API эндпоинт для анализа тональности текста:

```
GET /api/sentiment?text=hello
```

Ответ:
```json
{"sentiment": "positive"}
```

Возможные значения sentiment: `positive`, `negative`, `neutral`

## Технологический стек

- **Java 21**
- **Spring Boot 3.3**
- **Spring Boot Actuator** (health, metrics)
- **Micrometer + Prometheus** (метрики)
- **Docker** (multi-stage build, Alpine)
- **Kubernetes** (Minikube)
- **Prometheus + Grafana** (мониторинг)

## Структура проекта

```
sentiment-analyzer-mock/
├── pom.xml                          # Maven конфигурация
├── Dockerfile                       # Multi-stage сборка
├── .github/workflows/
│   └── build-push.yml               # CI/CD pipeline
├── k8s/
│   ├── namespace.yaml               # Namespace
│   ├── deployment.yaml              # Deployment (3 реплики)
│   ├── service.yaml                 # Service (LoadBalancer)
│   ├── ingress.yaml                 # Ingress
│   ├── hpa.yaml                     # HorizontalPodAutoscaler
│   └── servicemonitor.yaml          # ServiceMonitor для Prometheus
├── grafana/
│   ├── datasources/
│   │   └── prometheus-datasource.yaml
│   └── dashboards/
│       └── sentiment-app-dashboard.json
└── src/main/java/com/example/sentiment/
    ├── SentimentApplication.java
    ├── controller/SentimentController.java
    ├── service/SentimentService.java
    └── model/SentimentResponse.java
```

## Быстрый старт

### Предварительные требования

- Docker
- Minikube
- kubectl
- Helm

### 1. Установка Minikube

```bash
# Запуск кластера
minikube start --cpus=4 --memory=8192mb --nodes=2

# Включение аддонов
minikube addons enable ingress
minikube addons enable metrics-server
```

### 2. Установка Prometheus + Grafana

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
kubectl create namespace monitoring
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring
```

### 3. Сборка Docker образа

```bash
# Сборка внутри Minikube
eval $(minikube docker-env)
docker build -t sentiment-app:latest .

# Проверка размера образа (должен быть < 150MB)
docker images | grep sentiment-app
```

### 4. Развёртывание в Kubernetes

```bash
# Применение всех манифестов
kubectl apply -f k8s/

# Проверка статуса
kubectl get all -n sentiment-app
```

### 5. Тестирование API

```bash
# Получение URL сервиса
minikube service sentiment-service -n sentiment-app --url

# Тест API
curl "$(minikube service sentiment-service -n sentiment-app --url)/api/sentiment?text=hello"
# Ожидаемый ответ: {"sentiment":"positive"}

curl "$(minikube service sentiment-service -n sentiment-app --url)/api/sentiment?text=bad"
# Ожидаемый ответ: {"sentiment":"negative"}
```

### 6. Доступ к мониторингу

```bash
# Grafana
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
# Откройте http://localhost:3000
# Логин: admin
# Пароль: prom-operator (или получите из секрета)

# Получить пароль Grafana
kubectl get secret -n monitoring prometheus-grafana \
  -o jsonpath="{.data.admin-password}" | base64 --decode; echo
```

## API Endpoints

| Endpoint | Метод | Описание |
|----------|-------|----------|
| `/api/sentiment?text=...` | GET | Анализ тональности текста |
| `/actuator/health` | GET | Health check |
| `/actuator/prometheus` | GET | Prometheus метрики |
| `/actuator/info` | GET | Информация о приложении |

## Kubernetes ресурсы

| Ресурс | Описание |
|--------|----------|
| Namespace | `sentiment-app` |
| Deployment | 3 реплики, resource limits |
| Service | LoadBalancer, port 80 |
| Ingress | nginx, host: sentiment.local |
| HPA | min: 3, max: 10, CPU target: 50% |
| ServiceMonitor | Prometheus scraping |

## GitHub Actions

При push в main/master автоматически:
1. Собирается Docker образ
2. Образ пушится в GitHub Container Registry (ghcr.io)

```bash
# Использование образа из ghcr.io
docker pull ghcr.io/<username>/sentiment-analyzer-mock:latest
```

## Мониторинг

### Prometheus метрики

Приложение экспортирует метрики через `/actuator/prometheus`:

- `http_server_requests_seconds_*` - HTTP запросы
- `jvm_memory_*` - JVM память
- `jvm_gc_*` - Garbage Collection
- `system_cpu_*` - CPU использование

### Grafana Dashboard

Импортируйте dashboard из `grafana/dashboards/sentiment-app-dashboard.json`:

- HTTP Request Rate
- HTTP Response Latency (p95)
- JVM Memory Usage
- Running Pods
- CPU Usage %

## Проверка HPA

```bash
# Смотреть состояние HPA
kubectl get hpa -n sentiment-app -w

# Создать нагрузку для проверки автоскейлинга
URL=$(minikube service sentiment-service -n sentiment-app --url)
while true; do
  curl -s "$URL/api/sentiment?text=test" > /dev/null
done
```

## Troubleshooting

### Pods не запускаются

```bash
kubectl describe pod -n sentiment-app
kubectl logs -n sentiment-app -l app=sentiment-app
```

### HPA не работает

```bash
# Проверить metrics-server
kubectl top pods -n sentiment-app

# Если metrics недоступны
minikube addons enable metrics-server
```

### ServiceMonitor не подхватывается

```bash
# Проверить labels
kubectl get servicemonitor -n sentiment-app -o yaml

# ServiceMonitor должен иметь label: release: prometheus
```
