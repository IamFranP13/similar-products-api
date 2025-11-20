package com.interview.similar_products_api.application.service;

import com.interview.similar_products_api.domain.model.ProductDetail;
import com.interview.similar_products_api.domain.port.in.GetSimilarProductsQuery;
import com.interview.similar_products_api.domain.port.out.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class GetSimilarProductsService implements GetSimilarProductsQuery {

    private final ProductRepository productRepository;
    private final ExecutorService executorService;

    public GetSimilarProductsService(ProductRepository productRepository,
            ExecutorService executorService) {
        this.productRepository = productRepository;
        this.executorService = executorService;
    }

    @Override
    public List<ProductDetail> getSimilarProducts(String productId) {
        List<String> similarIds = productRepository.getSimilarProductIds(productId);
        if (similarIds == null || similarIds.isEmpty()) {
            return List.of();
        }

        List<CompletableFuture<ProductDetail>> futures = similarIds.stream()
                .map(id -> CompletableFuture
                        .supplyAsync(() -> productRepository.getProductDetail(id).orElse(null), executorService)
                        .exceptionally(ex -> null))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
    }
}
