import React, { useState, useEffect } from 'react';
import api from '../api.js';
import { useAuth } from '../AuthContext.jsx';
import { useNavigate } from 'react-router-dom';

const allCategories = [
  { value: 'ALIMENTO', name: 'Alimento' },
  { value: 'BRINQUEDO', name: 'Brinquedo' },
  { value: 'ACESSORIO', name: 'Acessório' },
  { value: 'HIGIENE', name: 'Higiene' },
  { value: 'MEDICAMENTO', name: 'Medicamento' },
  { value: 'OUTRO', name: 'Outro' }
];

function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);
  useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(handler);
  }, [value, delay]);
  return debouncedValue;
}

function ProductPage() {
  const [produtos, setProdutos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user } = useAuth();
  const navigate = useNavigate();

  const [productName, setProductName] = useState('');
  const [price, setPrice] = useState('');
  const [quantity, setQuantity] = useState(0);
  const [category, setCategory] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [description, setDescription] = useState('');
  const [organizations, setOrganizations] = useState([]);
  const [adminSelectedOrg, setAdminSelectedOrg] = useState('');
  const [amout, setAmout] = useState('');
  const [editingProduct, setEditingProduct] = useState(null);

  const [aiQuery, setAiQuery] = useState('');
  const [aiMessage, setAiMessage] = useState(null);
  const [filters, setFilters] = useState({
    name: '', minPrice: '', maxPrice: '', category: '', sort: '',
  });

  const [cartQuantities, setCartQuantities] = useState({});

  const [viewProduct, setViewProduct] = useState(null);

  const debouncedNameFilter = useDebounce(filters.name, 500);
  const debouncedAiQuery = useDebounce(aiQuery, 1000);


  useEffect(() => {
    if (debouncedAiQuery) {
      setLoading(true);
      setAiMessage(null);
      const params = new URLSearchParams();
      params.append('aiQuery', debouncedAiQuery);

      api.get('/product', { params })
        .then(response => {
          setProdutos(response.data.products || []);
          setAiMessage(response.data.friendlyMessage || "Busca concluída.");
          if (response.data.filters) {
            setFilters({
              ...filters,
              name: response.data.filters.name || '',
              minPrice: response.data.filters.minPrice || '',
              maxPrice: response.data.filters.maxPrice || '',
              category: response.data.filters.category || '',
              sort: response.data.filters.sort || ''
            });
          }
          setLoading(false);
        })
        .catch(error => {
          console.error("Erro IA:", error);
          setError("Erro ao carregar produtos via IA.");
          setLoading(false);
        });
    }
  }, [debouncedAiQuery]);

  useEffect(() => {
    if (!debouncedAiQuery) {
      setLoading(true);
      setAiMessage(null);
      const params = new URLSearchParams();
      if (debouncedNameFilter) params.append('name', debouncedNameFilter);
      if (filters.minPrice) params.append('minPrice', filters.minPrice);
      if (filters.maxPrice) params.append('maxPrice', filters.maxPrice);
      if (filters.category) params.append('category', filters.category);
      if (filters.sort) params.append('sort', filters.sort);

      api.get('/product', { params })
        .then(response => {
          setProdutos(Array.isArray(response.data) ? response.data : []);
          setLoading(false);
        })
        .catch(error => {
          console.error("Erro Manual:", error);
          setError("Erro ao carregar produtos.");
          setLoading(false);
        });
    }
  }, [debouncedNameFilter, filters.minPrice, filters.maxPrice, filters.category, filters.sort, debouncedAiQuery]);

  useEffect(() => {
    if (user && user.role === 'ROLE_ADMIN') {
      api.get('/org').then(res => setOrganizations(Array.isArray(res.data) ? res.data : []));
    }
  }, [user]);


  const handleAddToCart = (productId, maxQuantity) => {
    const quantityToAdd = cartQuantities[productId] || 1;
    if (quantityToAdd > maxQuantity) return alert('Quantidade maior que o estoque.');
    if (quantityToAdd <= 0) return alert('Quantidade inválida.');

    api.post(`/cart/add/${productId}`, { quantity: quantityToAdd })
      .then(() => {
        alert('Adicionado ao carrinho!');
        setProdutos(prev => prev.map(p => p.id === productId ? { ...p, quantity: p.quantity - quantityToAdd } : p));
        if (viewProduct && viewProduct.id === productId) {
          setViewProduct(prev => ({ ...prev, quantity: prev.quantity - quantityToAdd }));
        }
      })
      .catch(err => {
        if (err.response?.status === 401) navigate('/login');
        else alert('Erro ao adicionar: ' + (err.response?.data?.message || 'Erro desconhecido'));
      });
  };

  const resetForm = () => {
    setProductName(''); setPrice(''); setQuantity(0); setCategory('');
    setImageUrl(''); setDescription(''); setAmout(''); setAdminSelectedOrg('');
    setEditingProduct(null); setError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (user.role === 'ROLE_ADMIN' && !adminSelectedOrg && !editingProduct) return alert("Selecione a ONG.");

    const productData = {
      productName, price: parseFloat(price), quantity: parseInt(quantity, 10),
      category, imageUrl, description, amout,
      organization: user.role === 'ROLE_ADMIN' ? { id: adminSelectedOrg } : null
    };

    try {
      if (editingProduct) {
        const res = await api.put(`/product/${editingProduct.id}`, productData);
        alert('Atualizado!');
        setProdutos(prev => prev.map(p => p.id === editingProduct.id ? res.data : p));
      } else {
        const res = await api.post('/product', productData);
        alert('Criado!');
        setProdutos(prev => [res.data, ...prev]);
      }
      resetForm();
    } catch (err) {
      setError('Erro ao salvar: ' + (err.response?.data?.message || 'Erro desconhecido'));
    }
  };

  const handleDeleteProduct = async (productId) => {
    if (window.confirm('Deletar este produto?')) {
      try {
        await api.delete(`/product/${productId}`);
        setProdutos(prev => prev.filter(p => p.id !== productId));
      } catch (err) { alert('Erro ao deletar.'); }
    }
  };

  const handleEditClick = (product) => {
    setEditingProduct(product);
    setProductName(product.productName); setPrice(product.price); setQuantity(product.quantity);
    setCategory(product.category); setImageUrl(product.imageUrl || ''); setDescription(product.description || '');
    setAmout(product.amout || '');
    if (user.role === 'ROLE_ADMIN') setAdminSelectedOrg(product.organization?.id || '');
    window.scrollTo(0, 0);
  };

  const handleFilterChange = (e) => setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }));
  const handleAiQueryChange = (e) => { setAiQuery(e.target.value); setFilters({ name: '', minPrice: '', maxPrice: '', category: '', sort: '' }); };
  const handleManualFilterChange = (e) => { setAiQuery(''); handleFilterChange(e); };
  const clearFilters = () => { setAiQuery(''); setFilters({ name: '', minPrice: '', maxPrice: '', category: '', sort: '' }); };
  const handleCartQtyChange = (productId, value) => {
    const qty = parseInt(value, 10);
    setCartQuantities(prev => ({ ...prev, [productId]: isNaN(qty) ? 1 : qty }));
  };


  return (
    <div className="product-page-container">
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
          --shadow-hover: 0 8px 15px rgba(0, 0, 0, 0.1);
        }

        .product-page-container {
          padding: 20px;
          max-width: 1200px;
          margin: 0 auto;
          font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
          color: var(--text-dark);
        }

        .card {
          background: var(--white);
          border-radius: var(--border-radius);
          padding: 24px;
          margin-bottom: 24px;
          box-shadow: var(--shadow);
          border: 1px solid #eee;
        }

        h2 { margin-top: 0; color: var(--text-dark); font-size: 1.5rem; margin-bottom: 20px; }

        /* Forms & Inputs */
        .form-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
          gap: 15px;
        }

        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 0.9rem; }
        
        input, select, textarea {
          width: 100%;
          padding: 10px 12px;
          border: 1px solid #ddd;
          border-radius: 8px;
          font-size: 0.95rem;
          transition: border-color 0.2s;
          box-sizing: border-box;
        }
        input:focus, select:focus, textarea:focus {
          border-color: var(--primary);
          outline: none;
        }

        /* Buttons */
        .btn {
          padding: 10px 18px;
          border: none;
          border-radius: 8px;
          cursor: pointer;
          font-weight: 600;
          font-size: 0.9rem;
          transition: all 0.2s;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          gap: 5px;
        }
        .btn:disabled { opacity: 0.6; cursor: not-allowed; }
        .btn-success { background: var(--success); color: white; }
        .btn-success:hover:not(:disabled) { background: #219150; }
        .btn-primary { background: var(--primary); color: white; }
        .btn-primary:hover:not(:disabled) { background: #357abd; }
        .btn-warning { background: var(--warning); color: white; }
        .btn-danger { background: var(--danger); color: white; }
        .btn-secondary { background: #cbd5e0; color: #2d3748; }
        .btn-outline { background: transparent; border: 1px solid #cbd5e0; color: var(--text-light); }
        .btn-sm { padding: 5px 10px; font-size: 0.8rem; }

        /* Product Grid */
        .products-display-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
          gap: 25px;
        }

        .product-card {
          background: white;
          border-radius: var(--border-radius);
          box-shadow: var(--shadow);
          transition: transform 0.2s, box-shadow 0.2s;
          overflow: hidden;
          display: flex;
          flex-direction: column;
          border: 1px solid #f0f0f0;
        }
        .product-card:hover {
          transform: translateY(-5px);
          box-shadow: var(--shadow-hover);
        }

        .card-img-wrapper {
          width: 100%;
          height: 200px;
          background: #f8f9fa;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          position: relative;
        }
        .card-img-wrapper img {
          max-height: 100%;
          max-width: 100%;
          object-fit: contain;
          padding: 10px;
        }
        .zoom-hint {
          position: absolute;
          bottom: 10px;
          right: 10px;
          background: rgba(0,0,0,0.6);
          color: white;
          padding: 4px 8px;
          border-radius: 4px;
          font-size: 0.75rem;
          opacity: 0;
          transition: opacity 0.2s;
        }
        .card-img-wrapper:hover .zoom-hint { opacity: 1; }

        .card-content { padding: 15px; flex: 1; display: flex; flex-direction: column; }
        .card-title { font-size: 1.1rem; font-weight: 700; margin-bottom: 5px; color: var(--text-dark); cursor: pointer; }
        .card-description { font-size: 0.85rem; color: var(--text-light); margin-bottom: 10px; flex-grow: 1; }
        .card-price { font-size: 1.2rem; font-weight: bold; color: var(--success); margin-bottom: 5px; }
        .card-stock { font-size: 0.8rem; margin-bottom: 15px; }
        .text-green { color: var(--success); }
        .text-red { color: var(--danger); }

        .quantity-controls {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          margin-bottom: 10px;
        }
        .qty-display { width: 30px; text-align: center; font-weight: bold; }

        /* Admin Table (Keep it for desktop admin view if preferred, but simplified) */
        .admin-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .admin-table th, .admin-table td { padding: 12px; border-bottom: 1px solid #eee; text-align: left; }
        .admin-table th { background: #f8f9fa; font-weight: 600; color: var(--text-light); }

        /* MODAL STYLES */
        .modal-overlay {
          position: fixed; top: 0; left: 0; right: 0; bottom: 0;
          background: rgba(0, 0, 0, 0.7);
          backdrop-filter: blur(4px);
          z-index: 1000;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 20px;
        }
        .modal-content {
          background: white;
          border-radius: 16px;
          width: 100%;
          max-width: 900px;
          max-height: 90vh;
          overflow-y: auto;
          position: relative;
          box-shadow: 0 20px 40px rgba(0,0,0,0.3);
          display: flex;
          flex-direction: column;
        }
        .modal-close-btn {
          position: absolute; top: 15px; right: 15px;
          background: #f1f1f1; border: none;
          width: 36px; height: 36px; border-radius: 50%;
          font-size: 1.2rem; cursor: pointer; z-index: 10;
          display: flex; align-items: center; justify-content: center;
        }
        .modal-close-btn:hover { background: #e0e0e0; }

        .modal-body { display: flex; flex-direction: column; }
        @media(min-width: 768px) {
          .modal-body { flex-direction: row; }
          .modal-image-section { width: 50%; background: #f8f9fa; display: flex; align-items: center; justify-content: center; padding: 40px; }
          .modal-info-section { width: 50%; padding: 40px; }
        }
        
        .modal-image-section img { max-width: 100%; max-height: 500px; object-fit: contain; border-radius: 8px; box-shadow: 0 5px 15px rgba(0,0,0,0.05); }
        .modal-info-section { padding: 20px; }
        .modal-title { font-size: 2rem; margin: 0 0 10px 0; color: var(--text-dark); }
        .modal-meta { margin-bottom: 20px; color: var(--text-light); font-size: 0.9rem; }
        .modal-price { font-size: 2rem; color: var(--success); font-weight: bold; margin-bottom: 20px; }
        .modal-desc { font-size: 1rem; line-height: 1.6; color: #555; margin-bottom: 30px; }

        /* Responsividade */
        @media (max-width: 768px) {
          .products-display-grid { grid-template-columns: 1fr; }
          .admin-table { display: none; } /* Esconde tabela em mobile */
        }
      `}</style>

      {error && <div style={{ background: '#fee2e2', color: '#b91c1c', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>{error}</div>}
      {aiMessage && <div style={{ background: '#e0f2fe', color: '#0369a1', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>✨ {aiMessage}</div>}

      {user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_GERENTE') && (
        <div className="card" style={{ borderLeft: '5px solid var(--warning)' }}>
          <h2>{editingProduct ? '✏️ Editar Produto' : '➕ Cadastrar Novo Produto'}</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-grid">
              <div className="form-group">
                <label>Nome</label>
                <input type="text" value={productName} onChange={(e) => setProductName(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Preço (R$)</label>
                <input type="number" value={price} onChange={(e) => setPrice(e.target.value)} step="0.01" min="0" required />
              </div>
              <div className="form-group">
                <label>Estoque</label>
                <input type="number" value={quantity} onChange={(e) => setQuantity(e.target.value)} min="0" required />
              </div>
              <div className="form-group">
                <label>Categoria</label>
                <select value={category} onChange={(e) => setCategory(e.target.value)} required>
                  <option value="">Selecione...</option>
                  {allCategories.map(cat => (<option key={cat.value} value={cat.value}>{cat.name}</option>))}
                </select>
              </div>
              <div className="form-group">
                <label>URL da Imagem</label>
                <input type="text" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} placeholder="http://..." />
              </div>
              <div className="form-group">
                <label>Peso/Medida (ex: 1kg)</label>
                <input type="text" value={amout} onChange={(e) => setAmout(e.target.value)} />
              </div>
            </div>

            {user.role === 'ROLE_ADMIN' && (
              <div className="form-group">
                <label>ONG Proprietária</label>
                <select value={adminSelectedOrg} onChange={(e) => setAdminSelectedOrg(e.target.value)} required={!editingProduct}>
                  <option value="">Selecione a ONG...</option>
                  {organizations.map(org => (<option key={org.id} value={org.id}>{org.name}</option>))}
                </select>
              </div>
            )}

            <div className="form-group">
              <label>Descrição Detalhada</label>
              <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows="3"></textarea>
            </div>

            <div style={{ marginTop: '15px' }}>
              <button type="submit" className="btn btn-success">
                {editingProduct ? 'Salvar Alterações' : 'Cadastrar Produto'}
              </button>
              {editingProduct && (
                <button type="button" onClick={resetForm} className="btn btn-outline" style={{ marginLeft: '10px' }}>
                  Cancelar
                </button>
              )}
            </div>
          </form>
        </div>
      )}

      <div className="card">
        {user && user.role === 'ROLE_USUARIO' && (
          <div style={{ marginBottom: '20px' }}>
            <label style={{ fontWeight: 'bold', display: 'block', marginBottom: '8px' }}>🤖 Busca Inteligente (IA)</label>
            <input
              type="text"
              value={aiQuery}
              onChange={handleAiQueryChange}
              placeholder="Ex: quero uma ração barata para cachorro adulto..."
              style={{ width: '100%', padding: '12px', border: '2px solid #4a90e2' }}
            />
          </div>
        )}

        <div className="filter-form form-grid">
          <input type="text" name="name" value={filters.name} onChange={handleManualFilterChange} placeholder="🔍 Buscar por nome..." />
          <div style={{ display: 'flex', gap: '10px' }}>
            <input type="number" name="minPrice" value={filters.minPrice} onChange={handleManualFilterChange} placeholder="Min R$" />
            <input type="number" name="maxPrice" value={filters.maxPrice} onChange={handleManualFilterChange} placeholder="Max R$" />
          </div>
          <select name="category" value={filters.category} onChange={handleManualFilterChange}>
            <option value="">Todas as Categorias</option>
            {allCategories.map(cat => (<option key={cat.value} value={cat.value}>{cat.name}</option>))}
          </select>
          <select name="sort" value={filters.sort} onChange={handleManualFilterChange}>
            <option value="">Ordenar por...</option>
            <option value="price_asc">Menor Preço</option>
            <option value="price_desc">Maior Preço</option>
            <option value="name_asc">Nome (A-Z)</option>
          </select>
          <button type="button" onClick={clearFilters} className="btn btn-outline">Limpar</button>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '40px', color: '#7f8c8d' }}>Carregando produtos...</div>
      ) : produtos.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '40px', background: '#fff', borderRadius: '8px' }}>Nenhum produto encontrado.</div>
      ) : (
        <div className="products-display-grid">
          {produtos.map(prod => (
            <div key={prod.id} className="product-card">
              <div className="card-img-wrapper" onClick={() => setViewProduct(prod)}>
                <img
                  src={prod.imageUrl || 'https://via.placeholder.com/300?text=Sem+Imagem'}
                  alt={prod.productName}
                  onError={(e) => { e.target.src = 'https://via.placeholder.com/300?text=Erro+Imagem'; }}
                />
                <span className="zoom-hint">🔍 Ver Detalhes</span>
              </div>

              <div className="card-content">
                <h3 className="card-title" onClick={() => setViewProduct(prod)}>{prod.productName}</h3>
                <div className="card-price">R$ {prod.price ? prod.price.toFixed(2) : '0.00'}</div>

                <div className="card-stock">
                  {prod.quantity > 0 ?
                    <span className="text-green">✅ {prod.quantity} disponíveis</span> :
                    <span className="text-red">❌ Esgotado</span>
                  }
                  {prod.amout && <span style={{ marginLeft: '10px', color: '#777' }}>({prod.amout})</span>}
                </div>

                <div className="card-description">
                  {prod.description ? (prod.description.length > 60 ? prod.description.substring(0, 60) + '...' : prod.description) : 'Sem descrição.'}
                </div>

                {user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_GERENTE') && (
                  <div style={{ display: 'flex', gap: '10px', marginTop: 'auto' }}>
                    <button onClick={() => handleEditClick(prod)} className="btn btn-warning btn-sm" style={{ flex: 1 }}>Editar</button>
                    <button onClick={() => handleDeleteProduct(prod.id)} className="btn btn-danger btn-sm">🗑️</button>
                  </div>
                )}

                {user && user.role === 'ROLE_USUARIO' && (
                  <div style={{ marginTop: 'auto' }}>
                    {prod.quantity > 0 ? (
                      <>
                        <div className="quantity-controls">
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => handleCartQtyChange(prod.id, (cartQuantities[prod.id] || 1) - 1)}
                            disabled={(cartQuantities[prod.id] || 1) <= 1}
                          >-</button>
                          <span className="qty-display">{cartQuantities[prod.id] || 1}</span>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => handleCartQtyChange(prod.id, (cartQuantities[prod.id] || 1) + 1)}
                            disabled={(cartQuantities[prod.id] || 1) >= prod.quantity}
                          >+</button>
                        </div>
                        <button
                          onClick={() => handleAddToCart(prod.id, prod.quantity)}
                          className="btn btn-success"
                          style={{ width: '100%' }}
                        >
                          Adicionar ao Carrinho
                        </button>
                      </>
                    ) : (
                      <button className="btn btn-secondary" disabled style={{ width: '100%' }}>Indisponível</button>
                    )}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {viewProduct && (
        <div className="modal-overlay" onClick={() => setViewProduct(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <button className="modal-close-btn" onClick={() => setViewProduct(null)}>×</button>

            <div className="modal-body">
              <div className="modal-image-section">
                <img
                  src={viewProduct.imageUrl || 'https://via.placeholder.com/600?text=Sem+Imagem'}
                  alt={viewProduct.productName}
                  onError={(e) => { e.target.src = 'https://via.placeholder.com/600?text=Erro+Imagem'; }}
                />
              </div>
              <div className="modal-info-section">
                <h2 className="modal-title">{viewProduct.productName}</h2>
                <div className="modal-meta">
                  Categoria: <strong>{viewProduct.category}</strong>
                  {viewProduct.amout && <span> • Peso: {viewProduct.amout}</span>}
                </div>
                <div className="modal-price">R$ {viewProduct.price ? viewProduct.price.toFixed(2) : '0.00'}</div>

                <p className="modal-desc">{viewProduct.description || "Sem descrição detalhada para este produto."}</p>

                <div style={{ marginBottom: '20px' }}>
                  {viewProduct.quantity > 0 ?
                    <span className="text-green" style={{ fontSize: '1.1rem' }}>✅ Disponível ({viewProduct.quantity} em estoque)</span> :
                    <span className="text-red" style={{ fontSize: '1.1rem' }}>❌ Produto Esgotado</span>
                  }
                </div>

                {user && user.role === 'ROLE_USUARIO' && viewProduct.quantity > 0 && (
                  <div style={{ background: '#f5f7fa', padding: '20px', borderRadius: '12px' }}>
                    <label style={{ display: 'block', marginBottom: '10px', fontWeight: 'bold' }}>Quantidade:</label>
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <div className="quantity-controls" style={{ margin: 0 }}>
                        <button
                          className="btn btn-secondary"
                          onClick={() => handleCartQtyChange(viewProduct.id, (cartQuantities[viewProduct.id] || 1) - 1)}
                          disabled={(cartQuantities[viewProduct.id] || 1) <= 1}
                        >-</button>
                        <span className="qty-display" style={{ fontSize: '1.2rem', width: '40px' }}>{cartQuantities[viewProduct.id] || 1}</span>
                        <button
                          className="btn btn-secondary"
                          onClick={() => handleCartQtyChange(viewProduct.id, (cartQuantities[viewProduct.id] || 1) + 1)}
                          disabled={(cartQuantities[viewProduct.id] || 1) >= viewProduct.quantity}
                        >+</button>
                      </div>
                      <button
                        onClick={() => handleAddToCart(viewProduct.id, viewProduct.quantity)}
                        className="btn btn-success"
                        style={{ flex: 1 }}
                      >
                        Adicionar ao Carrinho
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {user && user.role === 'ROLE_USUARIO' && (
        <div style={{ marginTop: '40px', textAlign: 'center' }}>
          <button
            onClick={() => navigate('/cart')}
            className="btn btn-primary"
            style={{ padding: '15px 40px', fontSize: '1.1rem', boxShadow: '0 4px 15px rgba(74, 144, 226, 0.4)' }}
          >
            🛒 Ir para o Carrinho e Finalizar
          </button>
        </div>
      )}
    </div>
  );
}

export default ProductPage;