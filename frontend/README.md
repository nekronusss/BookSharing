# BookSharing — фронтенд

React-интерфейс каталога, профиля и обменов книгами. Полная инструкция с PostgreSQL и Spring Boot находится в [основном README](../README.md).

**React 18 · Vite 5 · React Router 6 · Axios · Tailwind CSS 3**

## Запуск

Понадобятся Node.js 22 или 24 с npm и работающий API на `http://localhost:8080`. Из этой папки:

```sh
npm ci
npm run dev
```

Откройте `http://localhost:4200`. Если порт занят, Vite выберет следующий свободный — точный адрес будет в терминале.

## Подключение к API

По умолчанию [API-клиент](src/services/api.js) обращается напрямую к `http://localhost:8080`. Чтобы изменить адрес, создайте `frontend/.env.local`:

```dotenv
VITE_API_URL=http://localhost:8080
```

Перезапустите Vite или пересоберите сайт. Переменные `VITE_*` видны браузеру: секреты в них не размещают.

В [vite.config.js](vite.config.js) есть dev-proxy `/api`. Для него можно установить `VITE_API_URL=/api`; в production этот proxy не работает — маршрут нужно настроить на веб-сервере. Загруженные изображения `/uploads/` также должны быть доступны через backend.

## Сборка

```sh
npm run build
npm run preview
```

`dist/` содержит статические файлы. `preview` позволяет локально проверить сборку по адресу из терминала; API должен работать отдельно. Для публичного размещения нужны HTTPS, корректный `VITE_API_URL`, CORS на API и возврат `index.html` для маршрутов React Router.

## Основные файлы

| Путь | Назначение |
| --- | --- |
| `src/App.jsx` | Маршруты и ленивое подключение страниц |
| `src/pages/` | Каталог, книга, обмены, профиль, вход и регистрация |
| `src/components/` | Навигация, карточки, формы и QR-коды |
| `src/context/AuthContext.jsx` | Состояние авторизации |
| `src/services/api.js` | Запросы Axios и JWT-заголовок |
| `src/index.css` | Стили и Tailwind |

Токен хранится в `localStorage`; при `401` клиент удаляет его и открывает вход. UI-тесты и команда lint пока не настроены. Текущие ограничения описаны в [README](../README.md#границы-текущей-версии).
