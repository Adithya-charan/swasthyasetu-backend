package com.swasthyasetu.webrtc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.List;

@Service
public class GeminiService {

    @Value("${app.gemini.api-key:YOUR_GEMINI_API_KEY}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateSummary(String complaint, String diagnosis, String meds, String notes, String language) {
        String prompt = String.format(
            "Generate a patient-friendly consultation summary in %s. " +
            "Doctor's findings: Chief Complaint: %s, Diagnosis: %s, Medicines: %s, Notes: %s. " +
            "The summary must include: " +
            "1. Simple explanation of the diagnosis. " +
            "2. Medicine usage instructions. " +
            "3. Important precautions. " +
            "4. Follow-up guidance. " +
            "Use clear headings and bullet points.",
            language, complaint, diagnosis, meds, notes
        );

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(GEMINI_API_URL + apiKey, entity, Map.class);
            
            if (response != null && response.containsKey("candidates")) {
                Object candidatesObj = response.get("candidates");
                if (candidatesObj instanceof List) {
                    List<?> candidates = (List<?>) candidatesObj;
                    if (!candidates.isEmpty()) {
                        Object firstCandidate = candidates.get(0);
                        if (firstCandidate instanceof Map) {
                            Map<?, ?> contentMap = (Map<?, ?>) ((Map<?, ?>) firstCandidate).get("content");
                            if (contentMap != null) {
                                Object partsObj = contentMap.get("parts");
                                if (partsObj instanceof List) {
                                    List<?> parts = (List<?>) partsObj;
                                    if (!parts.isEmpty() && parts.get(0) instanceof Map) {
                                        return (String) ((Map<?, ?>) parts.get(0)).get("text");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return "Summary generation failed or returned empty.";
        } catch (Exception e) {
            return String.format("Summary (Fallback):\nDiagnosis: %s\nTreatment: %s\nAdvice: %s\n\n(AI service unavailable: %s)", 
                                diagnosis, meds, notes, e.getMessage());
        }
    }
}
