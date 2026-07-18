package com.airgrab.airgrab_agent.agent;

import com.airgrab.airgrab_agent.model.CartResponse;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface OrderAgent {
    
    @SystemMessage({
        "You are an intelligent food ordering agent for Airgrab.",
        "Your job is to match a user's food request to the perfect dishes from the provided menu database.",
        "Rules:",
        "1. Extract the user's intent (dietary constraints, mood, protein goals, location).",
        "2. Extract the exact quantity requested for each item (e.g., 'two biryanis' = quantity 2). If no quantity is specified, default to 1.",
        "3. Search the provided {{menu_json}} for the best matching dishes.",
        "4. If the user orders multiple different items, include ALL of them in the response.",
        "5. Return ONLY a structured JSON response matching the CartResponse schema.",
        "6. If no exact match is found, pick the closest healthy or relevant alternative."
    })
    @UserMessage("User Audio Transcript: {{transcript}} \n\n Menu Database: {{menu_json}}")
    CartResponse processOrder(@V("transcript") String transcript, @V("menu_json") String menuJson);
}