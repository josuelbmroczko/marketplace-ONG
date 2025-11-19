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
      setError("Acesso negado.");
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      const [usersRes, orgsRes] = await Promise.all([
        api.get('/users'),
        api.get('/org')
      ]);
      setUsers(Array.isArray(usersRes.data) ? usersRes.data : []);
      setOrganizations(Array.isArray(orgsRes.data) ? orgsRes.data : []);
      setError(null);
    } catch (err) {
      console.error("Erro:", err);
      setError(err.response?.data?.message || "Erro ao carregar dados.");
      setUsers([]);
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
      return alert("Gerentes devem pertencer a uma ONG.");
    }

    try {
      if (editingUser) {
        if (password) userData.password = password;
        await api.put(`/users/${editingUser.id}`, userData);
        alert('Usuário atualizado!');
      } else {
        if (!password) return alert("Senha obrigatória.");
        userData.password = password;
        await api.post('/users/register', userData);
        alert('Usuário criado!');
      }
      resetForm();
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || "Erro ao salvar usuário.");
    }
  };

  const handleEditClick = (userToEdit) => {
    setEditingUser(userToEdit);
    setUsername(userToEdit.username);
    setRole(userToEdit.role.replace('ROLE_', ''));

    let rawRole = userToEdit.role;
    if (rawRole.startsWith('ROLE_')) rawRole = rawRole.replace('ROLE_', '');
    setRole(rawRole);

    setOrganizationId(userToEdit.organization ? userToEdit.organization.id : '');
    setPassword('');
    setError(null);
    window.scrollTo(0, 0);
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm('Tem certeza que deseja apagar este usuário?')) return;
    try {
      await api.delete(`/users/${userId}`);
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || "Erro ao apagar usuário.");
    }
  };

  const roleOrder = { 'ROLE_ADMIN': 1, 'ROLE_GERENTE': 2, 'ROLE_USUARIO': 3 };
  const sortedUsers = [...users].sort((a, b) => {
    const roleA = roleOrder[a.role] || 99;
    const roleB = roleOrder[b.role] || 99;
    if (roleA !== roleB) return roleA - roleB;
    const orgA = a.organization?.name?.toUpperCase() || 'ZZZ';
    const orgB = b.organization?.name?.toUpperCase() || 'ZZZ';
    if (orgA !== orgB) return orgA.localeCompare(orgB);
    return a.username.localeCompare(b.username);
  });

  const getBadgeStyle = (role) => {
    const r = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
    switch (r) {
      case 'ROLE_ADMIN': return 'badge-admin';
      case 'ROLE_GERENTE': return 'badge-gerente';
      default: return 'badge-user';
    }
  };

  if (loading) return <div style={{ padding: '50px', textAlign: 'center', color: '#7f8c8d' }}>Carregando sistema...</div>;

  if (!user || user.role !== 'ROLE_ADMIN') {
    return <div style={{ padding: '20px', color: 'red' }}>Acesso Negado.</div>;
  }

  return (
    <main className="admin-container">
      <style>{`
        :root {
          --primary: #4a90e2;
          --text-dark: #2c3e50;
          --text-light: #7f8c8d;
          --success: #27ae60;
          --danger: #e74c3c;
          --warning: #f39c12;
          --white: #ffffff;
          --border-radius: 12px;
          --shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
        }

        .admin-container {
          padding: 20px;
          max-width: 1100px;
          margin: 0 auto;
          font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
          color: var(--text-dark);
        }

        h1, h2 { margin-top: 0; color: var(--text-dark); }
        h1 { font-size: 1.8rem; margin-bottom: 10px; }
        h2 { font-size: 1.3rem; margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 10px; }

        .nav-link {
          color: var(--primary); text-decoration: none; font-weight: 500;
          display: inline-flex; align-items: center; gap: 5px; margin-bottom: 30px;
        }
        .nav-link:hover { text-decoration: underline; }

        /* Cards */
        .card {
          background: var(--white); border-radius: var(--border-radius);
          box-shadow: var(--shadow); padding: 25px; margin-bottom: 25px;
          border: 1px solid #eee;
        }

        /* Formulário Grid */
        .form-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
          gap: 20px;
        }

        .form-group label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 0.9rem; }
        input, select {
          width: 100%; padding: 10px; border: 1px solid #ddd;
          border-radius: 8px; font-size: 0.95rem; box-sizing: border-box;
        }
        input:focus, select:focus { border-color: var(--primary); outline: none; }

        /* Botões */
        .btn {
          padding: 10px 20px; border: none; border-radius: 8px; cursor: pointer;
          font-weight: 600; transition: opacity 0.2s; font-size: 0.9rem;
        }
        .btn:hover { opacity: 0.9; }
        .btn-success { background: var(--success); color: white; }
        .btn-warning { background: var(--warning); color: white; }
        .btn-danger { background: var(--danger); color: white; }
        .btn-secondary { background: #cbd5e0; color: #2d3748; }
        .btn-sm { padding: 6px 12px; font-size: 0.8rem; margin-left: 5px; }

        /* Badges de Role */
        .badge {
          padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: bold; text-transform: uppercase;
        }
        .badge-admin { background: #fee2e2; color: #b91c1c; border: 1px solid #fecaca; }
        .badge-gerente { background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd; }
        .badge-user { background: #f3f4f6; color: #4b5563; border: 1px solid #e5e7eb; }

        /* Tabela Desktop */
        .table-wrapper { overflow-x: auto; }
        .data-table { width: 100%; border-collapse: collapse; }
        .data-table th, .data-table td { padding: 15px; text-align: left; border-bottom: 1px solid #f0f0f0; }
        .data-table th { background: #f9fafb; color: var(--text-light); font-weight: 600; font-size: 0.9rem; }
        .data-table tr:hover td { background: #fafafa; }
        
        /* Mobile Card View */
        .mobile-list { display: none; flex-direction: column; gap: 15px; }
        .mobile-card {
          border: 1px solid #eee; border-radius: 8px; padding: 15px; background: white;
          display: flex; flex-direction: column; gap: 10px;
        }
        .mobile-header { display: flex; justify-content: space-between; align-items: center; }
        .mobile-user { font-weight: bold; font-size: 1.1rem; }

        @media (max-width: 768px) {
          .data-table { display: none; }
          .mobile-list { display: flex; }
        }

        .error-message {
          background: #fee2e2; color: #b91c1c; padding: 15px;
          border-radius: 8px; margin-bottom: 20px;
        }
      `}</style>

      <Link to="/admin/orgs" className="nav-link">
        ← Gerenciar Organizações
      </Link>

      <h1>Gerenciar Usuários</h1>
      <p style={{ color: '#7f8c8d', marginBottom: '25px' }}>Controle de acesso e cadastro de novos administradores ou gerentes.</p>

      {error && <div className="error-message">{error}</div>}

      <div className="card" style={{ borderLeft: editingUser ? '5px solid var(--warning)' : '5px solid var(--success)' }}>
        <h2>{editingUser ? ` Editando: ${editingUser.username}` : 'Novo Usuário'}</h2>

        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-group">
              <label>Nome de Usuário</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                placeholder="Ex: joao.silva"
              />
            </div>

            <div className="form-group">
              <label>Senha {editingUser && <small style={{ fontWeight: 'normal', color: '#999' }}>(opcional)</small>}</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required={!editingUser}
                placeholder={editingUser ? "••••••••" : "Senha forte"}
              />
            </div>

            <div className="form-group">
              <label>Permissão</label>
              <select value={role} onChange={(e) => setRole(e.target.value)} required>
                <option value="GERENTE">Gerente de ONG</option>
                <option value="ADMIN">Administrador</option>
                <option value="USUARIO">Usuário Comum</option>
              </select>
            </div>

            <div className="form-group">
              <label>Vincular ONG</label>
              <select
                value={organizationId}
                onChange={(e) => setOrganizationId(e.target.value)}
                disabled={role !== 'GERENTE'}
                required={role === 'GERENTE'}
                style={{ opacity: role !== 'GERENTE' ? 0.5 : 1 }}
              >
                <option value="">-- Nenhuma --</option>
                {organizations.map(org => (
                  <option key={org.id} value={org.id}>{org.name}</option>
                ))}
              </select>
            </div>
          </div>

          <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
            <button type="submit" className={editingUser ? "btn btn-warning" : "btn btn-success"}>
              {editingUser ? 'Salvar Alterações' : 'Criar Usuário'}
            </button>

            {editingUser && (
              <button type="button" onClick={resetForm} className="btn btn-secondary">
                Cancelar
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="card">
        <h2>Usuários Cadastrados ({users.length})</h2>

        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Usuário</th>
                <th>Permissão</th>
                <th>Organização</th>
                <th style={{ textAlign: 'right' }}>Ações</th>
              </tr>
            </thead>
            <tbody>
              {sortedUsers.map(u => (
                <tr key={u.id}>
                  <td>
                    <strong>{u.username}</strong>
                    <div style={{ fontSize: '0.75rem', color: '#999' }}>ID: {u.id.substring(0, 6)}...</div>
                  </td>
                  <td>
                    <span className={`badge ${getBadgeStyle(u.role)}`}>
                      {u.role.replace('ROLE_', '')}
                    </span>
                  </td>
                  <td>
                    {u.organization ? (
                      <span style={{ fontWeight: '500', color: '#4a90e2' }}> {u.organization.name}</span>
                    ) : (
                      <span style={{ color: '#ccc' }}>—</span>
                    )}
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <button onClick={() => handleEditClick(u)} className="btn btn-warning btn-sm">✏️</button>
                    <button onClick={() => handleDeleteUser(u.id)} className="btn btn-danger btn-sm">🗑️</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="mobile-list">
          {sortedUsers.map(u => (
            <div key={u.id} className="mobile-card">
              <div className="mobile-header">
                <span className="mobile-user">{u.username}</span>
                <span className={`badge ${getBadgeStyle(u.role)}`}>{u.role.replace('ROLE_', '')}</span>
              </div>

              <div style={{ fontSize: '0.9rem', color: '#666' }}>
                {u.organization ? `ONG: ${u.organization.name}` : 'Sem organização vinculada'}
              </div>

              <div style={{ display: 'flex', gap: '10px', marginTop: '5px' }}>
                <button onClick={() => handleEditClick(u)} className="btn btn-warning" style={{ flex: 1 }}>Editar</button>
                <button onClick={() => handleDeleteUser(u.id)} className="btn btn-danger" style={{ flex: 1 }}>Excluir</button>
              </div>
            </div>
          ))}
        </div>

      </div>
    </main>
  );
}

export default AdminUsersPage;