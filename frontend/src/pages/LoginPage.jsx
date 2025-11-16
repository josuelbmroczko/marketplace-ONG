import React, { useState } from 'react';
import { useAuth } from '../AuthContext.jsx'; 
import { useNavigate, useLocation, Link } from 'react-router-dom';

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    const success = await login(username, password);
    if (success) {
      navigate(from, { replace: true });
    } else {
      setError('Usuário ou senha inválidos.');
    }
  };

  return (
    <div className="login-card">
      <h1>Acessar Sistema</h1>
      {error && <div className="error-message">{error}</div>}
      
      {location.search.includes('registered=true') && (
        <div className="success-message">
          Usuário registrado com sucesso! Faça o login.
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="username">Usuário:</label>
          <input
            type="text"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="password">Senha:</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="btn btn-primary btn-login">
          Entrar
        </button>
      </form>

      <div className="register-link">
        Não tem uma conta?
        <Link to="/register">Registre-se aqui</Link>
      </div>
    </div>
  );
}

export default LoginPage;