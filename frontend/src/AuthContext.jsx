// Arquivo: frontend/src/AuthContext.jsx
import React, { createContext, useState, useContext, useEffect } from 'react';
import api from './api.js'; 

// 1. Cria o Contexto
const AuthContext = createContext(null);

// 2. Cria o "Provedor" (que vai envolver nosso app)
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true); // Estado de "carregando"

  // 3. Função para checar quem está logado (chamada no início)
  useEffect(() => {
    // Chama o endpoint /api/auth/me que criamos
    api.get('/auth/me')
      .then(response => {
        // Se response.data for "", o Axios pode retornar null
        setUser(response.data || null); 
      })
      .catch(error => {
        console.error("Não está autenticado:", error);
        setUser(null);
      })
      .finally(() => {
        setLoading(false); // Terminou de carregar
      });
  }, []); // [] = Roda apenas uma vez, quando o app inicia

  // 4. Funções de Login e Logout
  const login = async (username, password) => {
    try {
      // Usa URLSearchParams para simular o 'form-urlencoded'
      const response = await api.post('/auth/login', new URLSearchParams({ username, password }));
      setUser(response.data); // Salva o usuário logado
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
      setUser(null); // Limpa o usuário do estado de qualquer forma
    }
  };

  // 5. O valor que será compartilhado com o resto do app
  const value = {
    user,
    loading,
    login,
    logout,
  };

  // Não mostra nada até terminar de checar o login
  if (loading) {
    return <div className="container">Carregando sessão...</div>;
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// 6. Um "hook" customizado para facilitar o uso
export const useAuth = () => {
  return useContext(AuthContext);
};