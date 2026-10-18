import React, { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { FiArrowUpRight, FiBookOpen } from "react-icons/fi";
import { useAuth } from "../context/AuthContext";
import { isDemo, enableDemo } from "../services/demoMode";
import Button from "../components/ui/Button";
export default function Auth({ registration = false }) {
  const [username, setUsername] = useState(""),
    [password, setPassword] = useState(""),
    [confirm, setConfirm] = useState(""),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  const { login, register } = useAuth(),
    navigate = useNavigate(),
    location = useLocation();
  const submit = async (e) => {
    e.preventDefault();
    setError("");
    if (registration && password !== confirm) {
      setError("Пароли не совпадают");
      return;
    }
    setBusy(true);
    if (registration) {
      const result = await register(username.trim(), password);
      if (!result.success) {
        setError(result.error);
        setBusy(false);
        return;
      }
    }
    const result = await login(username.trim(), password);
    setBusy(false);
    if (result.success)
      navigate(location.state?.from || "/profile", { replace: true });
    else setError(result.error);
  };
  return (
    <div className="auth-layout">
      <aside className="auth-story">
        <p className="eyebrow">
          <FiBookOpen />
          Ваше место среди книг
        </p>
        <h2>
          Одна книга.
          <br />
          Много новых
          <br />
          <em>возможностей.</em>
        </h2>
        <p>
          Соберите свою полку, найдите единомышленников и дайте любимым историям
          продолжение.
        </p>
      </aside>
      <div className="auth-form-wrap">
        <form className="auth-form" onSubmit={submit}>
          <p className="eyebrow">BookSharing</p>
          <h1>{registration ? "Будем знакомы" : "С возвращением"}</h1>
          <p>
            {registration
              ? "Создайте аккаунт и поделитесь первой книгой."
              : "Ваша следующая история уже ждёт."}
          </p>
          {isDemo && (
            <div className="notice">
              Демо: <strong>alina</strong> или <strong>mark</strong>, пароль{" "}
              <strong>demo123</strong>. Используйте только демонстрационные
              данные.
            </div>
          )}
          {error && (
            <div className="notice error" role="alert">
              {error}
            </div>
          )}
          <label className="field">
            Имя пользователя
            <input
              autoComplete="username"
              required
              minLength={3}
              maxLength={50}
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Например, alina"
            />
          </label>
          <label className="field">
            Пароль
            <input
              type="password"
              autoComplete={registration ? "new-password" : "current-password"}
              required
              minLength={6}
              maxLength={72}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Не менее 6 символов"
            />
          </label>
          {registration && (
            <label className="field">
              Повторите пароль
              <input
                type="password"
                autoComplete="new-password"
                required
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
              />
            </label>
          )}
          <Button type="submit" loading={busy} className="w-full py-3 gap-3">
            {registration ? "Создать аккаунт" : "Войти"}
            <FiArrowUpRight />
          </Button>
          <p className="mt-6 text-center text-sm">
            {registration ? "Уже с нами?" : "Пока нет аккаунта?"}{" "}
            <Link
              className="text-link"
              state={location.state}
              to={registration ? "/login" : "/register"}
            >
              {registration ? "Войти" : "Присоединиться"}
            </Link>
          </p>
          {!isDemo && (
            <button
              type="button"
              onClick={enableDemo}
              className="text-link mt-5"
            >
              Посмотреть демонстрацию без регистрации <FiArrowUpRight />
            </button>
          )}
        </form>
      </div>
    </div>
  );
}
