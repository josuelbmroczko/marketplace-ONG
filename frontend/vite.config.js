import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],

  // VERIFIQUE SE ESTA SEÇÃO EXISTE E ESTÁ CORRETA
  server: {
    proxy: {
      // Qualquer chamada que comece com /api
      '/api': {
        // Será encaminhada para o seu backend
        target: 'http://localhost:8080', 
        
        // Importante para evitar erros de CORS
        changeOrigin: true, 
      }
    }
  }
})