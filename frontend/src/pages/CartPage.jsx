import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api.js';

function CartPage() {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const fetchCart = () => {
    setLoading(true);
    api.get('/cart')
      .then(response => {
        setCart(response.data);
        setLoading(false);
      })
      .catch(error => {
        console.error("Erro ao buscar carrinho:", error);
        setLoading(false);
      });
  };

  useEffect(fetchCart, []);

  const handleRemove = (productId) => {
    if (!window.confirm("Remover este item do carrinho?")) return;

    api.delete(`/cart/remove/${productId}`)
      .then(response => {
        setCart(response.data);
      })
      .catch(error => alert('Erro ao remover item'));
  };

  if (loading) return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: '50px', color: '#7f8c8d', fontFamily: 'Segoe UI' }}>
      Carregando carrinho...
    </div>
  );

  return (
    <div className="cart-container">
      <style>{`
        :root {
          --primary: #4a90e2;
          --secondary: #f5f7fa;
          --text-dark: #2c3e50;
          --text-light: #7f8c8d;
          --success: #27ae60;
          --danger: #e74c3c;
          --white: #ffffff;
          --border-radius: 12px;
          --shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
        }

        .cart-container {
          padding: 20px;
          max-width: 1200px;
          margin: 0 auto;
          font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
          color: var(--text-dark);
        }

        h1 { margin-bottom: 25px; font-size: 1.8rem; color: var(--text-dark); }

        /* Layout Grid: Esquerda (Itens) e Direita (Resumo) */
        .cart-layout {
          display: grid;
          grid-template-columns: 1fr 320px;
          gap: 25px;
        }

        /* Card Genérico */
        .card {
          background: var(--white);
          border-radius: var(--border-radius);
          box-shadow: var(--shadow);
          padding: 20px;
          border: 1px solid #eee;
        }

        /* Tabela Desktop */
        .cart-table {
          width: 100%;
          border-collapse: collapse;
        }
        .cart-table th {
          text-align: left;
          padding: 15px;
          color: var(--text-light);
          font-weight: 600;
          border-bottom: 2px solid #f0f0f0;
        }
        .cart-table td {
          padding: 15px;
          border-bottom: 1px solid #f0f0f0;
          vertical-align: middle;
        }
        .product-info { display: flex; align-items: center; gap: 15px; }
        .product-thumb {
          width: 60px; height: 60px; object-fit: cover;
          border-radius: 8px; border: 1px solid #eee;
        }
        .product-name { font-weight: 600; color: var(--text-dark); display: block; }
        .product-cat { font-size: 0.85rem; color: var(--text-light); }

        /* Resumo do Pedido */
        .summary-row {
          display: flex; justify-content: space-between;
          margin-bottom: 15px; font-size: 0.95rem; color: var(--text-light);
        }
        .summary-total {
          display: flex; justify-content: space-between;
          margin-top: 20px; padding-top: 20px;
          border-top: 2px solid #f0f0f0;
          font-size: 1.4rem; font-weight: bold; color: var(--text-dark);
        }
        .price-highlight { color: var(--success); }

        /* Botões */
        .btn {
          padding: 12px 20px; border: none; border-radius: 8px;
          cursor: pointer; font-weight: 600; transition: opacity 0.2s;
          text-decoration: none; display: inline-block; text-align: center;
        }
        .btn:hover { opacity: 0.9; }
        .btn-primary { background: var(--primary); color: white; width: 100%; }
        .btn-danger-outline {
          background: transparent; border: 1px solid #fee2e2; color: var(--danger);
          padding: 6px 12px; font-size: 0.85rem;
        }
        .btn-danger-outline:hover { background: #fee2e2; }
        .btn-secondary { background: #cbd5e0; color: #2d3748; }
        .link-back {
          display: block; text-align: center; margin-top: 15px;
          color: var(--primary); text-decoration: none; font-size: 0.9rem;
        }
        .link-back:hover { text-decoration: underline; }

        /* Mobile & Responsividade */
        .mobile-items { display: none; }

        @media (max-width: 900px) {
          .cart-layout { grid-template-columns: 1fr; }
        }

        @media (max-width: 600px) {
          .cart-table { display: none; } /* Esconde tabela */
          .mobile-items { display: flex; flex-direction: column; gap: 15px; }
          
          .mobile-card {
            display: flex; gap: 15px; padding: 15px;
            background: white; border-radius: var(--border-radius);
            box-shadow: var(--shadow); border: 1px solid #eee;
          }
          .mobile-thumb { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; }
          .mobile-details { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
          .mobile-top { display: flex; justify-content: space-between; align-items: start; }
          .mobile-price { color: var(--success); font-weight: bold; }
        }

        .empty-state {
          text-align: center; padding: 60px 20px;
          background: white; border-radius: var(--border-radius); box-shadow: var(--shadow);
        }
      `}</style>

      <h1>🛒 Meu Carrinho</h1>

      {!cart || cart.items.length === 0 ? (
        <div className="empty-state">
          <div style={{ fontSize: '3rem', marginBottom: '10px' }}>🛍️</div>
          <h2 style={{ margin: '0 0 10px 0', color: '#2c3e50' }}>Seu carrinho está vazio</h2>
          <p style={{ color: '#7f8c8d', marginBottom: '30px' }}>Parece que você ainda não escolheu seus produtos.</p>
          <Link to="/" className="btn btn-primary" style={{ maxWidth: '250px' }}>
            Voltar para a Loja
          </Link>
        </div>
      ) : (
        <div className="cart-layout">

          <div className="cart-items-section">
            <div className="card" style={{ padding: 0, overflow: 'hidden' }}>

              <table className="cart-table">
                <thead>
                  <tr>
                    <th style={{ paddingLeft: '20px' }}>Produto</th>
                    <th>Preço Unit.</th>
                    <th>Qtd.</th>
                    <th>Subtotal</th>
                    <th>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {cart.items.map(item => (
                    <tr key={item.product.id}>
                      <td style={{ paddingLeft: '20px' }}>
                        <div className="product-info">
                          <img
                            src={item.product.imageUrl || 'https://via.placeholder.com/60'}
                            alt={item.product.productName}
                            className="product-thumb"
                            onError={(e) => { e.target.src = 'https://via.placeholder.com/60'; }}
                          />
                          <div>
                            <span className="product-name">{item.product.productName}</span>
                            <span className="product-cat">{item.product.category}</span>
                          </div>
                        </div>
                      </td>
                      <td>R$ {item.product.price.toFixed(2)}</td>
                      <td>{item.quantity}</td>
                      <td style={{ fontWeight: 'bold', color: 'var(--text-dark)' }}>R$ {item.subtotal.toFixed(2)}</td>
                      <td>
                        <button
                          onClick={() => handleRemove(item.product.id)}
                          className="btn btn-danger-outline"
                          title="Remover item"
                        >
                          🗑️
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              <div className="mobile-items">
                {cart.items.map(item => (
                  <div key={item.product.id} className="mobile-card">
                    <img
                      src={item.product.imageUrl || 'https://via.placeholder.com/80'}
                      alt={item.product.productName}
                      className="mobile-thumb"
                      onError={(e) => { e.target.src = 'https://via.placeholder.com/80'; }}
                    />
                    <div className="mobile-details">
                      <div className="mobile-top">
                        <span className="product-name" style={{ fontSize: '0.95rem' }}>{item.product.productName}</span>
                        <button
                          onClick={() => handleRemove(item.product.id)}
                          style={{ background: 'none', border: 'none', color: '#e74c3c', fontSize: '1.2rem', padding: 0 }}
                        >
                          &times;
                        </button>
                      </div>
                      <div style={{ fontSize: '0.85rem', color: '#7f8c8d' }}>Qtd: {item.quantity}</div>
                      <div className="mobile-price">R$ {item.subtotal.toFixed(2)}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="cart-summary-section">
            <div className="card" style={{ position: 'sticky', top: '20px' }}>
              <h3 style={{ marginTop: 0, marginBottom: '20px' }}>Resumo do Pedido</h3>

              <div className="summary-row">
                <span>Subtotal ({cart.items.length} itens)</span>
                <span>R$ {cart.totalPrice.toFixed(2)}</span>
              </div>

              {/* Você pode adicionar frete ou desconto aqui futuramente */}
              {/* <div className="summary-row">
                <span>Frete</span>
                <span>Grátis</span>
              </div> 
              */}

              <div className="summary-total">
                <span>Total</span>
                <span className="price-highlight">R$ {cart.totalPrice.toFixed(2)}</span>
              </div>

              <div style={{ marginTop: '20px' }}>
                <button onClick={() => navigate('/checkout')} className="btn btn-primary">
                  Finalizar Compra
                </button>

                <Link to="/" className="link-back">
                  ← Continuar comprando
                </Link>
              </div>
            </div>
          </div>

        </div>
      )}
    </div>
  );
}

export default CartPage;