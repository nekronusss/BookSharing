import { demoSeed } from "./demoSeed.js";
const STORAGE = "booksharing-demo-data-v1";
const now = () => new Date().toISOString();
function read() {
  try {
    const data = JSON.parse(localStorage.getItem(STORAGE));
    if (data?.books && data?.users) return data;
  } catch {}
  const data = demoSeed();
  localStorage.setItem(STORAGE, JSON.stringify(data));
  return data;
}
function fail(message, status = 400) {
  const error = new Error(message);
  error.response = { status, data: { message } };
  throw error;
}
export async function demoAdapter(config) {
  try {
    return await handle(config);
  } catch (error) {
    error.config = config;
    throw error;
  }
}
async function handle(config) {
  await new Promise((resolve) => setTimeout(resolve, 120));
  const db = read(),
    token = localStorage.getItem("demo-token") || "",
    username = token.replace(/^demo:/, ""),
    user = db.users.find((u) => u.username === username);
  const path = config.url.split("?")[0],
    method = (config.method || "get").toLowerCase();
  let body = config.data;
  if (typeof body === "string")
    try {
      body = JSON.parse(body);
    } catch {}
  body = body || {};
  const params = config.params || {},
    required = () => {
      if (!user) fail("Войдите в аккаунт", 401);
      return user;
    };
  const findBook = (id) =>
    db.books.find((b) => b.id === Number(id)) || fail("Книга не найдена", 404);
  const visible = (b) =>
    b.username === username ||
    (b.isPublic !== false &&
      !db.users.find((u) => u.username === b.username)?.privateBooks);
  const view = (b) => ({
    ...b,
    isLikedByUser: (db.likes[username] || []).includes(b.id),
    userScore: db.ratings[username + ":" + b.id] || 0,
  });
  const detail = (e) => ({
    ...e,
    canAccept: e.status === "PENDING" && e.offererUsername === username,
    canReject: e.status === "PENDING" && e.offererUsername === username,
    canCancel: e.status === "PENDING" && e.requesterUsername === username,
    canMarkAsShipped:
      e.status === "WAITING_FOR_SHIPMENT" && e.offererUsername === username,
    canMarkAsDelivered:
      e.status === "IN_TRANSIT" && e.requesterUsername === username,
    canConfirmReceipt:
      e.status === "DELIVERED" &&
      ((e.requesterUsername === username && !e.requesterConfirmedReceipt) ||
        (e.offererUsername === username && !e.offererConfirmedReceipt)),
  });
  let data;
  if (path === "/auth/login") {
    const account = db.users.find((u) => u.username === body.username);
    if (!account || body.password !== "demo123")
      fail("Для демо используйте alina или mark и пароль demo123", 401);
    data = { ...account, token: "demo:" + account.username };
  } else if (path === "/auth/register") {
    if (db.users.some((u) => u.username === body.username))
      fail("Это имя уже занято", 409);
    if (body.password !== "demo123")
      fail(
        "В демонстрации используйте пароль demo123. Не вводите настоящий пароль.",
      );
    data = {
      id: Date.now(),
      username: body.username,
      displayName: body.username,
    };
    db.users.push(data);
  } else if (path === "/users/me") {
    data = required();
  } else if (path === "/users/profile") {
    required();
    const profile = JSON.parse(body.get("profile") || "{}");
    Object.assign(user, {
      displayName: profile.displayName,
      email: profile.email,
    });
    data = user;
  } else if (path === "/users/privacy") {
    Object.assign(required(), body);
    data = {};
  } else if (path === "/users/history") {
    required();
    data = [];
  } else if (path === "/books/favorites") {
    required();
    data = db.books
      .filter((b) => (db.likes[username] || []).includes(b.id) && visible(b))
      .map(view);
  } else if (
    (path === "/books" || path === "/books/search") &&
    method === "get"
  ) {
    let books = db.books.filter(visible);
    if (params.mine) {
      required();
      books = books.filter((b) => b.username === username);
    }
    if (params.search) {
      const q = params.search.toLowerCase();
      books = books.filter((b) =>
        (b.title + " " + b.author).toLowerCase().includes(q),
      );
    }
    if (params.category)
      books = books.filter((b) => b.category === params.category);
    if (params.tag) books = books.filter((b) => b.tags.includes(params.tag));
    const [key, order] = String(params.sort || "addedDate,desc").split(",");
    books.sort(
      (a, b) =>
        (typeof a[key] === "number"
          ? a[key] - b[key]
          : String(a[key]).localeCompare(String(b[key]), "ru")) *
        (order === "desc" ? -1 : 1),
    );
    const size = Number(params.size) || 12,
      page = Number(params.page) || 0;
    data =
      path === "/books"
        ? books.map(view)
        : {
            content: books.slice(page * size, (page + 1) * size).map(view),
            totalElements: books.length,
            totalPages: Math.ceil(books.length / size),
            number: page,
          };
  } else if (
    (path === "/books" && method === "post") ||
    (/^\/books\/\d+$/.test(path) && method === "put")
  ) {
    required();
    const values = JSON.parse(body.get("book"));
    if (!values.title?.trim() || !values.author?.trim())
      fail("Укажите название и автора");
    const book =
      method === "put"
        ? findBook(path.split("/")[2])
        : {
            id: Date.now(),
            username,
            userId: user.id,
            likesCount: 0,
            rating: 0,
            views: 0,
            addedDate: now().slice(0, 10),
            qrCode: "BOOK-" + Date.now(),
          };
    if (book.username !== username)
      fail("Редактировать книгу может только владелец", 403);
    Object.assign(book, values);
    const file = body.get("file");
    if (file?.size) {
      if (file.size > 2 * 1024 * 1024)
        fail("В демо обложка должна быть не больше 2 МБ");
      book.imageUrl = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(file);
      });
    }
    if (method === "post") db.books.unshift(book);
    data = view(book);
  } else if (/^\/books\/\d+$/.test(path)) {
    const book = findBook(path.split("/")[2]);
    if (!visible(book)) fail("Книга не найдена", 404);
    if (method === "delete") {
      required();
      if (book.username !== username)
        fail("Удалить книгу может только владелец", 403);
      if (
        db.exchanges.some((e) =>
          [e.requestedBookId, e.offeredBookId].includes(book.id),
        )
      )
        fail("Книга связана с историей обменов. Скройте её из каталога.");
      db.books = db.books.filter((b) => b.id !== book.id);
      data = {};
    } else {
      book.views++;
      data = view(book);
    }
  } else if (/^\/books\/\d+\/like$/.test(path)) {
    required();
    const book = findBook(path.split("/")[2]);
    const likes = db.likes[username] || [];
    const liked = likes.includes(book.id);
    db.likes[username] = liked
      ? likes.filter((id) => id !== book.id)
      : [...likes, book.id];
    book.likesCount += liked ? -1 : 1;
    data = view(book);
  } else if (/^\/books\/\d+\/comments$/.test(path)) {
    const book = findBook(path.split("/")[2]);
    if (method === "post") {
      required();
      if (!body.text?.trim()) fail("Напишите комментарий");
      data = {
        id: Date.now(),
        bookId: book.id,
        text: body.text,
        authorUsername: username,
        createdAt: now(),
      };
      db.comments.push(data);
    } else data = db.comments.filter((c) => c.bookId === book.id);
  } else if (/^\/books\/comments\/\d+$/.test(path)) {
    required();
    const comment = db.comments.find(
      (c) => c.id === Number(path.split("/")[3]),
    );
    if (!comment) fail("Комментарий не найден", 404);
    if (comment.authorUsername !== username) fail("Нет доступа", 403);
    if (method === "delete")
      db.comments = db.comments.filter((c) => c !== comment);
    else comment.text = body.text;
    data = comment;
  } else if (/^\/books\/\d+\/rating$/.test(path)) {
    required();
    const book = findBook(path.split("/")[2]);
    if (method === "delete") delete db.ratings[username + ":" + book.id];
    else {
      if (body.score < 1 || body.score > 5) fail("Оценка от 1 до 5");
      db.ratings[username + ":" + book.id] = body.score;
    }
    data = { score: body.score };
  } else if (path.startsWith("/qrcode/scan/")) {
    const book = db.books.find(
      (b) =>
        b.qrCode === decodeURIComponent(path.slice("/qrcode/scan/".length)),
    );
    if (!book || !visible(book)) fail("QR-код не найден", 404);
    data = view(book);
  } else if (path === "/exchanges" && method === "get") {
    required();
    data = db.exchanges
      .filter((e) =>
        [e.requesterUsername, e.offererUsername].includes(username),
      )
      .map(detail)
      .reverse();
  } else if (path === "/exchanges" && method === "post") {
    required();
    const requested = findBook(body.requestedBookId),
      offered = body.offeredBookId ? findBook(body.offeredBookId) : null;
    if (requested.username === username) fail("Нельзя запросить свою книгу");
    if (!visible(requested)) fail("Книга недоступна");
    if (offered && offered.username !== username) fail("Предложите свою книгу");
    if (
      db.exchanges.some(
        (e) =>
          e.requestedBookId === requested.id &&
          e.requesterUsername === username &&
          e.status === "PENDING",
      )
    )
      fail("Заявка на эту книгу уже отправлена");
    data = {
      id: Date.now(),
      requesterId: user.id,
      requesterUsername: username,
      offererId: requested.userId,
      offererUsername: requested.username,
      requestedBookId: requested.id,
      requestedBookTitle: requested.title,
      offeredBookId: offered?.id,
      offeredBookTitle: offered?.title,
      message: body.message,
      status: "PENDING",
      createdAt: now(),
      requesterConfirmedReceipt: false,
      offererConfirmedReceipt: false,
    };
    db.exchanges.push(data);
    data = detail(data);
  } else if (/^\/exchanges\/\d+/.test(path)) {
    required();
    const [, , id, action] = path.split("/");
    const e = db.exchanges.find((e) => e.id === Number(id));
    if (!e) fail("Обмен не найден", 404);
    if (![e.requesterUsername, e.offererUsername].includes(username))
      fail("Нет доступа к обмену", 403);
    if (method === "post") {
      const d = detail(e);
      const allowed = {
        accept: d.canAccept,
        reject: d.canReject,
        cancel: d.canCancel,
        "mark-shipped": d.canMarkAsShipped,
        "mark-delivered": d.canMarkAsDelivered,
        "confirm-receipt": d.canConfirmReceipt,
      };
      if (!allowed[action]) fail("Действие недоступно");
      if (action === "accept") {
        const ids = [e.requestedBookId, e.offeredBookId].filter(Boolean);
        if (
          findBook(e.requestedBookId).username !== e.offererUsername ||
          (e.offeredBookId &&
            findBook(e.offeredBookId).username !== e.requesterUsername)
        )
          fail("Владелец книги изменился");
        if (
          db.exchanges.some(
            (other) =>
              other.id !== e.id &&
              ["WAITING_FOR_SHIPMENT", "IN_TRANSIT", "DELIVERED"].includes(
                other.status,
              ) &&
              [other.requestedBookId, other.offeredBookId].some((id) =>
                ids.includes(id),
              ),
          )
        )
          fail("Книга уже участвует в другом обмене");
        e.status = "WAITING_FOR_SHIPMENT";
        e.acceptedAt = now();
      }
      if (action === "reject") e.status = "REJECTED";
      if (action === "cancel") e.status = "CANCELLED";
      if (action === "mark-shipped") {
        e.status = "IN_TRANSIT";
        e.trackingNumber = body.trackingNumber;
      }
      if (action === "mark-delivered") e.status = "DELIVERED";
      if (action === "confirm-receipt") {
        e[
          username === e.requesterUsername
            ? "requesterConfirmedReceipt"
            : "offererConfirmedReceipt"
        ] = true;
        if (e.requesterConfirmedReceipt && e.offererConfirmedReceipt) {
          e.status = "COMPLETED";
          e.completedAt = now();
          Object.assign(findBook(e.requestedBookId), {
            username: e.requesterUsername,
            userId: e.requesterId,
          });
          if (e.offeredBookId)
            Object.assign(findBook(e.offeredBookId), {
              username: e.offererUsername,
              userId: e.offererId,
            });
          db.exchanges
            .filter(
              (other) =>
                other.id !== e.id &&
                other.status === "PENDING" &&
                [other.requestedBookId, other.offeredBookId].some((id) =>
                  [e.requestedBookId, e.offeredBookId]
                    .filter(Boolean)
                    .includes(id),
                ),
            )
            .forEach((other) => (other.status = "CANCELLED"));
        }
      }
      e.updatedAt = now();
    }
    data = detail(e);
  } else fail("Этот раздел пока недоступен в демонстрации", 404);
  try {
    localStorage.setItem(STORAGE, JSON.stringify(db));
  } catch {
    fail(
      "Хранилище демо заполнено. Уменьшите обложку или сбросьте демонстрацию.",
    );
  }
  return { data, status: 200, statusText: "OK", headers: {}, config };
}
