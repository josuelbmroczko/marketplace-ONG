import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api.js';
import { useAuth } from '../AuthContext.jsx';

function AdminUsersPage() {
  const { user } = useAuth();

  const [users, setUsers] = useState([]);
  const [organizations, setOrganizations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [editingUser, setEditingUser] = useState(null);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('GERENTE');
  const [organizationId, setOrganizationId] = useState('');

  const fetchData = async () => {
    if (!user || user.role !== 'ROLE_ADMIN') {
      setError("Acesso negado. Somente administradores podem ver esta página.");
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      const [usersRes, orgsRes] = await Promise.all([
        api.get('/users'),
        api.get('/org')
      ]);
      console.log("Resposta da API de /users:", usersRes.data);
      setUsers(Array.isArray(usersRes.data) ? usersRes.data : []);
      setOrganizations(Array.isArray(orgsRes.data) ? orgsRes.data : []);
      setError(null);
    } catch (err) {
      console.error("Erro ao buscar dados:", err);
      setError(err.response?.data?.message || "Não foi possível carregar os dados.");
      setUsers([]);
      setOrganizations([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  const resetForm = () => {
    setUsername('');
    setPassword('');
    setRole('GERENTE');
    setOrganizationId('');
    setEditingUser(null);
    setError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    const userData = {
      username,
      role,
      organizationId: role === 'GERENTE' ? organizationId : null,
    };

    if (role === 'GERENTE' && !organizationId) {
      setError("Gerentes devem pertencer a uma ONG.");
      return;
    }

    try {
      if (editingUser) {
        if (password) {
          userData.password = password;
        }
        await api.put(`/users/${editingUser.id}`, userData);

      } else {
        if (!password) {
          setError("Senha é obrigatória para criar novo usuário.");
          return;
        }
        userData.password = password;
        await api.post('/users/register', userData);
      }

      resetForm();
      fetchData();

    } catch (err) {
      console.error("Erro ao salvar usuário:", err);
      setError(err.response?.data?.message || "Erro ao salvar usuário.");
    }
  };

  const handleEditClick = (userToEdit) => {
    setEditingUser(userToEdit);
    setUsername(userToEdit.username);
    setRole(userToEdit.role.replace('ROLE_', ''));
    setOrganizationId(userToEdit.organization ? userToEdit.organization.id : '');
    setPassword('');
    setError(null);
    window.scrollTo(0, 0);
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm('Tem certeza que deseja apagar este usuário?')) {
      return;
    }
    try {
      await api.delete(`/users/${userId}`);
      fetchData();
    } catch (err) {
      console.error("Erro ao apagar usuário:", err);
      setError(err.response?.data?.message || "Erro ao apagar usuário.");
    }
  };

  const roleOrder = {
    'ROLE_ADMIN': 1,
    'ROLE_GERENTE': 2,
    'ROLE_USUARIO': 3
  };

  const sortedUsers = [...users].sort((a, b) => {
    const roleA = roleOrder[a.role] || 99;
    const roleB = roleOrder[b.role] || 99;

    if (roleA !== roleB) {
      return roleA - roleB;
    }

    const orgA = a.organization ? a.organization.name.toUpperCase() : 'ZZZ_NA';
    const orgB = b.organization ? b.organization.name.toUpperCase() : 'ZZZ_NA';

    if (orgA !== orgB) {
      return orgA.localeCompare(orgB);
    }

    return a.username.localeCompare(b.username);
  });

  const getRowClass = (role) => {
    switch (role) {
      case 'ROLE_ADMIN':
        return 'role-admin';
      case 'ROLE_GERENTE':
        return 'role-gerente';
      case 'ROLE_USUARIO':
        return 'role-usuario';
      default:
        return '';
    }
  };

  if (loading) {
    return <div className="container">Carregando...</div>;
  }

  if (!user || user.role !== 'ROLE_ADMIN') {
    return (
      <main className="container">
        <h1>Painel do Administrador</h1>
        {error && <div className="error-message">{error}</div>}
      </main>
    );
  }

  return (
    <main className="container">
      <style>{`
        .container {
          padding: 15px;
          max-width: 1200px;
          margin: 0 auto;
        }
        
        .card {
          background: white;
          border-radius: 8px;
          padding: 20px;
          margin-bottom: 20px;
          box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .form-group {
          margin-bottom: 15px;
        }
        
        .form-group label {
          display: block;
          margin-bottom: 5px;
          font-weight: bold;
        }
        
        .form-group input,
        .form-group select {
          width: 100%;
          padding: 8px;
          border: 1px solid #ddd;
          border-radius: 4px;
          box-sizing: border-box;
        }
        
        .btn {
          padding: 10px 15px;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          font-size: 14px;
          margin: 5px;
        }
        
        .btn-success { background: #28a745; color: white; }
        .btn-warning { background: #ffc107; color: black; }
        .btn-danger { background: #dc3545; color: white; }
        .btn-secondary { background: #6c757d; color: white; }
        .btn-primary { background: #007bff; color: white; }
        
        .nav-link {
          display: inline-block;
          background: #007bff;
          color: white;
          padding: 10px 15px;
          border-radius: 4px;
          text-decoration: none;
          margin-bottom: 20px;
        }
        
        .error-message {
          background: #f8d7da;
          color: #721c24;
          padding: 10px;
          border-radius: 4px;
          margin-bottom: 20px;
        }
        
        /* Tabela */
        .data-table {
          width: 100%;
          border-collapse: collapse;
          margin-bottom: 20px;
        }
        
        .data-table th,
        .data-table td {
          padding: 12px;
          text-align: left;
          border-bottom: 1px solid #ddd;
        }
        
        .data-table thead th {
          background-color: #343a40;
          color: #ffffff;
        }
        
        /* Estilos das roles */
        .role-admin td {
          background-color: #fff0f0;
          border-bottom: 1px solid #ffdddd;
        }
        .role-gerente td {
          background-color: #e3f2fd;
          border-bottom: 1px solid #bbdefb;
        }
        .role-usuario td {
          background-color: #f8f9fa;
          border-bottom: 1px solid #e9ecef;
        }
        
        /* Cards para mobile */
        .users-grid {
          display: none;
          flex-direction: column;
          gap: 15px;
        }
        
        .user-card {
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 15px;
          background: white;
        }
        
        .user-card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;
        }
        
        .user-card-username {
          font-weight: bold;
          font-size: 1.1rem;
        }
        
        .user-card-role {
          padding: 3px 8px;
          border-radius: 12px;
          font-size: 0.8rem;
          font-weight: bold;
        }
        
        .role-admin-badge { background: #fff0f0; color: #dc3545; }
        .role-gerente-badge { background: #e3f2fd; color: #007bff; }
        .role-usuario-badge { background: #f8f9fa; color: #6c757d; }
        
        .user-card-details {
          margin-bottom: 15px;
        }
        
        .user-card-org {
          font-size: 0.9rem;
          color: #6c757d;
          margin-bottom: 10px;
        }
        
        .user-card-actions {
          display: flex;
          gap: 10px;
        }
        
        .user-card-actions .btn {
          flex: 1;
          text-align: center;
        }
        
        /* Responsividade */
        @media (max-width: 768px) {
          .data-table {
            display: none;
          }
          
          .users-grid {
            display: flex;
          }
          
          .form-actions {
            display: flex;
            flex-direction: column;
            gap: 10px;
          }
          
          .form-actions .btn {
            width: 100%;
            margin: 2px 0;
          }
        }
        
        @media (min-width: 769px) {
          .users-grid {
            display: none;
          }
        }
        
        .table-wrapper {
          overflow-x: auto;
        }
        
        @media (max-width: 480px) {
          .container {
            padding: 10px;
          }
          
          .card {
            padding: 15px;
          }
          
          .user-card-actions {
            flex-direction: column;
          }
        }
      `}</style>

      <h1>Painel do Administrador</h1>

      <Link to="/admin/orgs" className="nav-link">
        Gerenciar ONGs &raquo;
      </Link>

      {error && <div className="error-message">{error}</div>}

      <div className="card">
        <h2>{editingUser ? 'Editar Usuário' : 'Criar Novo Usuário'}</h2>

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
              required={!editingUser}
              placeholder={editingUser ? 'Deixe em branco para não alterar' : ''}
            />
          </div>
          <div className="form-group">
            <label htmlFor="role">Permissão (Role):</label>
            <select
              id="role"
              value={role}
              onChange={(e) => setRole(e.target.value)}
              required
            >
              <option value="GERENTE">Gerente de ONG</option>
              <option value="ADMIN">Administrador</option>
              <option value="USUARIO">Usuário (Cliente)</option>
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="organizationId">ONG (Obrigatório para Gerente):</label>
            <select
              id="organizationId"
              value={organizationId}
              onChange={(e) => setOrganizationId(e.target.value)}
              disabled={role !== 'GERENTE'}
              required={role === 'GERENTE'}
            >
              <option value="">Nenhuma (se for Admin ou Usuário)</option>
              {organizations.map(org => (
                <option key={org.id} value={org.id}>
                  {org.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-actions">
            <button type="submit" className="btn btn-success">
              {editingUser ? 'Atualizar Usuário' : 'Salvar Novo Usuário'}
            </button>

            {editingUser && (
              <button type="button" onClick={resetForm} className="btn btn-secondary">
                Cancelar Edição
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="card">
        <h2>Usuários do Sistema ({users.length})</h2>
        <div className="table-wrapper">
          <table className="data-table user-table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Role</th>
                <th>ONG</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {sortedUsers.map(u => (
                <tr key={u.id} className={getRowClass(u.role)}>
                  <td>{u.username}</td>
                  <td>{u.role.replace('ROLE_', '')}</td>
                  <td>{u.organization ? u.organization.name : 'N/A'}</td>
                  <td>
                    <button
                      onClick={() => handleEditClick(u)}
                      className="btn btn-warning btn-sm"
                      style={{ marginRight: '5px' }}
                    >
                      Editar
                    </button>
                    <button
                      onClick={() => handleDeleteUser(u.id)}
                      className="btn btn-danger btn-sm"
                    >
                      Apagar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="users-grid">
          {sortedUsers.map(u => (
            <div key={u.id} className="user-card">
              <div className="user-card-header">
                <div className="user-card-username">{u.username}</div>
                <div className={`user-card-role ${getRowClass(u.role).replace('role-', '')}-badge`}>
                  {u.role.replace('ROLE_', '')}
                </div>
              </div>
              <div className="user-card-details">
                <div className="user-card-org">
                  <strong>ONG:</strong> {u.organization ? u.organization.name : 'N/A'}
                </div>
              </div>
              <div className="user-card-actions">
                <button
                  onClick={() => handleEditClick(u)}
                  className="btn btn-warning"
                >
                  Editar
                </button>
                <button
                  onClick={() => handleDeleteUser(u.id)}
                  className="btn btn-danger"
                >
                  Apagar
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </main>
  );
}

export default AdminUsersPage;