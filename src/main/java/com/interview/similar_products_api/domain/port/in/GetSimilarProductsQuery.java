package com.interview.similar_products_api.domain.port.in;

import com.interview.similar_products_api.domain.model.ProductDetail;

import java.util.List;

public interface GetSimilarProductsQuery {
    List<ProductDetail> getSimilarProducts(String productId);
}
