import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { FiArrowLeft, FiRepeat, FiCheckCircle } from "react-icons/fi";
import { exchangeAPI } from "../services/api";
import { exchangeLabels, exchangeSteps } from "../services/exchangeStatus";
import { useAuth } from "../context/AuthContext";
import Button from "../components/ui/Button";
import ConfirmModal from "../components/ui/ConfirmModal";
export default function ExchangeDetail() {
  const { id } = useParams(),
    { user } = useAuth(),
    [item, setItem] = useState(null),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false),
    [error, setError] = useState(""),
    [tracking, setTracking] = useState(""),
    [action, setAction] = useState(null);
  const load = () => {
    setLoading(true);
    setError("");
    exchangeAPI
      .getById(id)
      .then(({ data }) => setItem(data))
      .catch((e) => setError(e.formattedMessage))
      .finally(() => setLoading(false));
  };
  useEffect(load, [id]);
  const actions = {
    accept: [
      "Принять заявку",
      "Вы готовы передать книгу? После принятия согласуйте способ и время передачи.",
    ],
    reject: ["Отклонить заявку", "Участник увидит, что обмен отклонён."],
    cancel: ["Отменить заявку", "Отменить вашу заявку на эту книгу?"],
    markAsShipped: [
      "Книга передана / отправлена",
      "Подтвердите, что вы действительно передали книгу получателю или службе доставки.",
    ],
    markAsDelivered: [
      "Книга доставлена",
      "Отметьте доставку после того, как книга окажется у получателя.",
    ],
    confirmReceipt: [
      "Подтвердить получение",
      "Вы проверили книгу и согласны завершить обмен? Владелец изменится после подтверждения обеих сторон.",
    ],
  };
  const execute = async () => {
    setBusy(true);
    setError("");
    try {
      const { data } = await exchangeAPI[action](
        id,
        ...(action === "markAsShipped" ? [tracking.trim() || null] : []),
      );
      setItem(data);
      setAction(null);
    } catch (e) {
      setAction(null);
      setError(e.formattedMessage);
    } finally {
      setBusy(false);
    }
  };
  if (loading)
    return (
      <div className="page-width section-space">
        <div className="skeleton h-96" />
      </div>
    );
  if (!item)
    return (
      <div className="page-width section-space empty-panel">
        <h1 className="page-title">Обмен недоступен</h1>
        <p>{error}</p>
        <Button onClick={load}>Повторить</Button>
      </div>
    );
  const closed = ["REJECTED", "CANCELLED"].includes(item.status),
    index = exchangeSteps.indexOf(item.status);
  return (
    <section className="page-width section-space">
      <Link className="text-link mb-8" to="/exchanges">
        <FiArrowLeft />
        Мои обмены
      </Link>
      <div className="section-heading">
        <div>
          <p className="eyebrow">От читателя к читателю</p>
          <h1 className="page-title">История одного обмена</h1>
        </div>
        <span className="status-pill">{exchangeLabels[item.status]}</span>
      </div>
      {error && (
        <div className="notice error" role="alert">
          {error}
        </div>
      )}
      <div className="grid lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2">
          <div className="panel">
            <div className="grid sm:grid-cols-2 gap-6">
              <div>
                <p className="eyebrow">От @{item.offererUsername}</p>
                <Link to={"/books/" + item.requestedBookId}>
                  <h2 className="text-3xl">{item.requestedBookTitle}</h2>
                </Link>
                <p className="text-xs text-gray-500 mt-3">
                  Получатель: @{item.requesterUsername}
                </p>
              </div>
              <div className="sm:border-l sm:pl-6">
                <p className="eyebrow">
                  <FiRepeat />В ответ от @{item.requesterUsername}
                </p>
                <h2 className="text-3xl">
                  {item.offeredBookTitle || "Спасибо за историю"}
                </h2>
                <p className="text-xs text-gray-500 mt-3">
                  {item.offeredBookTitle
                    ? "Получатель: @" + item.offererUsername
                    : "Передача книги без встречного обмена"}
                </p>
              </div>
            </div>
            {item.message && (
              <div className="mt-8 border-t pt-6">
                <p className="text-xs text-gray-500 mb-3">Сообщение к заявке</p>
                <p className="text-sm leading-7 whitespace-pre-wrap">
                  {item.message}
                </p>
              </div>
            )}
          </div>
          {!closed && (
            <div className="panel mt-6">
              <h2 className="text-2xl mb-6">Путь книги</h2>
              <ol className="space-y-5">
                {exchangeSteps.map((s, i) => (
                  <li
                    key={s}
                    className={
                      "flex items-center gap-4 text-sm " +
                      (i <= index ? "text-primary-700" : "text-gray-400")
                    }
                  >
                    <span
                      className={
                        "h-8 w-8 rounded-full flex items-center justify-center shrink-0 " +
                        (i <= index ? "bg-primary-100" : "bg-gray-100")
                      }
                    >
                      {i < index || item.status === "COMPLETED" ? (
                        <FiCheckCircle />
                      ) : (
                        i + 1
                      )}
                    </span>
                    {exchangeLabels[s]}
                    {i === index && item.status !== "COMPLETED" && (
                      <span className="text-xs ml-auto">Сейчас</span>
                    )}
                  </li>
                ))}
              </ol>
            </div>
          )}
          {item.status === "COMPLETED" && (
            <div className="notice mt-6">
              Обмен завершён! Книги появились на полках новых владельцев.
            </div>
          )}
        </div>
        <aside className="space-y-6">
          <div className="panel">
            <h2 className="text-2xl mb-5">Следующий шаг</h2>
            {item.canMarkAsShipped && (
              <label className="field">
                Трек-номер (необязательно)
                <input
                  maxLength={100}
                  value={tracking}
                  onChange={(e) => setTracking(e.target.value)}
                  placeholder="Или передайте при встрече"
                />
              </label>
            )}
            <div className="flex flex-col gap-3">
              {[
                ["canAccept", "accept"],
                ["canReject", "reject"],
                ["canCancel", "cancel"],
                ["canMarkAsShipped", "markAsShipped"],
                ["canMarkAsDelivered", "markAsDelivered"],
                ["canConfirmReceipt", "confirmReceipt"],
              ]
                .filter(([flag]) => item[flag])
                .map(([flag, key]) => (
                  <Button
                    key={key}
                    variant={
                      ["reject", "cancel"].includes(key) ? "outline" : "primary"
                    }
                    onClick={() => setAction(key)}
                    disabled={busy}
                  >
                    {actions[key][0]}
                  </Button>
                ))}
            </div>
            {![
              "canAccept",
              "canReject",
              "canCancel",
              "canMarkAsShipped",
              "canMarkAsDelivered",
              "canConfirmReceipt",
            ].some((flag) => item[flag]) && (
              <p className="text-sm text-gray-500 leading-7">
                {closed
                  ? "Эта заявка закрыта. Вы можете найти другую книгу в каталоге."
                  : item.status === "COMPLETED"
                    ? "Спасибо, что делитесь историями."
                    : "Сейчас ход другого участника. Обновите статус, когда он выполнит действие."}
              </p>
            )}
            <Button variant="ghost" className="mt-4 w-full" onClick={load}>
              Обновить статус
            </Button>
          </div>
          <div className="panel">
            <p className="text-xs text-gray-500">Заявка создана</p>
            <p className="text-sm mt-2">
              {new Date(item.createdAt).toLocaleString("ru-RU")}
            </p>
            {item.trackingNumber && (
              <>
                <p className="text-xs text-gray-500 mt-5">Трек-номер</p>
                <p className="text-sm break-all mt-2">{item.trackingNumber}</p>
              </>
            )}
            {item.status === "DELIVERED" && (
              <div className="text-xs text-gray-500 space-y-3 mt-5">
                <p>
                  Получатель:{" "}
                  {item.requesterConfirmedReceipt
                    ? "подтвердил"
                    : "ждём подтверждения"}
                </p>
                <p>
                  Владелец:{" "}
                  {item.offererConfirmedReceipt
                    ? "подтвердил"
                    : "ждём подтверждения"}
                </p>
              </div>
            )}
          </div>
          <Link to="/guide" className="text-link">
            Как провести хороший обмен →
          </Link>
        </aside>
      </div>
      <ConfirmModal
        isOpen={!!action}
        onClose={() => !busy && setAction(null)}
        onConfirm={execute}
        loading={busy}
        title={actions[action]?.[0]}
        message={actions[action]?.[1]}
        variant={["reject", "cancel"].includes(action) ? "danger" : "primary"}
        confirmText="Подтвердить"
        cancelText="Назад"
      />
    </section>
  );
}
