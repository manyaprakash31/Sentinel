import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Validate session against backend /api/auth/me on mount or reload
  useEffect(() => {
    const verifySession = async () => {
      const token = localStorage.getItem('sentinel_token');
      if (!token) {
        setLoading(false);
        return;
      }

      try {
        const res = await api.get('/auth/me');
        if (res.success && res.data) {
          const freshUser = {
            id: res.data.id,
            username: res.data.username,
            email: res.data.email,
            fullName: res.data.fullName,
            roles: res.data.roles,
            token,
          };
          setUser(freshUser);
          localStorage.setItem('sentinel_user', JSON.stringify(freshUser));
        } else {
          logout();
        }
      } catch (err) {
        console.warn('Session verification failed, logging out:', err);
        logout();
      } finally {
        setLoading(false);
      }
    };

    verifySession();
  }, []);

  const login = async (username, password) => {
    const res = await api.post('/auth/login', { username, password });
    if (res.success && res.data) {
      const userData = {
        id: res.data.id,
        username: res.data.username,
        email: res.data.email,
        fullName: res.data.fullName,
        roles: res.data.roles,
        token: res.data.token,
      };
      localStorage.setItem('sentinel_token', res.data.token);
      localStorage.setItem('sentinel_user', JSON.stringify(userData));
      setUser(userData);
      return userData;
    }
    throw new Error(res.message || 'Login failed');
  };

  const logout = () => {
    localStorage.removeItem('sentinel_token');
    localStorage.removeItem('sentinel_user');
    setUser(null);
  };

  const hasRole = (role) => {
    if (!user || !user.roles) return false;
    const target = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
    const cleanRole = role.replace('ROLE_', '');
    return user.roles.includes(target) || user.roles.includes(cleanRole);
  };

  const isAdmin = () => hasRole('ADMIN');
  const isAnalyst = () => hasRole('ANALYST') || hasRole('ADMIN');
  const isViewerOnly = () => !isAnalyst() && !isAdmin();

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, hasRole, isAdmin, isAnalyst, isViewerOnly, setUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
