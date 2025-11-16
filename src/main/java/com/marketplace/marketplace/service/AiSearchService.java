package com.marketplace.marketplace.service;

import com.marketplace.marketplace.dto.AiSearchResponse; // 1. Mude o import
import com.marketplace.marketplace.dto.SearchFilters;

public interface AiSearchService {

    AiSearchResponse parseSearchQuery(String query) throws Exception;
}