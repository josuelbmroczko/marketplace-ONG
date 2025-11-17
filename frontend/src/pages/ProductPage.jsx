import React, { useState, useEffect, useCallback, useMemo } from 'react';
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
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);
    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);
  return debouncedValue;
}

function ProductPage() {
  const [produtos, setProdutos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user } = useAuth();
  console.log("VERIFICANDO ROLE DO USUÁRIO:", user);
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
    name: '',
    minPrice: '',
    maxPrice: '',
    category: '',
    sort: '',
  });
  const [cartQuantities, setCartQuantities] = useState({});

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
          
          const aiFilters = response.data.filters;
          if (aiFilters) {
            setFilters({
              name: aiFilters.name || '',
              minPrice: aiFilters.minPrice || '',
              maxPrice: aiFilters.maxPrice || '',
              category: aiFilters.category || '',
              sort: aiFilters.sort || ''
            });
          }
          setLoading(false);
        })
        .catch(error => {
          console.error("Erro ao buscar produtos (IA):", error);
          setError("Erro ao carregar produtos.");
          setLoading(false);
          setProdutos([]);
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
          console.error("Erro ao buscar produtos (Manual):", error);
          setError("Erro ao carregar produtos.");
          setLoading(false);
          setProdutos([]);
        });
    }
  }, [
    debouncedNameFilter, 
    filters.minPrice, 
    filters.maxPrice, 
    filters.category, 
    filters.sort,
    debouncedAiQuery
  ]);


  useEffect(() => {
    if (user && user.role === 'ROLE_ADMIN') {
      api.get('/org')
        .then(response => {
          setOrganizations(Array.isArray(response.data) ? response.data : []);
        })
        .catch(err => console.error("Erro ao buscar organizações:", err));
    }
  }, [user]);

  const handleAddToCart = (productId, maxQuantity) => {
    const quantityToAdd = cartQuantities[productId] || 1;

    if (quantityToAdd > maxQuantity) {
      alert('Erro: A quantidade pedida é maior que o estoque disponível.');
      return;
    }
    if (quantityToAdd <= 0) {
      alert('Erro: A quantidade deve ser pelo menos 1.');
      return;
    }

    api.post(`/cart/add/${productId}`, { quantity: quantityToAdd })
      .then(response => {
        alert('Produto adicionado ao carrinho!');
        setProdutos(prevProdutos =>
          prevProdutos.map(p =>
            p.id === productId
              ? { ...p, quantity: p.quantity - quantityToAdd }
              : p
          )
        );
      })
      .catch(error => {
        if (error.response && error.response.status === 401) {
          alert('Você precisa estar logado para adicionar ao carrinho.');
          navigate('/login');
        } else {
          alert('Erro ao adicionar: ' + (error.response?.data?.message || 'Erro de estoque'));
        }
      });
  };

  const resetForm = () => {
    setProductName('');
    setPrice('');
    setQuantity(0);
    setCategory('');
    setImageUrl('');
    setDescription('');
    setAmout('');
    setAdminSelectedOrg('');
    setEditingProduct(null);
    setError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (user.role === 'ROLE_ADMIN' && !adminSelectedOrg && !editingProduct) {
      alert("Admin, por favor selecione a ONG proprietária.");
      return;
    }

    const productData = {
      productName,
      price: parseFloat(price),
      quantity: parseInt(quantity, 10),
      category,
      imageUrl,
      description,
      amout,
      organization: user.role === 'ROLE_ADMIN' ? { id: adminSelectedOrg } : null
    };

    try {
      if (editingProduct) {
        const response = await api.put(`/product/${editingProduct.id}`, productData);
        alert('Produto atualizado com sucesso!');
        setProdutos(prev => prev.map(p => p.id === editingProduct.id ? response.data : p));
      } else {
        const response = await api.post('/product', productData);
        alert('Produto criado com sucesso!');
        setProdutos(prev => [response.data, ...prev]);
      }

      resetForm();
    } catch (err) {
      console.error("Erro ao salvar produto:", err);
      let errorMsg = err.response?.data?.message || 'Erro desconhecido';
      alert('Erro ao salvar produto: ' + errorMsg);
      setError('Erro ao salvar produto: ' + errorMsg);
    }
  };

  const handleEditClick = (product) => {
    setEditingProduct(product);
    setProductName(product.productName);
    setPrice(product.price);
    setQuantity(product.quantity);
    setCategory(product.category);
    setImageUrl(product.imageUrl || '');
    setDescription(product.description || '');
    setAmout(product.amout || '');

    if (user.role === 'ROLE_ADMIN') {
      setAdminSelectedOrg(product.organization ? product.organization.id : '');
    }

    window.scrollTo(0, 0);
  };

  const handleDeleteProduct = async (productId) => {
    if (window.confirm('Tem certeza que deseja deletar este produto?')) {
      try {
        await api.delete(`/product/${productId}`);
        alert('Produto deletado com sucesso.');
        setProdutos(prev => prev.filter(p => p.id !== productId));
      } catch (err) {
        alert('Erro ao deletar produto.');
      }
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const handleAiQueryChange = (e) => {
    setAiQuery(e.target.value);

    setFilters({ name: '', minPrice: '', maxPrice: '', category: '', sort: '' });
  };

  const handleManualFilterChange = (e) => {
    setAiQuery('');
    handleFilterChange(e);
  };

  const handleCartQtyChange = (productId, value) => {
    const qty = parseInt(value, 10);
    setCartQuantities(prev => ({ ...prev, [productId]: isNaN(qty) ? 1 : qty }));
  };

  const clearFilters = () => {
    setAiQuery('');
    setFilters({ name: '', minPrice: '', maxPrice: '', category: '', sort: '' });
  };

  const getColumnCount = () => {
    let visibleColumns = ['Foto', 'Nome', 'Preço', 'Estoque'];
    if (user && user.role === 'ROLE_USUARIO') {
      visibleColumns.push('ONG', 'Comprar');
    } else if (user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_GERENTE')) {
      visibleColumns.push('Ações');
    } else if (!user) {
      visibleColumns.push('ONG');
    }
    return visibleColumns.length;
  };

  const filteredAndSortedProducts = useMemo(() => {
    return produtos;
  }, [produtos]);


  return (
    <div className="product-page-container">

      <style>{`
        .buy-action { display: flex; align-items: center; justify-content: space-around; }
        .quantity-input { width: 50px; margin-right: 5px; text-align: center; }
        .product-image { max-width: 60px; height: auto; border-radius: 4px; }
      `}</style>

      {error && <div className="error-message">{error}</div>}

      {user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_GERENTE') && (
        <div className="card">
          <h2>{editingProduct ? 'Editar Produto' : 'Adicionar Novo Produto'}</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="productName">Nome do Produto:</label>
              <input type="text" id="productName" value={productName} onChange={(e) => setProductName(e.target.value)} required />
            </div>
            <div className="form-group">
              <label htmlFor="price">Preço (R$):</label>
              <input type="number" id="price" value={price} onChange={(e) => setPrice(e.target.value)} step="0.01" min="0" required />
            </div>
            <div className="form-group">
              <label htmlFor="quantity">Quantidade em Estoque:</label>
              <input type="number" id="quantity" value={quantity} onChange={(e) => setQuantity(e.target.value)} min="0" required />
            </div>
            <div className="form-group">
              <label htmlFor="imageUrl">URL da Imagem:</label>
              <input type="text" id="imageUrl" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} placeholder="https://exemplo.com/imagem.png" />
            </div>
            <div className="form-group">
              <label htmlFor="category-form">Categoria:</label>
              <select id="category-form" value={category} onChange={(e) => setCategory(e.target.value)} required>
                <option value="">-- Selecione uma Categoria --</option>
                {allCategories.map(cat => (<option key={cat.value} value={cat.value}>{cat.name}</option>))}
              </select>
            </div>
            {user.role === 'ROLE_ADMIN' && (
              <div className="form-group">
                <label htmlFor="organization">ONG Proprietária:</label>
                <select id="organization" value={adminSelectedOrg} onChange={(e) => setAdminSelectedOrg(e.target.value)} required={!editingProduct}>
                  <option value="">-- Selecione a ONG --</option>
                  {organizations.map(org => (<option key={org.id} value={org.id}>{org.name}</option>))}
                </select>
              </div>
            )}
            <div className="form-group">
              <label htmlFor="description">Descrição:</label>
              <textarea id="description" value={description} onChange={(e) => setDescription(e.target.value)} rows="3"></textarea>
            </div>
            <div className="form-group">
              <label htmlFor="amout">Amout (Ex: 1kg, 100ml):</label>
              <input type="text" id="amout" value={amout} onChange={(e) => setAmout(e.target.value)} placeholder="Ex: 1kg, 100ml, 1 unidade" />
            </div>
            <button type="submit" className="btn btn-success">
              {editingProduct ? 'Atualizar Produto' : 'Salvar Produto'}
            </button>
            {editingProduct && (
              <button type="button" onClick={resetForm} className="btn btn-secondary" style={{ marginLeft: '10px' }}>
                Cancelar Edição
              </button>
            )}
          </form>
        </div>
      )}

      {user && user.role === 'ROLE_USUARIO' && (
        <div className="card">
          <h2>Busca Inteligente (IA)</h2>
          <input
            type="text"
            value={aiQuery}
            onChange={handleAiQueryChange}
            placeholder="Ex: ração para filhotes castrados..."
            style={{ width: '100%', padding: '8px' }}
          />
        </div>
      )}
      {aiMessage && (<div className="ai-message"><strong>{aiMessage}</strong></div>)}

      <div className="card">
        <h2>Nossos Produtos</h2>
        <form className="filter-form" onSubmit={(e) => e.preventDefault()}>
          <input type="text" name="name" value={filters.name} onChange={handleManualFilterChange} placeholder="Nome do produto..." />
          <input type="number" name="minPrice" value={filters.minPrice} onChange={handleManualFilterChange} placeholder="Preço min" />
          <input type="number" name="maxPrice" value={filters.maxPrice} onChange={handleManualFilterChange} placeholder="Preço max" />
          <select name="category" value={filters.category} onChange={handleManualFilterChange}>
            <option value="">Todas as Categorias</option>
            {allCategories.map(cat => (<option key={cat.value} value={cat.value}>{cat.name}</option>))}
          </select>
          <select name="sort" value={filters.sort} onChange={handleManualFilterChange}>
            <option value="">Ordenar por</option>
            <option value="price_asc">Preço (Menor)</option>
            <option value="price_desc">Preço (Maior)</option>
            <option value="name_asc">Nome (A-Z)</option>
          </select>
          <button type="button" onClick={clearFilters}>Limpar Filtros</button>
        </form>
      </div>

      <div className="card">
        <div className="table-container">
          <table className="product-table">
            <thead>
              <tr>
                <th>Foto</th>
                <th>Nome</th>
                <th>Preço</th>
                <th>Estoque</th>
                {(user && user.role === 'ROLE_USUARIO') || !user ? <th>ONG</th> : null}
                {user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_GERENTE') ? <th>Ações</th> : null}
                {user && user.role === 'ROLE_USUARIO' ? <th>Comprar</th> : null}
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={getColumnCount()} className="loading">Carregando produtos...</td></tr>

              ) : filteredAndSortedProducts.length === 0 ? (
                <tr><td colSpan={getColumnCount()} className="empty-state">Nenhum produto encontrado.</td></tr>
              ) : (
                filteredAndSortedProducts.map(prod => (
                  <tr key={prod.id}>
                    <td>
                      {prod.imageUrl ? (
                        <img src={prod.imageUrl} alt={prod.productName} className="product-image" onError={(e) => { e.target.src = 'https://via.placeholder.com/60'; }} />
                      ) : (
                        <img src="https://via.placeholder.com/60" alt="Sem foto" className="product-image" />
                      )}
                    </td>
                    <td>
                      <strong>{prod.productName}</strong>
                      {prod.description && <p style={{ fontSize: '12px', margin: 0 }}>{prod.description}</p>}
                      {prod.amout && <small style={{ color: '#6c757d' }}>({prod.amout})</small>}
                    </td>
                    <td>
                      <strong style={{ color: '#28a745' }}>R$ {prod.price ? prod.price.toFixed(2) : '0.00'}</strong>
                    </td>
                    <td>
                      {prod.quantity > 0 ? (
                        <span style={{ color: 'green' }}>{prod.quantity} em estoque</span>
                      ) : (
                        <span style={{ color: 'red' }}>Esgotado</span>
                      )}
                    </td>
                    {(user && user.role === 'ROLE_USUARIO') || !user ? (
                      <td>
                        <span style={{ fontSize: '13px', color: '#495057' }}>
                          {prod.organization ? prod.organization.name : 'N/A'}
                        </span>
                      </td>
                    ) : null}
                    {user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_GERENTE') ? (
                      <td>
                        <div className="actions">
                          <button onClick={() => handleEditClick(prod)} className="btn btn-warning">Editar</button>
                          <button onClick={() => handleDeleteProduct(prod.id)} className="btn btn-danger">Deletar</button>
                        </div>

                      </td>
                    ) : null}
                    {user && user.role === 'ROLE_USUARIO' ? (
                      <td>
                        <div className="buy-action">
                          <input
                            type="number"
                            min="1"
                            max={prod.quantity}
                            value={cartQuantities[prod.id] || 1}
                            onChange={(e) => handleCartQtyChange(prod.id, e.target.value)}
                            className="quantity-input"
                            disabled={prod.quantity === 0}
                          />
                          <button
                            onClick={() => handleAddToCart(prod.id, prod.quantity)}
                            className="btn btn-success btn-sm"
                            disabled={prod.quantity === 0}
                          >
                            {prod.quantity > 0 ? 'Comprar' : 'Esgotado'}
                          </button>
                        </div>
                      </td>
                    ) : null}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default ProductPage;