# Локальная база и запуск сервера

Нужны JDK 21, Node.js 22 или 24 с npm и PostgreSQL либо Docker. Maven отдельно устанавливать не нужно: используйте Maven Wrapper из репозитория. Для первой установки зависимостей нужен интернет.

```sh
git clone https://github.com/nekronusss/BookSharing.git
cd BookSharing
```

## PostgreSQL в Docker

При запущенном Docker Engine выполните в PowerShell или Bash:

```sh
docker run --name booksharing-db -e POSTGRES_DB=booksharing -e POSTGRES_USER=booksharing -e POSTGRES_PASSWORD=booksharing-local-only -p 127.0.0.1:55433:5432 -v booksharing-data:/var/lib/postgresql/data -d postgres:17
docker exec booksharing-db pg_isready -U booksharing -d booksharing
```

Дождитесь `accepting connections`. Пароль предназначен только для этой локальной базы. Порт 55433 не занимает стандартный порт PostgreSQL. Данные сохраняются в томе `booksharing-data`. При повторном запуске используйте `docker start booksharing-db`.

## Сервер

PowerShell, из корня репозитория:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:55433/booksharing'
$env:DB_USERNAME = 'booksharing'
$env:DB_PASSWORD = 'booksharing-local-only'
$env:JWT_SECRET = node -p "require('node:crypto').randomBytes(32).toString('hex')"
$env:DDL_AUTO = 'update'
./mvnw.cmd spring-boot:run
```

macOS/Linux, Bash:

```bash
export DB_URL=jdbc:postgresql://localhost:55433/booksharing
export DB_USERNAME=booksharing
export DB_PASSWORD=booksharing-local-only
export JWT_SECRET=$(node -p "require('node:crypto').randomBytes(32).toString('hex')")
export DDL_AUTO=update
sh mvnw spring-boot:run
```

`DDL_AUTO=update` предназначен только для отдельной локальной базы. Без него используется `validate`, а таблицы автоматически не создаются. Spring также поддерживает стандартные переменные `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` и `SPRING_DATASOURCE_PASSWORD`; они переопределяют соответствующие свойства. Обычный `.env` сервер не читает автоматически.

Дождитесь `Started BookSharingApplication`. API слушает `http://localhost:8080`. В другом терминале запустите интерфейс по [README](../README.md) и откройте `http://localhost:4200/?demo=0`. В новой реальной базе нет аккаунтов и книг; демоданные хранятся только в браузере.

## Первый реальный обмен

1. Зарегистрируйте первого пользователя и добавьте книгу.
2. В приватном окне браузера зарегистрируйте второго пользователя и предложите обмен. Вторую книгу можно предложить взамен.
3. Под первым пользователем примите предложение и отметьте отправку.
4. Под вторым отметьте доставку, затем подтвердите получение с обеих сторон.
5. Проверьте статус `COMPLETED` и смену владельцев книг.

Владелец может отклонить ожидающее предложение, инициатор — отменить. QR открывает карточку, но не подтверждает получение. Интеграции с курьерской службой нет.

## Сборка сервера

С работающей отдельной БД и переменными из инструкции:

```powershell
./mvnw.cmd test
./mvnw.cmd package
java -jar target/BookSharing-0.0.1-SNAPSHOT.jar
```

В Bash используйте `sh mvnw`. Перед запуском JAR остановите сервер разработки через Ctrl+C. Фронтенд собирается отдельно в `frontend/dist/`.

## Решение проблем

| Симптом | Что проверить |
| --- | --- |
| `release version 21 not supported` | `java -version`, `JAVA_HOME`, `./mvnw.cmd -v` должны указывать на JDK 21 |
| Нет соединения с БД | `docker ps`, `pg_isready`, адрес, порт и переменные в терминале |
| Ошибка проверки схемы на пустой БД | Для отдельной локальной базы задайте `DDL_AUTO=update` |
| Порт 8080 занят | Измените `SERVER_PORT` и адрес API/прокси или остановите другой сервер |
| PowerShell блокирует `npm.ps1` | Используйте `npm.cmd ci` и `npm.cmd run dev` |
| После перезапуска API ответ `401` | Войдите заново: токен истёк либо изменился `JWT_SECRET` |
| Создание книги возвращает `415` | Нужен multipart; пример в [API.md](API.md) |
| Не работает камера | Разрешите доступ; вне localhost нужен HTTPS |

Для остановки завершите сервер и Vite через Ctrl+C, затем выполните `docker stop booksharing-db`. Для переноса приложения сохраняйте БД и каталог `uploads/`. Повторная генерация JWT-секрета делает старые токены недействительными.
