package com.marketplace.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiSearchResponse {

    private String friendlyMessage;
    private SearchFilters filters;

    // --- CONSTRUTOR ANTIGO (DEIXE ELE AQUI) ---
    public AiSearchResponse(String friendlyMessage, SearchFilters filters) {
        this.friendlyMessage = friendlyMessage;
        this.filters = filters;
    }

    // --- ADICIONE ESTE NOVO CONSTRUTOR VAZIO ---
    // O Jackson (leitor de JSON) precisa disto para funcionar
    public AiSearchResponse() {
    }
    // --- FIM DA CORREÇÃO ---


    public String getFriendlyMessage() {
        return friendlyMessage;
    }

    public void setFriendlyMessage(String friendlyMessage) {
        this.friendlyMessage = friendlyMessage;
    }

    public SearchFilters getFilters() {
        return filters;
    }

    public void setFilters(SearchFilters filters) {
        this.filters = filters;
    }
}