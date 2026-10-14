# BookSharing API

[← Запуск приложения](../README.md)

Базовый адрес локального сервера: `http://localhost:8080`. Префикса `/api` у Spring-контроллеров нет; `/api` в конфигурации Vite — необязательный dev-proxy.

## Регистрация и вход

`POST /auth/register`, `Content-Type: application/json`:

```json
{
  "username": "reader_one",
  "password": "local-demo-password-42"
}
```

Имя — от 3 до 50 символов, пароль — минимум 6 символов. Пример предназначен для локального теста; выберите собственный пароль. Успех — `201` с объектом пользователя, занятое имя — `409`.

Для входа отправьте те же поля в `POST /auth/login`. Ответ `200` содержит `token`, `username`, `displayName` и `avatarUrl`. Далее передавайте токен:

```http
Authorization: Bearer <token>
```

Текущий пользователь: `GET /users/me`. Токен действует один час при стандартной конфигурации; refresh endpoint не реализован.

Пример для PowerShell:

```powershell
$base = "http://localhost:8080"
$credentials = @{ username = "reader_one"; password = "local-demo-password-42" } | ConvertTo-Json
Invoke-RestMethod "$base/auth/register" -Method Post -ContentType "application/json" -Body $credentials
$session = Invoke-RestMethod "$base/auth/login" -Method Post -ContentType "application/json" -Body $credentials
$headers = @{ Authorization = "Bearer $($session.token)" }
Invoke-RestMethod "$base/users/me" -Headers $headers
```

Регистрацию выполняйте один раз; для существующего аккаунта начинайте со входа.

## Каталог

| Метод и путь | Назначение |
| --- | --- |
| `GET /books` | Список книг |
| `GET /books/{id}` | Карточка книги |
| `GET /books/search` | Поиск с пагинацией |
| `POST /books` | Создание книги, multipart, нужен JWT |
| `DELETE /books/{id}` | Удаление собственной книги, нужен JWT |
| `POST /books/{id}/like` | Переключение отметки «нравится», нужен JWT |
| `GET /books/favorites` | Избранное текущего пользователя; передавайте JWT |

У `/books` есть параметры `search`, `rating`, `tag`, `category`. Реализация выбирает первый непустой фильтр в этом порядке. Для комбинированного поиска используйте `/books/search` с параметрами `search`, `author`, `ratingMin`, `ratingMax`, `category`, `tag`, `viewsMin`, `updatedAfter`, `page`, `size` и `sort`.

```http
GET /books/search?search=Java&page=0&size=12&sort=title,asc
```

`/books` возвращает массив, а `/books/search` — объект Spring Page с `content` и метаданными пагинации.

### Создание книги

`POST /books` принимает **multipart/form-data** с частью `book`, содержащей JSON-строку, и необязательной частью `file`:

```json
{
  "title": "A Study in Scarlet",
  "author": "Arthur Conan Doyle",
  "year": 1887,
  "description": "A detective novel",
  "tags": ["detective"],
  "category": "Fiction",
  "isPublic": true
}
```

Сохраните этот JSON в `book.json`. В Bash, с токеном своего аккаунта в переменной `TOKEN`:

```bash
curl --fail-with-body http://localhost:8080/books \
  -H "Authorization: Bearer $TOKEN" \
  -F 'book=<book.json'
```

Для загрузки файла добавьте `-F 'file=@cover.jpg'`. На Windows используйте `curl.exe` и `$env:TOKEN` вместо `$TOKEN`; команда в одну строку:

```powershell
curl.exe --fail-with-body http://localhost:8080/books -H "Authorization: Bearer $env:TOKEN" -F "book=<book.json"
```

После входа из примера выше токен можно задать как `$env:TOKEN = $session.token`. Не указывайте `Content-Type` multipart вручную: curl добавит boundary. Сервер разрешает JPEG, PNG и PDF размером до 50 МБ. Успешный ответ — `201` с `BookDto`.

В запросе создания нет поля `qrCode`, и сервер пока не присваивает его автоматически. Поэтому для новой книги QR-код недоступен: существующие QR-эндпоинты требуют заполненного значения в данных. `PUT /books/{id}` в текущем сервере отсутствует.

## Обмены

Все запросы этого раздела выполняются с JWT. Для создания отправьте `POST /exchanges`:

```json
{
  "requestedBookId": 1,
  "offeredBookId": 2,
  "message": "Would you like to exchange books?"
}
```

Замените ID на реальные: `requestedBookId` — чужая книга, `offeredBookId` — ваша книга взамен. `offeredBookId` можно не передавать. Ответ — `201` с объектом обмена.

| Метод и путь | Действие |
| --- | --- |
| `GET /exchanges` | Обмены текущего пользователя |
| `GET /exchanges/{id}` | Карточка обмена |
| `POST /exchanges/{id}/accept` | Владелец принимает предложение |
| `POST /exchanges/{id}/reject` | Владелец отклоняет предложение |
| `POST /exchanges/{id}/cancel` | Инициатор отменяет ожидающее предложение |
| `POST /exchanges/{id}/mark-shipped` | Владелец отмечает отправку |
| `POST /exchanges/{id}/mark-delivered` | Получатель отмечает доставку |
| `POST /exchanges/{id}/confirm-receipt` | Участник подтверждает получение |

В `mark-shipped` можно передать JSON `{"trackingNumber":"LOCAL-001"}`. Остальные действия не требуют тела. Обычная последовательность статусов:

```text
PENDING → WAITING_FOR_SHIPMENT → IN_TRANSIT → DELIVERED → COMPLETED
```

Для завершения нужны подтверждения обоих участников. `REJECTED` и `CANCELLED` завершают предложение без передачи книг.

## Другие операции

| Метод и путь | Назначение |
| --- | --- |
| `GET /books/{id}/comments` | Комментарии |
| `POST /books/{id}/comments` | Комментарий, JSON `{"text":"My review"}`, нужен JWT |
| `POST /books/{id}/rating` | Оценка, JSON `{"score":5,"liked":true}`, нужен JWT |
| `GET /books/{id}/stats` | Статистика оценок |
| `GET /qrcode/book/{id}` | QR-код книги, изображение PNG |
| `GET /qrcode/scan/{qrCode}` | Поиск книги по значению QR-кода |
| `PUT /users/profile` | Профиль: multipart-части `profile` и/или `avatar`, нужен JWT |
| `PUT /users/privacy` | Настройки приватности, нужен JWT |

Это справочник основных сценариев, а не полная OpenAPI-спецификация. Остальные маршруты находятся в [controller/](../src/main/java/org/example/booksharing/controller/). Наличие эндпоинта не означает, что для него есть отдельный экран в React.
