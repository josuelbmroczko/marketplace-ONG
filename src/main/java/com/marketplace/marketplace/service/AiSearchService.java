package com.marketplace.marketplace.service;

import com.marketplace.marketplace.dto.AiSearchResponse; // 1. Mude o import
import com.marketplace.marketplace.dto.SearchFilters;

public interface AiSearchService {

    /**
     * Converte uma consulta de texto em filtros E uma mensagem amigável.
     * @param query O texto do usuário.
     * @return Um objeto AiSearchResponse (com a mensagem e os filtros).
     * @throws Exception Se a API da IA falhar.
     */
    AiSearchResponse parseSearchQuery(String query) throws Exception; // 2. Mude o tipo de retorno
}