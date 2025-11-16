import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api.js'; 

function AdminOrgsPage() {
  const [orgs, setOrgs] = useState([]);
  const [newOrgName, setNewOrgName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchOrgs = () => {
    api.get('/org')
      .then(response => {
        setOrgs(Array.isArray(response.data) ? response.data : []);
        setError(null);
      })
      .catch(error => {
        console.error("Erro ao buscar ONGs:", error);
        setError("Não foi possível carregar as ONGs. Você tem permissão de Admin?");
        setOrgs([]);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    setLoading(true);
    fetchOrgs();
  }, []); 

  const handleCreateOrg = (e) => {
    e.preventDefault(); 
    if (!newOrgName) return;

    api.post('/org', { name: newOrgName })
      .then(response => {
        setNewOrgName('');
        fetchOrgs();
      })
      .catch(error => {
        console.error("Erro ao criar ONG:", error);
        setError("Erro ao criar ONG.");
      });
  };

  if (loading) {
    return <div className="container">Carregando ONGs...</div>;
  }

  return (
    <main className="container">
      <h1>Gerenciar Organizações (ONGs)</h1>
      
      <Link to="/admin/users" className="nav-link">
        &laquo; Voltar para Gerenciar Usuários
      </Link>

      {error && <div className="error-message">{error}</div>}

      <div className="card">
        <h2>Criar Nova ONG</h2>
        
        <form onSubmit={handleCreateOrg}>
          <div className="form-group">
            <label htmlFor="name">Nome da ONG:</label>
            <input 
              type="text" 
              id="name" 
              value={newOrgName}
              onChange={(e) => setNewOrgName(e.target.value)}
              required 
            />
          </div>
          <button type="submit" className="btn btn-success">
            Salvar Nova ONG
          </button>
        </form>
      </div>

      <div className="card">
        <h2>ONGs Cadastradas</h2>
        <div className="table-wrapper"> 
      <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
            </tr>
          </thead>
          <tbody>
            {orgs.map(org => (
              <tr key={org.id}>
                <td>{org.id}</td>
                <td>{org.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
        </div>
      </div>
    </main>
  );
}

export default AdminOrgsPage;