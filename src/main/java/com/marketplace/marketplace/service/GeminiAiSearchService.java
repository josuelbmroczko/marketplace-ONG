package com.marketplace.marketplace.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.marketplace.dto.AiSearchResponse; // Importe o novo DTO
import com.marketplace.marketplace.dto.SearchFilters;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GeminiAiSearchService implements AiSearchService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiSearchService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public GeminiAiSearchService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiSearchResponse parseSearchQuery(String query) throws Exception { // 1. Mude o tipo de retorno
        log.info("Enviando consulta para a API do Gemini: '{}'", query);

        String prompt = buildPrompt(query);
        GeminiRequest requestBody = new GeminiRequest(List.of(new Content(List.of(new Part(prompt)))));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestBody, headers);
        String url = apiUrl + "?key=" + apiKey;

        GeminiResponse response = restTemplate.postForObject(url, entity, GeminiResponse.class);

        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new RuntimeException("Resposta inválida da API do Gemini.");
        }

        String jsonText = response.getCandidates().get(0).getContent().getParts().get(0).getText();
        jsonText = jsonText.replace("```json", "").replace("```", "").trim();

        log.info("JSON completo (com mensagem) recebido do Gemini: {}", jsonText);

        // 2. Converta o JSON no novo DTO AiSearchResponse
        return objectMapper.readValue(jsonText, AiSearchResponse.class);
    }

    private String buildPrompt(String query) {
        // 3. ATUALIZE O PROMPT
        return "Você é um assistente de busca para um e-commerce de ONGs, simulando um vendedor amigável. " +
                "Converta a seguinte consulta de usuário em um JSON de filtros E crie uma 'friendlyMessage' confirmando a busca. " +
                "A consulta é: '" + query + "'.\n\n" +
                "O formato JSON obrigatório é: \n" +
                "{\n" +
                "  \"friendlyMessage\": \"Uma frase amigável, ex: 'Claro! Buscando X para você.'\",\n" +
                "  \"filters\": {\"name\": \"string_ou_null\", \"category\": \"string_ou_null\", \"minPrice\": numero_ou_null, \"maxPrice\": numero_ou_null}\n" +
                "}\n\n" +
                "Exemplos:\n" +
                "Consulta: 'ração de cachorro barata'\n" +
                "JSON: {\"friendlyMessage\": \"Com certeza! Estou procurando as melhores rações para cães por menos de R$50.\", \"filters\": {\"name\": \"ração\", \"category\": \"alimento\", \"minPrice\": null, \"maxPrice\": 50.0}}\n\n" +
                "Consulta: 'coleiras acima de 100 reais'\n" +
                "JSON: {\"friendlyMessage\": \"Entendido! Vamos ver as coleiras mais estilosas acima de R$100!\", \"filters\": {\"name\": \"coleira\", \"category\": \"acessório\", \"minPrice\": 100.0, \"maxPrice\": null}}\n\n" +
                "Retorne APENAS o JSON.";
    }

    // --- DTOs Internos para a API do Gemini (sem alteração) ---
    @Data
    private static class GeminiRequest {
        private final List<Content> contents;
    }
    // ... (O resto dos DTOs internos: Content, Part, GeminiResponse, Candidate)
    @Data
    private static class Content {
        private final List<Part> parts;
    }
    @Data
    private static class Part {
        private final String text;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiResponse {
        private List<Candidate> candidates;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Candidate {
        private Content content;
    }
}