package com.airgrab.airgrab_agent.model;

import java.util.List;

public record CartResponse(
    String restaurantName,
    String dishName,
    int price,
    List<String> matchingTags,
    String reasonForMatch
) {}