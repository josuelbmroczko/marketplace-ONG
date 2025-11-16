import React, { createContext, useState, useContext, useEffect } from 'react';
import api from './api.js'; 

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/auth/me')
      .then(response => {
        setUser(response.data || null); 
      })
      .catch(error => {
        console.error("Não está autenticado:", error);
        setUser(null);
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  const login = async (username, password) => {
    try {
      const response = await api.post('/auth/login', new URLSearchParams({ username, password }));
      setUser(response.data);
      return true;
    } catch (error) {
      console.error("Falha no login:", error);
      setUser(null);
      return false;
    }
  };

  const logout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      console.error("Erro no logout:", error);
    } finally {
      setUser(null);
    }
  };

  const value = {
    user,
    loading,
    login,
    logout,
  };

  if (loading) {
    return <div className="container">Carregando sessão...</div>;
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  return useContext(AuthContext);
};