import React, { useEffect, useState } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import { FiArrowLeft, FiUpload, FiCheck } from "react-icons/fi";
import { bookAPI } from "../services/api";
import { isDemo } from "../services/demoMode";
import { useAuth } from "../context/AuthContext";
import BookCover from "../components/book/BookCover";
import Button from "../components/ui/Button";
export default function CreateBook() {
  const { id } = useParams(),
    navigate = useNavigate(),
    { user } = useAuth();
  const [form, setForm] = useState({
      title: "",
      author: "",
      year: new Date().getFullYear(),
      category: "Классика",
      description: "",
      tags: "",
      isPublic: true,
    }),
    [file, setFile] = useState(null),
    [preview, setPreview] = useState(""),
    [loading, setLoading] = useState(!!id),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  useEffect(() => {
    if (!id) return;
    bookAPI
      .getById(id)
      .then(({ data }) => {
        if (data.username !== user.username) {
          setError("Редактировать книгу может только владелец");
          return;
        }
        setForm({ ...data, tags: (data.tags || []).join(", ") });
      })
      .catch((e) => setError(e.formattedMessage))
      .finally(() => setLoading(false));
  }, [id]);
  useEffect(() => {
    if (!file) {
      setPreview("");
      return;
    }
    const url = URL.createObjectURL(file);
    setPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);
  const change = (e) =>
    setForm({
      ...form,
      [e.target.name]:
        e.target.type === "checkbox" ? e.target.checked : e.target.value,
    });
  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const payload = {
        title: form.title.trim(),
        author: form.author.trim(),
        year: Number(form.year),
        description: form.description,
        category: form.category,
        tags: form.tags
          .split(",")
          .map((t) => t.trim())
          .filter(Boolean),
        isPublic: form.isPublic,
      };
      const { data } = id
        ? await bookAPI.update(id, payload, file)
        : await bookAPI.create(payload, file);
      navigate("/books/" + data.id);
    } catch (e) {
      setError(e.formattedMessage);
    } finally {
      setBusy(false);
    }
  };
  return (
    <section className="page-width section-space">
      <Link to="/profile" className="text-link mb-6">
        <FiArrowLeft />
        Моя полка
      </Link>
      <div className="section-heading">
        <div>
          <p className="eyebrow">Дайте истории продолжение</p>
          <h1 className="page-title">
            {id ? "Редактировать книгу" : "Поделиться книгой"}
          </h1>
        </div>
      </div>
      {error && (
        <div className="notice error" role="alert">
          {error}
        </div>
      )}
      {loading ? (
        <div className="skeleton h-96" />
      ) : (
        <div className="detail-grid">
          <aside>
            <BookCover
              key={preview || form.imageUrl}
              book={{
                ...form,
                id: Number(id) || 0,
                imageUrl: preview || form.imageUrl,
                title: form.title || "Ваша следующая история",
                author: form.author || "Имя автора",
              }}
            />
            <p className="text-xs text-gray-500 mt-4 leading-6">
              Хорошая фотография помогает книге найти читателя. Без фото мы
              создадим типографскую обложку.
            </p>
          </aside>
          <form className="panel" onSubmit={submit}>
            <div className="grid sm:grid-cols-2 gap-x-5">
              <label className="field">
                Название *
                <input
                  name="title"
                  required
                  maxLength={255}
                  value={form.title}
                  onChange={change}
                  placeholder="Маленький принц"
                />
              </label>
              <label className="field">
                Автор *
                <input
                  name="author"
                  required
                  maxLength={255}
                  value={form.author}
                  onChange={change}
                  placeholder="Антуан де Сент-Экзюпери"
                />
              </label>
              <label className="field">
                Год издания *
                <input
                  type="number"
                  name="year"
                  required
                  min={1000}
                  max={new Date().getFullYear() + 1}
                  value={form.year}
                  onChange={change}
                />
              </label>
              <label className="field">
                Жанр
                <select name="category" value={form.category} onChange={change}>
                  {[
                    "Классика",
                    "Художественная литература",
                    "Фантастика",
                    "Нон-фикшн",
                    "Наука",
                    "Детские книги",
                    "Другое",
                  ].map((c) => (
                    <option key={c}>{c}</option>
                  ))}
                </select>
              </label>
            </div>
            <label className="field">
              О книге и её состоянии
              <textarea
                name="description"
                rows={5}
                maxLength={5000}
                value={form.description}
                onChange={change}
                placeholder="Чем вам запомнилась книга? Есть ли пометки или повреждения?"
              />
            </label>
            <label className="field">
              Теги через запятую
              <input
                name="tags"
                maxLength={300}
                value={form.tags}
                onChange={change}
                placeholder="приключения, дружба, для подростков"
              />
            </label>
            <label className="field">
              <span className="flex items-center gap-2">
                <FiUpload />
                Обложка · JPG или PNG, до {isDemo ? "2" : "10"} МБ
              </span>
              <input
                type="file"
                accept="image/jpeg,image/png"
                onChange={(e) => {
                  const f = e.target.files[0];
                  if (
                    f &&
                    (!["image/jpeg", "image/png"].includes(f.type) ||
                      f.size > (isDemo ? 2 : 10) * 1024 * 1024)
                  ) {
                    setError("Выберите JPG или PNG допустимого размера");
                    e.target.value = "";
                    return;
                  }
                  setFile(f);
                  setError("");
                }}
              />
            </label>
            <label className="flex gap-3 items-center text-sm mb-6">
              <input
                type="checkbox"
                name="isPublic"
                checked={form.isPublic}
                onChange={change}
              />
              Показывать в общем каталоге
            </label>
            <div className="flex flex-wrap gap-3 justify-end">
              <Link
                className="action-link secondary"
                to={id ? "/books/" + id : "/profile"}
              >
                Отмена
              </Link>
              <Button type="submit" loading={busy} className="gap-2">
                <FiCheck />
                {id ? "Сохранить изменения" : "Добавить на полку"}
              </Button>
            </div>
          </form>
        </div>
      )}
    </section>
  );
}
