package com.marketplace.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos extras que a IA possa mandar
public class AiSearchResponse {

    private String friendlyMessage;
    private SearchFilters filters;

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

    public AiSearchResponse(String friendlyMessage, SearchFilters filters) {
        this.friendlyMessage = friendlyMessage;
        this.filters = filters;
    }
}