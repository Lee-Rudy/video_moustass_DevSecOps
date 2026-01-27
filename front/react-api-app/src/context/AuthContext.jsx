import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { login as apiLogin } from '../api/authApi';

const STORAGE_KEY = 'auth_user';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);

  const loadStored = useCallback(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const data = JSON.parse(raw);
        if (data?.token && data?.userId != null) setUser(data);
      }
    } catch (_) {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, []);

  useEffect(() => { loadStored(); }, [loadStored]);

  const login = useCallback(async (mail, password) => {
    const data = await apiLogin(mail, password);
    const u = { 
      token: data.token, 
      userId: data.userId, 
      name: data.name || '', 
      isAdmin: data.isAdmin || false 
    };
    setUser(u);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(u));
    return u;
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    localStorage.removeItem(STORAGE_KEY);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
