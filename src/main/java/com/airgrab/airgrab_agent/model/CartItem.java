package com.airgrab.airgrab_agent.model;

import java.util.List;

public record CartItem(
    String restaurantName,
    String dishName,
    int price,
    int quantity, // The AI will now populate this field!
    List<String> matchingTags,
    String reasonForMatch
) {}