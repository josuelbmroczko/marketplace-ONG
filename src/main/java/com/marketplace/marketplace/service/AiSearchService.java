package com.marketplace.marketplace.service;

import com.marketplace.marketplace.dto.AiSearchResponse;


public interface AiSearchService {

    AiSearchResponse parseSearchQuery(String query) throws Exception;
}