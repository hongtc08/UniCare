import { createContext, useContext, useState, useCallback } from 'react';
import { readSession } from '../utils/session';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    let parsed = null;
    if (saved) {
      try {
        parsed = JSON.parse(saved);
      } catch {
        parsed = null;
      }
    }
    const tokenSession = readSession();
    if (tokenSession && parsed) {
      return {
        ...parsed,
        userId: parsed.userId || parsed.id || tokenSession.userId,
        id: parsed.id || parsed.userId || tokenSession.userId,
      };
    }
    return parsed;
  });

  const login = (userData, token) => {
    let fullUserData = { ...userData };
    if (!fullUserData.userId && !fullUserData.id && token) {
      const tokenSession = readSession();
      if (tokenSession?.userId) {
        fullUserData.userId = tokenSession.userId;
        fullUserData.id = tokenSession.userId;
      }
    }
    setUser(fullUserData);
    localStorage.setItem('token', token);
    localStorage.setItem('role', fullUserData.role);
    localStorage.setItem('user', JSON.stringify(fullUserData));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('user');
  };

  const updateIdentity = useCallback((identity) => {
    setUser((current) => {
      if (!current || current.username.trim() !== identity.username) return current;
      const updated = { ...current, fullName: identity.fullName || current.fullName };
      localStorage.setItem('user', JSON.stringify(updated));
      return updated;
    });
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout, updateIdentity, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);
