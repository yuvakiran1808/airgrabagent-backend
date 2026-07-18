package com.airgrab.airgrab_agent.model;

import java.util.List;

public record CartResponse(
    List<CartItem> items,
    String orderSummary
) {}