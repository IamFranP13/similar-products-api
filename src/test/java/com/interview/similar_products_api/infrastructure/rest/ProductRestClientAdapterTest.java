package com.interview.similar_products_api.infrastructure.rest;

import com.interview.similar_products_api.domain.exception.ExternalServiceException;
import com.interview.similar_products_api.domain.exception.ProductNotFoundException;
import com.interview.similar_products_api.domain.model.ProductDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
class ProductRestClientAdapterTest {

    @Mock
    private RestClient restClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ProductRestClientAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProductRestClientAdapter(restClient);
    }

    @SuppressWarnings("unchecked")
    private void mockGetRequest(String uriTemplate, String productId) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(uriTemplate, productId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // onStatus devuelve siempre el mismo responseSpec (lo llamas dos veces en el
        // adapter)
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    @Test
    void getSimilarProductIdsReturnsListOfIdsOnSuccess() {
        String productId = "1";
        List<String> expectedIds = List.of("2", "3", "4");

        mockGetRequest("/product/{productId}/similarids", productId);

        when(responseSpec.body(ArgumentMatchers.<ParameterizedTypeReference<List<String>>>any()))
                .thenReturn(expectedIds);

        List<String> result = adapter.getSimilarProductIds(productId);

        assertEquals(expectedIds, result);
    }

    @Test
    void getSimilarProductIdsPropagatesProductNotFoundException() {
        String productId = "unknown";

        mockGetRequest("/product/{productId}/similarids", productId);

        when(responseSpec.body(ArgumentMatchers.<ParameterizedTypeReference<List<String>>>any()))
                .thenThrow(new ProductNotFoundException(productId));

        assertThrows(ProductNotFoundException.class,
                () -> adapter.getSimilarProductIds(productId));
    }

    @Test
    void getSimilarProductIdsWrapsRestClientExceptionInExternalServiceException() {
        String productId = "1";

        mockGetRequest("/product/{productId}/similarids", productId);

        when(responseSpec.body(ArgumentMatchers.<ParameterizedTypeReference<List<String>>>any()))
                .thenThrow(new RestClientException("upstream error"));

        ExternalServiceException ex = assertThrows(
                ExternalServiceException.class,
                () -> adapter.getSimilarProductIds(productId));

        assertTrue(ex.getMessage().contains("similar ids for product " + productId));
    }

    @Test
    void getProductDetailReturnsOptionalWithProductOnSuccess() {
        String productId = "2";
        ProductDetail detail = new ProductDetail(
                productId,
                "Dress",
                BigDecimal.valueOf(19.99),
                true);

        mockGetRequest("/product/{productId}", productId);

        when(responseSpec.body(ProductDetail.class)).thenReturn(detail);

        Optional<ProductDetail> result = adapter.getProductDetail(productId);

        assertTrue(result.isPresent());
        assertEquals(detail, result.get());
    }

    @Test
    void getProductDetailReturnsEmptyOptionalWhenProductNotFound() {
        String productId = "5";

        mockGetRequest("/product/{productId}", productId);

        when(responseSpec.body(ProductDetail.class))
                .thenThrow(new ProductNotFoundException(productId));

        Optional<ProductDetail> result = adapter.getProductDetail(productId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getProductDetailWrapsRestClientExceptionInExternalServiceException() {
        String productId = "2";

        mockGetRequest("/product/{productId}", productId);

        when(responseSpec.body(ProductDetail.class))
                .thenThrow(new RestClientException("upstream error"));

        ExternalServiceException ex = assertThrows(
                ExternalServiceException.class,
                () -> adapter.getProductDetail(productId));

        assertTrue(ex.getMessage().contains("product detail for " + productId));
    }
}
