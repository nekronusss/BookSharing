import React, { useEffect, useState } from "react";
import { aiAPI } from "../../services/api";
import { isDemo } from "../../services/demoMode";
import AiResults from "./AiResults";
import Button from "../ui/Button";
export default function SimilarBooks({ id }) {
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    if (isDemo) return;
    let active = true;
    setResult(null);
    setError("");
    aiAPI
      .similar(id)
      .then(({ data }) => {
        if (active) setResult(data);
      })
      .catch((e) => {
        if (active)
          setError(e.formattedMessage || "Не удалось найти похожие книги");
      });
    return () => {
      active = false;
    };
  }, [id, retry]);
  return (
    <section className="mt-12">
      <h2 className="text-2xl mb-5">Похожие книги</h2>
      {isDemo ? (
        <p className="ai-note">
          Поиск похожих книг доступен в реальном каталоге.
        </p>
      ) : error ? (
        <div role="alert" className="ai-note">
          {error}{" "}
          <Button variant="outline" onClick={() => setRetry((n) => n + 1)}>
            Повторить
          </Button>
        </div>
      ) : result ? (
        <AiResults result={result} />
      ) : (
        <p role="status" className="ai-note">
          Ищем похожие книги…
        </p>
      )}
    </section>
  );
}
