package com.interview.similar_products_api.domain.model;

import java.math.BigDecimal;

public record ProductDetail(
        String id,
        String name,
        BigDecimal price,
        boolean availability) {
}
