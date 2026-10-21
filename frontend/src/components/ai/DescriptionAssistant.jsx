import React, { useState } from "react";
import { aiAPI } from "../../services/api";
import { isDemo } from "../../services/demoMode";
import Button from "../ui/Button";
export default function DescriptionAssistant({ form, onApply }) {
  const [draft, setDraft] = useState(null);
  const [baseline, setBaseline] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const snapshot = JSON.stringify([
    form.title,
    form.author,
    form.description,
    form.tags,
    form.category,
  ]);
  const stale = baseline !== snapshot;
  async function generate() {
    setBusy(true);
    setError("");
    setDraft(null);
    setBaseline(snapshot);
    try {
      const { data } = await aiAPI.describe({
        title: form.title.trim(),
        author: form.author.trim(),
        description: form.description,
        category: form.category,
        tags: form.tags
          .split(",")
          .map((t) => t.trim())
          .filter(Boolean),
      });
      setDraft(data);
    } catch (e) {
      setError(e.formattedMessage || "Не удалось подготовить черновик");
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="ai-panel mb-5">
      <Button
        variant="outline"
        onClick={generate}
        loading={busy}
        disabled={
          isDemo ||
          !form.title.trim() ||
          !form.author.trim() ||
          !form.description.trim()
        }
      >
        Помочь с описанием
      </Button>
      <p className="ai-note">
        {isDemo
          ? "В деморежиме модель не подключена."
          : "Добавьте несколько достоверных сведений о книге. По нажатию они передаются настроенному AI-провайдеру для редактирования."}
      </p>
      {error && (
        <p className="notice error" role="alert">
          {error}
        </p>
      )}
      {draft && (
        <div className="ai-draft">
          <h3>Черновик AI — проверьте факты перед применением</h3>
          <p className="whitespace-pre-wrap">{draft.description}</p>
          <p className="ai-note">
            Категория: {draft.category}
            <br />
            Теги: {draft.tags.join(", ")}
          </p>
          {stale && (
            <p role="status" className="ai-note">
              Исходные поля изменились. Сформируйте новый черновик.
            </p>
          )}
          <div className="flex gap-3 flex-wrap">
            <Button
              disabled={stale}
              onClick={() => {
                onApply(draft);
                setDraft(null);
              }}
            >
              Применить черновик к форме
            </Button>
            <Button variant="ghost" onClick={() => setDraft(null)}>
              Отклонить
            </Button>
          </div>
          <p className="ai-note">
            Книга сохранится только после нажатия основной кнопки сохранения.
          </p>
        </div>
      )}
    </div>
  );
}
