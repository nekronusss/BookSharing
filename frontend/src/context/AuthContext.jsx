import React, { createContext, useContext, useEffect, useState } from "react";
import { authAPI, userAPI } from "../services/api";
import { authKey } from "../services/demoMode";
const Context = createContext(null);
export const useAuth = () => useContext(Context);
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null),
    [loading, setLoading] = useState(true);
  const logout = () => {
    localStorage.removeItem(authKey("token"));
    localStorage.removeItem(authKey("user"));
    setUser(null);
  };
  const updateUser = (data) => {
    setUser(data);
    localStorage.setItem(authKey("user"), JSON.stringify(data));
  };
  useEffect(() => {
    let active = true;
    if (localStorage.getItem(authKey("token")))
      userAPI
        .getProfile()
        .then(({ data }) => {
          if (active) updateUser(data);
        })
        .catch(() => {
          if (active) logout();
        })
        .finally(() => {
          if (active) setLoading(false);
        });
    else setLoading(false);
    const expired = () => logout();
    window.addEventListener("auth-expired", expired);
    return () => {
      active = false;
      window.removeEventListener("auth-expired", expired);
    };
  }, []);
  const login = async (username, password) => {
    try {
      const { data } = await authAPI.login(username, password);
      localStorage.setItem(authKey("token"), data.token);
      updateUser({
        username: data.username,
        displayName: data.displayName,
        avatarUrl: data.avatarUrl,
      });
      return { success: true };
    } catch (error) {
      return { success: false, error: error.formattedMessage };
    }
  };
  const register = async (username, password) => {
    try {
      await authAPI.register(username, password);
      return { success: true };
    } catch (error) {
      return { success: false, error: error.formattedMessage };
    }
  };
  return (
    <Context.Provider
      value={{ user, loading, login, register, logout, updateUser }}
    >
      {children}
    </Context.Provider>
  );
}
