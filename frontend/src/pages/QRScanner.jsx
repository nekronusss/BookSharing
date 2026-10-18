import React, { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Html5Qrcode } from "html5-qrcode";
import { FiCamera, FiSearch } from "react-icons/fi";
import { qrcodeAPI } from "../services/api";
import { isDemo } from "../services/demoMode";
import Button from "../components/ui/Button";
export default function QRScanner() {
  const navigate = useNavigate(),
    scanner = useRef(null),
    active = useRef(true),
    [scanning, setScanning] = useState(false),
    [busy, setBusy] = useState(false),
    [error, setError] = useState(""),
    [code, setCode] = useState("");
  const stop = async () => {
    if (scanner.current?.isScanning) await scanner.current.stop();
    setScanning(false);
  };
  useEffect(() => {
    active.current = true;
    return () => {
      active.current = false;
      if (scanner.current?.isScanning) scanner.current.stop().catch(() => {});
    };
  }, []);
  const lookup = async (value) => {
    setBusy(true);
    setError("");
    try {
      const { data } = await qrcodeAPI.scan(value.trim());
      if (active.current) navigate("/books/" + data.id);
    } catch (e) {
      if (active.current) setError(e.formattedMessage);
    } finally {
      if (active.current) setBusy(false);
    }
  };
  const start = async () => {
    setError("");
    setBusy(true);
    try {
      scanner.current = new Html5Qrcode("qr-reader");
      await scanner.current.start(
        { facingMode: "environment" },
        { fps: 10, qrbox: 220 },
        async (value) => {
          await stop();
          await lookup(value);
        },
        () => {},
      );
      if (!active.current) {
        await scanner.current.stop();
        return;
      }
      setScanning(true);
    } catch {
      setError(
        "Камера недоступна. Разрешите доступ в браузере или введите код вручную. Для камеры нужен HTTPS или localhost.",
      );
    } finally {
      setBusy(false);
    }
  };
  return (
    <section className="page-width section-space max-w-3xl">
      <p className="eyebrow">У каждой книги свой маршрут</p>
      <h1 className="page-title mb-4">Паспорт истории</h1>
      <p className="text-gray-500 mb-8">
        Наведите камеру на QR-код BookSharing, чтобы открыть карточку книги.
      </p>
      <div className="panel">
        <div id="qr-reader" className="max-w-sm mx-auto" />
        {!scanning && (
          <div className="empty-panel mb-6">
            <FiCamera />
            <h2>Знакомая книга — в один кадр</h2>
            <p>Камера включится только после нажатия кнопки.</p>
          </div>
        )}
        {error && (
          <div className="notice error" role="alert">
            {error}
          </div>
        )}
        <div className="text-center">
          <Button onClick={scanning ? stop : start} loading={busy}>
            {scanning ? "Остановить камеру" : "Включить камеру"}
          </Button>
        </div>
        <form
          className="border-t mt-8 pt-6"
          onSubmit={(e) => {
            e.preventDefault();
            lookup(code);
          }}
        >
          <label className="field">
            Или введите код вручную
            <input
              value={code}
              required
              maxLength={200}
              onChange={(e) => setCode(e.target.value)}
              placeholder={isDemo ? "BOOK-DEMO-1" : "Код с паспорта книги"}
            />
          </label>
          <Button
            type="submit"
            variant="outline"
            disabled={busy || !code.trim()}
          >
            <FiSearch className="mr-2" />
            Найти книгу
          </Button>
          {isDemo && (
            <p className="text-xs text-gray-500 mt-4">
              Для демонстрации: BOOK-DEMO-1 … BOOK-DEMO-16.
            </p>
          )}
        </form>
      </div>
    </section>
  );
}
