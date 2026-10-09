import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('sentinel_user');
    const token = localStorage.getItem('sentinel_token');
    if (savedUser && token) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (e) {
        localStorage.removeItem('sentinel_user');
        localStorage.removeItem('sentinel_token');
      }
    }
    setLoading(false);
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
    window.location.href = '/login';
  };

  const hasRole = (role) => {
    if (!user || !user.roles) return false;
    const target = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
    return user.roles.includes(target) || user.roles.includes(role);
  };

  const isAdmin = () => hasRole('ADMIN');
  const isAnalyst = () => hasRole('ANALYST') || hasRole('ADMIN');

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, hasRole, isAdmin, isAnalyst }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
