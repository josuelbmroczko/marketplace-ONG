import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api.js'; 

function CheckoutPage() {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [nome, setNome] = useState('');
  const [endereco, setEndereco] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    api.get('/cart') 
      .then(response => {
        if (!response.data || response.data.items.length === 0) {
          navigate('/cart');
        } else {
          setCart(response.data);
        }
        setLoading(false);
      })
      .catch(err => {
        console.error("Erro ao buscar carrinho:", err);
        setLoading(false);
        navigate('/cart');
      });
  }, [navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const params = new URLSearchParams();
      params.append('nome', nome);
      params.append('endereco', endereco);

      await api.post('/checkout/process', params);
      
      navigate('/order-success');
    } catch (err) {
      console.error("Erro ao processar checkout:", err);
      setError(err.response?.data?.message || "Erro ao finalizar a compra. Verifique o estoque.");
      if (err.response?.status === 400) {
        navigate('/cart?error=stock_final');
      }
    }
  };

  if (loading || !cart) {
    return <div className="container">Carregando...</div>;
  }

return (
    <div className="container">
      {/* O "card" principal não precisa mais de 'overflow: hidden',
        pois não estamos mais usando 'float'.
      */}
      <div className="card">
        <h1>Finalizar Compra</h1>
        {error && <div className="error-message">{error}</div>}

        {/* ESTA É A MUDANÇA PRINCIPAL:
          Um novo wrapper 'checkout-layout' usa Flexbox
          para organizar o formulário e o resumo.
        */}
        <div className="checkout-layout">
          
          {/* Coluna do Formulário */}
          <div className="checkout-form">
            <h3>Seus Dados</h3>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="nome">Nome Completo:</label>
                <input 
                  type="text" 
                  id="nome" 
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  required 
                />
              </div>
              <div className="form-group">
                <label htmlFor="endereco">Endereço de Entrega:</label>
                <input 
                  type="text" 
                  id="endereco" 
                  value={endereco}
                  onChange={(e) => setEndereco(e.target.value)}
                  required 
                />
              </div>
              <button type="submit" className="btn btn-success">Comprar Agora</button>
            </form>
          </div>

          {/* Coluna do Resumo do Pedido */}
          <div className="checkout-summary">
            <h3>Resumo do Pedido</h3>
            <ul>
              {cart.items.map(item => (
                <li key={item.product.id}>
                  <span>{item.product.productName} (x{item.quantity})</span>
                  <span>R$ {item.subtotal.toFixed(2)}</span>
                </li>
              ))}
            </ul>
            <hr />
            <h4 style={{textAlign: 'right'}}>
              Total: <span>R$ {cart.totalPrice.toFixed(2)}</span>
            </h4>
          </div>

        </div> {/* Fim de .checkout-layout */}
      </div> {/* Fim de .card */}
    </div> /* Fim de .container */
  );
}

export default CheckoutPage;