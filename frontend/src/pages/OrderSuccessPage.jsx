import React from 'react';
import { Link } from 'react-router-dom';

function OrderSuccessPage() {
  return (
    <div className="container">
      <div className="card" style={{textAlign: 'center'}}>
        <h1 style={{color: '#28a745'}}>Compra Realizada com Sucesso!</h1>
        <p>Recebemos o seu pedido. Obrigado por apoiar esta causa!</p>
        <p>(Isto é apenas uma mensagem de confirmação, o estoque foi atualizado.)</p>
        <Link to="/" className="btn btn-primary">Voltar aos Produtos</Link>
      </div>
    </div>
  );
}

export default OrderSuccessPage;