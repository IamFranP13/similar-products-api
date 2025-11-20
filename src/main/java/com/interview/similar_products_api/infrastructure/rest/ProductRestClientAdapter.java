package com.interview.similar_products_api.infrastructure.rest;

import com.interview.similar_products_api.domain.exception.ExternalServiceException;
import com.interview.similar_products_api.domain.exception.ProductNotFoundException;
import com.interview.similar_products_api.domain.model.ProductDetail;
import com.interview.similar_products_api.domain.port.out.ProductRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

@Component
public class ProductRestClientAdapter implements ProductRepository {

    private final RestClient restClient;

    public ProductRestClientAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<String> getSimilarProductIds(String productId) {
        try {
            return restClient.get()
                    .uri("/product/{productId}/similarids", productId)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ProductNotFoundException(productId);
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new ExternalServiceException(
                                        "Error retrieving similar ids for product " + productId);
                            })
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Error calling similar ids for product " + productId, ex);
        }
    }

    @Override
    public Optional<ProductDetail> getProductDetail(String productId) {
        try {
            ProductDetail productDetail = restClient.get()
                    .uri("/product/{productId}", productId)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ProductNotFoundException(productId);
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new ExternalServiceException("Error retrieving product detail for " + productId);
                            })
                    .body(ProductDetail.class);

            return Optional.ofNullable(productDetail);
        } catch (ProductNotFoundException ex) {
            return Optional.empty();
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Error calling product detail for " + productId, ex);
        }
    }
}
