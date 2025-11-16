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
      <div className="card">
        <h1>Meu Carrinho</h1>

        {!cart || cart.items.length === 0 ? (
          <div>
            <p>Seu carrinho está vazio.</p>
            <Link to="/" className="btn btn-primary" style={{backgroundColor: '#6c757d'}}>
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
                        style={{fontSize: '0.9em', padding: '5px 10px'}}
                      >
                        Remover
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div style={{textAlign: 'right', fontSize: '1.5rem', fontWeight: 'bold', marginTop: '2rem'}}>
              Total: <span>R$ {cart.totalPrice.toFixed(2)}</span>
            </div>

            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem'}}>
              <Link to="/" style={{marginTop: '1.5rem'}}>Continuar comprando</Link>
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