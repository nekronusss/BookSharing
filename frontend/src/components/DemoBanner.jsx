import React from "react";
import {
  isDemo,
  exitDemo,
  resetDemo,
  switchDemoUser,
} from "../services/demoMode";
import { useAuth } from "../context/AuthContext";
export default function DemoBanner() {
  const { user } = useAuth();
  return isDemo ? (
    <div className="demo-banner">
      Демонстрация · данные только в этом браузере ·{" "}
      {user?.displayName || user?.username || "Гость"}
      <button
        onClick={() =>
          switchDemoUser(user?.username === "alina" ? "mark" : "alina")
        }
      >
        Войти как {user?.username === "alina" ? "Марк" : "Алина"}
      </button>
      <button onClick={resetDemo}>Сбросить демо</button>
      <button onClick={exitDemo}>К реальному API</button>
    </div>
  ) : null;
}
