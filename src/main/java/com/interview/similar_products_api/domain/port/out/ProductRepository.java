package com.interview.similar_products_api.domain.port.out;

import com.interview.similar_products_api.domain.model.ProductDetail;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<String> getSimilarProductIds(String productId);

    Optional<ProductDetail> getProductDetail(String productId);
}
