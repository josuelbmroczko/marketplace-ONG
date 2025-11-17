import React from 'react';
import { Routes, Route, Link, useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext.jsx';

import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import ProductPage from './pages/ProductPage.jsx';
import CartPage from './pages/CartPage.jsx';
import CheckoutPage from './pages/CheckoutPage.jsx';
import OrderSuccessPage from './pages/OrderSuccessPage.jsx';
import AdminOrgsPage from './pages/AdminOrgsPage.jsx';
import AdminUsersPage from './pages/AdminUsersPage.jsx';
import ProtectedRoute from './ProtectedRoute.jsx';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async (e) => {
    e.preventDefault();
    await logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="container">
        <div>
      <Link to="/" className="navbar-brand">Marketplace</Link>
    

          {user && user.role === 'ROLE_ADMIN' && (
            <>
              <Link to="/admin/orgs" className="navbar-link">
                Criar e Gerenciar ONGs 
              </Link>
              <Link to="/admin/users" className="navbar-link">
                Criar e Gerenciar Usuários,Gerentes e Admis
              </Link>
            </>
          )}
        </div>

        {user ? (
          <form onSubmit={handleLogout} className="logout-form">
            <span style={{ color: 'white', marginRight: '1rem' }}>Olá, {user.username}</span>
            <button type="submit" className="btn-logout">Sair</button>
          </form>
        ) : (
          <div>
            <Link to="/login" className="navbar-link" style={{ fontWeight: 'bold' }}>
              Login
            </Link>
            <Link to="/register" className="navbar-link" style={{ fontWeight: 'bold' }}>
              Registrar
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
};

function App() {
  return (
    <div>
      <Navbar />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/order-success" element={<OrderSuccessPage />} />
        <Route path="/" element={<ProtectedRoute><ProductPage /></ProtectedRoute>} />
        <Route path="/cart" element={<ProtectedRoute><CartPage /></ProtectedRoute>} />
        <Route path="/checkout" element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
        <Route
          path="/admin/orgs"
          element={
            <ProtectedRoute role="ROLE_ADMIN">
              <AdminOrgsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute role="ROLE_ADMIN">
              <AdminUsersPage />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  );
}

export default App;