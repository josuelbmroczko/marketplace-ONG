import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api.js'; 

function RegisterPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [organizationId, setOrganizationId] = useState('');
  const [organizations, setOrganizations] = useState([]);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    api.get('/org')
      .then(response => {
        setOrganizations(response.data);
      })
      .catch(err => {
        console.error("Erro ao buscar ONGs", err);
        setError("Não foi possível carregar as ONGs.");
      });
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    try {
      await api.post('/register/salvar', {
        username, 
        password, 
        organizationId 
      });
      navigate('/login?registered=true');
    } catch (err) {
      console.error("Erro no registro:", err);
      setError(err.response?.data?.message || "Falha no registro. Tente outro nome de usuário.");
    }
  };

  return (
    <div className="login-card">
      <h1>Criar Nova Conta</h1>
      {error && <div className="error-message">{error}</div>}
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="username">Usuário (Email):</label>
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

        <div className="form-group">
          <label htmlFor="organizationId">Qual ONG você deseja apoiar?</label>
          <select 
            id="organizationId" 
            value={organizationId} 
            onChange={(e) => setOrganizationId(e.target.value)} 
            required
          >
            <option value="">-- Selecione uma ONG --</option>
            {organizations.map(org => (
              <option key={org.id} value={org.id}>
                {org.name}
              </option>
            ))}
          </select>
        </div>

        <button type="submit" className="btn btn-success btn-login">
          Registrar
        </button>
      </form>

      <Link to="/login" className="register-link">
        Já tem uma conta? Faça o login
      </Link>
    </div>
  );
}

export default RegisterPage;