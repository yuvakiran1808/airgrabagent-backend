package com.airgrab.airgrab_agent.controllers;

import com.airgrab.airgrab_agent.agent.OrderAgent;
import com.airgrab.airgrab_agent.model.CartResponse;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AgentCommerceController {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final OrderAgent orderAgent;
    private final String menuData;

    public AgentCommerceController(@Value("${openai.api.key}") String apiKey) {
        // 1. Initialize the LLM (gpt-4o-mini is fast and cheap for this)
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .responseFormat("json_object")
                .build();

        // 2. Wire the LLM to our OrderAgent interface
        this.orderAgent = AiServices.builder(OrderAgent.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        // 3. Load the JSON database into memory on startup
        this.menuData = loadMenuData();
    }

    @PostMapping("/process-voice")
    public ResponseEntity<?> processVoiceRequest(@RequestParam("file") MultipartFile file) {
        try {
            // STEP 1: Transcribe Audio using Whisper API
            String transcript = transcribeAudio(file);
            System.out.println("Audio Transcript: " + transcript);

            // STEP 2: Let the AI Agent find the perfect meal
            CartResponse cart = orderAgent.processOrder(transcript, menuData);

            // STEP 3: Return the structured cart to the user
            return ResponseEntity.ok(cart);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Helper Methods ---

    private String transcribeAudio(MultipartFile file) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(openAiApiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // Wrap the file so RestTemplate can read its filename and bytes
        Resource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() { return file.getOriginalFilename(); }
        };
        
        body.add("file", resource);
        body.add("model", "whisper-1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/audio/transcriptions", 
                requestEntity, 
                Map.class
        );

        return response.getBody().get("text").toString();
    }

    private String loadMenuData() {
        try {
            InputStream is = getClass().getResourceAsStream("/static/restaurantdata.json");
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load /static/restaurantdata.json", e);
        }
    }
}