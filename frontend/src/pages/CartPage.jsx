import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api.js'; 

function CartPage() {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);

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
    api.delete(`/cart/remove/${productId}`) 
      .then(response => {
        setCart(response.data);
      })
      .catch(error => alert('Erro ao remover item'));
  };

  if (loading) return <div className="container">Carregando carrinho...</div>;
  
  return (
    <div className="container">
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
        
        .data-table th {
          background: #f8f9fa;
          font-weight: bold;
        }
        
        .btn {
          padding: 10px 15px;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          font-size: 14px;
          text-decoration: none;
          display: inline-block;
          text-align: center;
        }
        
        .btn-primary { 
          background: #007bff; 
          color: white; 
        }
        
        .btn-danger { 
          background: #dc3545; 
          color: white; 
          font-size: 0.9em; 
          padding: 5px 10px; 
        }
        
        .cart-summary {
          text-align: right;
          font-size: 1.5rem;
          font-weight: bold;
          margin-top: 2rem;
          padding: 15px 0;
        }
        
        .cart-actions {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 1rem;
          flex-wrap: wrap;
          gap: 15px;
        }
        
        .continue-shopping {
          margin-top: 1.5rem;
          color: #007bff;
          text-decoration: none;
        }
        
        /* Cards para mobile */
        .cart-items-mobile {
          display: none;
          flex-direction: column;
          gap: 15px;
          margin-bottom: 20px;
        }
        
        .cart-item-card {
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 15px;
          background: white;
        }
        
        .cart-item-header {
          display: flex;
          justify-content: between;
          align-items: flex-start;
          margin-bottom: 10px;
        }
        
        .cart-item-name {
          flex: 1;
          font-weight: bold;
          margin-right: 10px;
        }
        
        .cart-item-price {
          color: #28a745;
          font-weight: bold;
        }
        
        .cart-item-details {
          display: flex;
          justify-content: space-between;
          margin-bottom: 10px;
          font-size: 14px;
        }
        
        .cart-item-actions {
          text-align: right;
        }
        
        /* Responsividade */
        @media (max-width: 768px) {
          .data-table {
            display: none;
          }
          
          .cart-items-mobile {
            display: flex;
          }
          
          .cart-summary {
            text-align: center;
            font-size: 1.3rem;
          }
          
          .cart-actions {
            flex-direction: column;
            text-align: center;
          }
          
          .continue-shopping {
            margin-top: 0;
          }
          
          .btn {
            width: 100%;
            margin: 5px 0;
          }
        }
        
        @media (min-width: 769px) {
          .cart-items-mobile {
            display: none;
          }
        }
        
        .empty-cart {
          text-align: center;
          padding: 40px 20px;
        }
        
        .empty-cart p {
          margin-bottom: 20px;
          font-size: 1.1rem;
          color: #6c757d;
        }
      `}</style>

      <div className="card">
        <h1>Meu Carrinho</h1>

        {!cart || cart.items.length === 0 ? (
          <div className="empty-cart">
            <p>Seu carrinho está vazio.</p>
            <Link to="/" className="btn btn-primary" style={{backgroundColor: '#6c757d', maxWidth: '250px', margin: '0 auto'}}>
              Voltar aos Produtos
            </Link>
          </div>
        ) : (
          <div>
            <table className="data-table cart-table">
              <thead>
                <tr>
                  <th>Produto</th>
                  <th>Preço</th>
                  <th>Qtd.</th>
                  <th>Subtotal</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {cart.items.map(item => (
                  <tr key={item.product.id}>
                    <td>{item.product.productName}</td>
                    <td>R$ {item.product.price.toFixed(2)}</td>
                    <td>{item.quantity}</td>
                    <td>R$ {item.subtotal.toFixed(2)}</td>
                    <td>
                      <button 
                        onClick={() => handleRemove(item.product.id)}
                        className="btn btn-danger"
                      >
                        Remover
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="cart-items-mobile">
              {cart.items.map(item => (
                <div key={item.product.id} className="cart-item-card">
                  <div className="cart-item-header">
                    <div className="cart-item-name">{item.product.productName}</div>
                    <div className="cart-item-price">R$ {item.product.price.toFixed(2)}</div>
                  </div>
                  <div className="cart-item-details">
                    <span>Quantidade: {item.quantity}</span>
                    <span>Subtotal: R$ {item.subtotal.toFixed(2)}</span>
                  </div>
                  <div className="cart-item-actions">
                    <button 
                      onClick={() => handleRemove(item.product.id)}
                      className="btn btn-danger"
                    >
                      Remover
                    </button>
                  </div>
                </div>
              ))}
            </div>

            <div className="cart-summary">
              Total: <span>R$ {cart.totalPrice.toFixed(2)}</span>
            </div>

            <div className="cart-actions">
              <Link to="/" className="continue-shopping">Continuar comprando</Link>
              <Link to="/checkout" className="btn btn-primary">
                Finalizar Compra
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default CartPage;