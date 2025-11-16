import React from 'react';
import { useAuth } from './AuthContext.jsx'; 
import { Navigate, useLocation } from 'react-router-dom';

function ProtectedRoute({ children, role }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (role && user.role !== role) {
    console.warn(`Acesso negado para ${user.role}. Rota requer ${role}.`);
    return <Navigate to="/" replace />;
  }

  return children;
}

export default ProtectedRoute;