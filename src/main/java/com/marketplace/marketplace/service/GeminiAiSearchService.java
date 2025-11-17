package com.marketplace.marketplace.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.marketplace.dto.AiSearchResponse;
import com.marketplace.marketplace.dto.SearchFilters;
import jakarta.servlet.http.HttpServletRequest;
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
    public AiSearchResponse parseSearchQuery(String query) throws Exception {
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

        return objectMapper.readValue(jsonText, AiSearchResponse.class);
    }

    // -----------------------------------------------------------------
    // AQUI ESTÁ A MUDANÇA (O PROMPT MELHORADO)
    // -----------------------------------------------------------------
    private String buildPrompt(String query) {
        return "Você é um assistente de busca para um e-commerce de ONGs, simulando um vendedor BEM amigável e caloroso. " +
                "Converta a seguinte consulta de usuário em um JSON de filtros E crie uma 'friendlyMessage' confirmando a busca de forma humana e curta. " +
                "A consulta é: '" + query + "'.\n\n" +
                "REGRAS IMPORTANTES:\n" +
                "1. A 'friendlyMessage' É OBRIGATÓRIA. Seja criativo!\n" +
                "2. (SINÔNIMOS) TENTE adivinhar a categoria. 'Ração', 'Feijão', 'Comida' ou 'Alimentação' é 'ALIMENTO'. 'Coleira' ou 'Passeio' é 'ACESSORIO'. 'Remédio' ou 'Medicamento' é 'MEDICAMENTO'. 'Jogos' ou 'Bola' é 'BRINQUEDO'.\n" +
                "3. (EXTRAIR NOME) Se o usuário digitar o NOME de um produto (ex: 'ração', 'remédio', 'feijão', 'piolho'), COLOQUE ESSE NOME no filtro 'name'.\n" +
                "4. (PREÇO) Se o usuário pedir 'barato', use 'maxPrice: 50'. Se pedir 'caro', use 'minPrice: 100'.\n" +
                "5. (ORDENAÇÃO) Se o usuário pedir 'mais barato', use 'sort: \"price_asc\"'. Se pedir 'mais caro', use 'sort: \"price_desc\"'.\n" +
                "6. (ORTOGRAFIA) Corrija erros de ortografia. 'rmedio' deve ser 'remédio'. 'brincedo' deve ser 'brinquedo'.\n\n" +
                "O formato JSON obrigatório é: \n" +
                "{\n" +
                "  \"friendlyMessage\": \"Uma frase amigável, ex: 'Claro! Buscando X para você.'\",\n" +
                "  \"filters\": {\"name\": \"string_ou_null\", \"category\": \"string_ou_null\", \"minPrice\": numero_ou_null, \"maxPrice\": numero_ou_null, \"sort\": \"string_ou_null\"}\n" +
                "}\n\n" +
                "Exemplos:\n" +
                "Consulta: 'tem comida?'\n" +
                "JSON: {\"friendlyMessage\": \"Claro! Buscando comidas para você!\", \"filters\": {\"name\": null, \"category\": \"ALIMENTO\", \"minPrice\": null, \"maxPrice\": null, \"sort\": null}}\n\n" +
                "Consulta: 'presiso de um rmedio para piolho'\n" +
                "JSON: {\"friendlyMessage\": \"Certo! Buscando remédios para piolho.\", \"filters\": {\"name\": \"piolho\", \"category\": \"MEDICAMENTO\", \"minPrice\": null, \"maxPrice\": null, \"sort\": null}}\n\n" +
                "Consulta: 'qual o feijão mais barato?'\n" +
                "JSON: {\"friendlyMessage\": \"Opa! Buscando o feijão mais em conta!\", \"filters\": {\"name\": \"feijão\", \"category\": \"ALIMENTO\", \"minPrice\": null, \"maxPrice\": null, \"sort\": \"price_asc\"}}\n\n" +
                "Consulta: 'jogos'\n" +
                "JSON: {\"friendlyMessage\": \"Ok! Vamos ver os brinquedos e jogos!\", \"filters\": {\"name\": \"jogos\", \"category\": \"BRINQUEDO\", \"minPrice\": null, \"maxPrice\": null, \"sort\": null}}\n\n" +
                "Retorne APENAS o JSON.";
    }

    @Data
    private static class GeminiRequest {
        private final List<Content> contents;
        private GeminiRequest(List<Content> contents) {
            this.contents = contents;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Content {
        private List<Part> parts;

        public Content(List<Part> parts) { this.parts = parts; }
        public Content() {}

        public List<Part> getParts() { return parts; }
        public void setParts(List<Part> parts) { this.parts = parts; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Part {
        private String text;

        public Part(String text) { this.text = text; }
        public Part() {}

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiResponse {
        private List<Candidate> candidates;

        public List<Candidate> getCandidates() { return candidates; }
        public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Candidate {
        private Content content;

        public Content getContent() { return content; }
        public void setContent(Content content) { this.content = content; }
    }
}