import React, { useEffect, useState } from "react";
import { aiAPI } from "../../services/api";
import { isDemo } from "../../services/demoMode";
import Button from "../ui/Button";
import AiResults from "./AiResults";

const genres = [
  "Фантастика",
  "Фэнтези",
  "Психология",
  "Детективы",
  "Наука",
  "Классика",
  "Нон-фикшн",
  "Художественная литература",
];
export default function Recommendations() {
  const [preferences, setPreferences] = useState({
    genres: [],
    interests: "",
    useHistory: false,
  });
  const [result, setResult] = useState(null);
  const [busy, setBusy] = useState(!isDemo);
  const [ready, setReady] = useState(false);
  const [error, setError] = useState("");
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    if (isDemo) return;
    let active = true;
    setBusy(true);
    setError("");
    Promise.all([aiAPI.preferences(), aiAPI.recommendations()])
      .then(([p, r]) => {
        if (active) {
          setPreferences(p.data);
          setResult(r.data);
          setReady(true);
        }
      })
      .catch((e) => {
        if (active)
          setError(e.formattedMessage || "Не удалось загрузить подборку");
      })
      .finally(() => {
        if (active) setBusy(false);
      });
    return () => {
      active = false;
    };
  }, [revision]);
  async function save(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const { data } = await aiAPI.savePreferences(preferences);
      setPreferences(data);
      setResult((await aiAPI.recommendations()).data);
    } catch (e) {
      setError(e.formattedMessage || "Не удалось сохранить предпочтения");
    } finally {
      setBusy(false);
    }
  }
  return (
    <section className="page-width section-space">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Ваша следующая история</p>
          <h2>Подобрано специально для вас</h2>
        </div>
      </div>
      {isDemo ? (
        <p className="ai-note">
          Персональный AI-подбор доступен с реальным аккаунтом и каталогом. В
          деморежиме модель не подключена.
        </p>
      ) : (
        <>
          <details
            className="ai-panel"
            open={
              ready && !preferences.genres.length && !preferences.interests
                ? true
                : undefined
            }
          >
            <summary>Любимые жанры и интересы</summary>
            <form onSubmit={save}>
              <fieldset disabled={busy || !ready}>
                <legend className="ai-note">
                  Выберите жанры, чтобы начать подбор даже без истории чтения.
                </legend>
                <div className="ai-genres">
                  {genres.map((genre) => (
                    <label key={genre}>
                      <input
                        type="checkbox"
                        checked={preferences.genres.includes(genre)}
                        onChange={(e) =>
                          setPreferences((p) => ({
                            ...p,
                            genres: e.target.checked
                              ? [...p.genres, genre]
                              : p.genres.filter((g) => g !== genre),
                          }))
                        }
                      />
                      {genre}
                    </label>
                  ))}
                </div>
                <label className="field">
                  Темы, которые вам интересны
                  <input
                    maxLength={500}
                    value={preferences.interests}
                    placeholder="Путешествия, философия, история…"
                    onChange={(e) =>
                      setPreferences((p) => ({
                        ...p,
                        interests: e.target.value,
                      }))
                    }
                  />
                </label>
                <label className="ai-check">
                  <input
                    type="checkbox"
                    checked={preferences.useHistory}
                    onChange={(e) =>
                      setPreferences((p) => ({
                        ...p,
                        useHistory: e.target.checked,
                      }))
                    }
                  />
                  Учитывать моё избранное, оценки и завершённые обмены
                </label>
                <p className="ai-note">
                  Эти сигналы обрабатываются на сервере BookSharing и не
                  передаются языковой модели.
                </p>
                <Button type="submit" loading={busy}>
                  Сохранить предпочтения
                </Button>
              </fieldset>
            </form>
          </details>
          {error && (
            <div className="notice error" role="alert">
              {error}{" "}
              <Button
                variant="outline"
                onClick={() => setRevision((n) => n + 1)}
              >
                Повторить
              </Button>
            </div>
          )}
          {busy && (
            <p className="ai-note" role="status">
              Подбираем книги…
            </p>
          )}
          {result && <AiResults result={result} />}
        </>
      )}
    </section>
  );
}
