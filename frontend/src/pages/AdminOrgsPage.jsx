import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api.js';

function AdminOrgsPage() {
  const [orgs, setOrgs] = useState([]);
  const [orgName, setOrgName] = useState('');
  const [editingOrg, setEditingOrg] = useState(null);

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
        setError("Não foi possível carregar as ONGs. Verifique suas permissões.");
        setOrgs([]);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    setLoading(true);
    fetchOrgs();
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!orgName) return;

    if (editingOrg) {
      api.put(`/org/${editingOrg.id}`, { name: orgName })
        .then(() => {
          alert('ONG atualizada com sucesso!');
          resetForm();
          fetchOrgs();
        })
        .catch(error => {
          console.error("Erro update:", error);
          setError("Erro ao atualizar ONG.");
        });
    } else {
      api.post('/org', { name: orgName })
        .then(() => {
          alert('ONG criada com sucesso!');
          resetForm();
          fetchOrgs();
        })
        .catch(error => {
          console.error("Erro create:", error);
          setError("Erro ao criar ONG.");
        });
    }
  };

  const handleDelete = (id) => {
    if (window.confirm("ATENÇÃO: Deletar a ONG irá apagar TODOS os produtos vinculados a ela. Tem certeza?")) {
      api.delete(`/org/${id}`)
        .then(() => {
          alert('ONG removida.');
          if (editingOrg && editingOrg.id === id) resetForm();
          fetchOrgs();
        })
        .catch(err => {
          alert('Erro ao deletar ONG.');
          console.error(err);
        });
    }
  };


  const handleEditClick = (org) => {
    setEditingOrg(org);
    setOrgName(org.name);
    window.scrollTo(0, 0);
  };


  const resetForm = () => {
    setEditingOrg(null);
    setOrgName('');
    setError(null);
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '50px', color: '#7f8c8d', fontFamily: 'Segoe UI' }}>
        Carregando dados...
      </div>
    );
  }

  return (
    <main className="admin-container">
      <style>{`
        :root {
          --primary: #4a90e2;
          --secondary: #f5f7fa;
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
          max-width: 1000px;
          margin: 0 auto;
          font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
          color: var(--text-dark);
        }

        h1, h2 { color: var(--text-dark); margin-top: 0; }
        h1 { margin-bottom: 10px; font-size: 1.8rem; }
        h2 { font-size: 1.2rem; margin-bottom: 15px; border-bottom: 2px solid #f0f0f0; padding-bottom: 10px; }

        .nav-link {
          color: var(--primary); text-decoration: none; font-weight: 500;
          display: inline-flex; align-items: center; gap: 5px; margin-bottom: 20px;
        }
        .nav-link:hover { text-decoration: underline; }

        .card {
          background: var(--white); border-radius: var(--border-radius);
          box-shadow: var(--shadow); padding: 24px; margin-bottom: 24px;
          border: 1px solid #eee;
        }

        /* Formulário */
        .form-group { flex: 1; min-width: 250px; }
        .form-group label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 0.9rem; }
        input[type="text"] {
          width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px;
          font-size: 1rem; box-sizing: border-box;
        }
        input[type="text"]:focus { border-color: var(--primary); outline: none; }

        /* Botões */
        .btn {
          padding: 12px 20px; border: none; border-radius: 8px; cursor: pointer;
          font-weight: 600; font-size: 0.95rem; transition: opacity 0.2s;
        }
        .btn:hover { opacity: 0.9; }
        .btn-success { background: var(--success); color: white; }
        .btn-warning { background: var(--warning); color: white; } /* Editar */
        .btn-danger { background: var(--danger); color: white; }   /* Deletar */
        .btn-secondary { background: #cbd5e0; color: #2d3748; }    /* Cancelar */
        .btn-sm { padding: 8px 12px; font-size: 0.85rem; margin-left: 5px; }

        /* Tabela */
        .table-wrapper { overflow-x: auto; }
        .data-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .data-table th, .data-table td { padding: 15px; text-align: left; border-bottom: 1px solid #f0f0f0; }
        .data-table th { background-color: #f8f9fa; color: var(--text-light); font-weight: 600; }
        .data-table tr:hover td { background-color: #fafafa; }
        .id-column { width: 80px; color: var(--text-light); font-family: monospace; font-size: 0.8rem; }
        .actions-column { text-align: right; width: 180px; }

        .error-message {
          background: #fee2e2; color: #b91c1c; padding: 15px;
          border-radius: 8px; margin-bottom: 20px; border: 1px solid #fecaca;
        }
      `}</style>

      <Link to="/admin/users" className="nav-link">← Voltar para Gerenciar Usuários</Link>

      <h1> Gerenciar Organizações</h1>
      <p style={{ color: '#7f8c8d', marginBottom: '30px' }}>Cadastre, edite ou remova ONGs parceiras.</p>

      {error && <div className="error-message"> {error}</div>}

      <div className="card" style={{ borderLeft: editingOrg ? '5px solid var(--warning)' : '5px solid var(--success)' }}>
        <h2>{editingOrg ? ' Editar ONG' : 'Nova ONG'}</h2>

        <form onSubmit={handleSubmit} style={{ display: 'flex', alignItems: 'flex-end', gap: '15px', flexWrap: 'wrap' }}>
          <div className="form-group">
            <label htmlFor="name">Nome da Organização:</label>
            <input
              type="text"
              id="name"
              value={orgName}
              onChange={(e) => setOrgName(e.target.value)}
              placeholder="Ex: ONG Anjos de Patas"
              required
            />
          </div>

          <div style={{ display: 'flex', gap: '10px' }}>
            <button type="submit" className={editingOrg ? "btn btn-warning" : "btn btn-success"}>
              {editingOrg ? 'Atualizar' : 'Cadastrar'}
            </button>

            {editingOrg && (
              <button type="button" onClick={resetForm} className="btn btn-secondary">
                Cancelar
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="card">
        <h2>ONGs Cadastradas ({orgs.length})</h2>

        {orgs.length === 0 ? (
          <div style={{ padding: '20px', textAlign: 'center', color: '#7f8c8d' }}>Nenhuma organização cadastrada.</div>
        ) : (
          <div className="table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th className="id-column">ID</th>
                  <th>Nome da Organização</th>
                  <th className="actions-column">Ações</th>
                </tr>
              </thead>
              <tbody>
                {orgs.map(org => (
                  <tr key={org.id}>
                    <td className="id-column">{org.id.substring(0, 8)}...</td>
                    <td><strong>{org.name}</strong></td>
                    <td className="actions-column">
                      <button
                        onClick={() => handleEditClick(org)}
                        className="btn btn-warning btn-sm"
                        title="Editar nome"
                      >
                        ✏️
                      </button>
                      <button
                        onClick={() => handleDelete(org.id)}
                        className="btn btn-danger btn-sm"
                        title="Excluir ONG e Produtos"
                      >
                        🗑️
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </main>
  );
}

export default AdminOrgsPage;