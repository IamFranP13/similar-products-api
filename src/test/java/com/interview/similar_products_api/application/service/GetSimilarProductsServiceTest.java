package com.interview.similar_products_api.application.service;

import com.interview.similar_products_api.domain.exception.ExternalServiceException;
import com.interview.similar_products_api.domain.model.ProductDetail;
import com.interview.similar_products_api.domain.port.out.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSimilarProductsServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ExecutorService executorService;

    private GetSimilarProductsService service;

    @BeforeEach
    void setUp() {
        executorService = Executors.newSingleThreadExecutor();
        service = new GetSimilarProductsService(productRepository, executorService);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void returnsEmptyListWhenSimilarIdsIsNull() {
        String productId = "1";
        when(productRepository.getSimilarProductIds(productId)).thenReturn(null);

        List<ProductDetail> result = service.getSimilarProducts(productId);

        assertTrue(result.isEmpty());
        verify(productRepository, never()).getProductDetail(anyString());
    }

    @Test
    void returnsEmptyListWhenSimilarIdsIsEmpty() {
        String productId = "1";
        when(productRepository.getSimilarProductIds(productId)).thenReturn(List.of());

        List<ProductDetail> result = service.getSimilarProducts(productId);

        assertTrue(result.isEmpty());
        verify(productRepository, never()).getProductDetail(anyString());
    }

    @Test
    void returnsAllProductDetailsWhenRepositoryReturnsData() {
        String productId = "1";
        List<String> similarIds = List.of("2", "3");

        ProductDetail product2 = new ProductDetail("2", "Product 2", BigDecimal.valueOf(10.0), true);
        ProductDetail product3 = new ProductDetail("3", "Product 3", BigDecimal.valueOf(20.0), false);

        when(productRepository.getSimilarProductIds(productId)).thenReturn(similarIds);
        when(productRepository.getProductDetail("2")).thenReturn(Optional.of(product2));
        when(productRepository.getProductDetail("3")).thenReturn(Optional.of(product3));

        List<ProductDetail> result = service.getSimilarProducts(productId);

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).id());
        assertEquals("3", result.get(1).id());
    }

    @Test
    void preservesOrderOfSimilarProducts() {
        String productId = "1";
        List<String> similarIds = List.of("3", "2", "4");

        ProductDetail product3 = new ProductDetail("3", "Product 3", BigDecimal.valueOf(30.0), true);
        ProductDetail product2 = new ProductDetail("2", "Product 2", BigDecimal.valueOf(20.0), true);
        ProductDetail product4 = new ProductDetail("4", "Product 4", BigDecimal.valueOf(40.0), true);

        when(productRepository.getSimilarProductIds(productId)).thenReturn(similarIds);
        when(productRepository.getProductDetail("3")).thenReturn(Optional.of(product3));
        when(productRepository.getProductDetail("2")).thenReturn(Optional.of(product2));
        when(productRepository.getProductDetail("4")).thenReturn(Optional.of(product4));

        List<ProductDetail> result = service.getSimilarProducts(productId);

        assertEquals(3, result.size());
        assertEquals("3", result.get(0).id());
        assertEquals("2", result.get(1).id());
        assertEquals("4", result.get(2).id());
    }

    @Test
    void filtersOutEmptyProductDetails() {
        String productId = "1";
        List<String> similarIds = List.of("2", "3");

        ProductDetail product2 = new ProductDetail("2", "Product 2", BigDecimal.valueOf(10.0), true);

        when(productRepository.getSimilarProductIds(productId)).thenReturn(similarIds);
        when(productRepository.getProductDetail("2")).thenReturn(Optional.of(product2));
        when(productRepository.getProductDetail("3")).thenReturn(Optional.empty());

        List<ProductDetail> result = service.getSimilarProducts(productId);

        assertEquals(1, result.size());
        assertEquals("2", result.get(0).id());
    }

    @Test
    void filtersOutProductDetailsWhenRepositoryThrowsForSomeIds() {
        String productId = "1";
        List<String> similarIds = List.of("2", "3", "4");

        ProductDetail product2 = new ProductDetail("2", "Product 2", BigDecimal.valueOf(10.0), true);
        ProductDetail product4 = new ProductDetail("4", "Product 4", BigDecimal.valueOf(40.0), true);

        when(productRepository.getSimilarProductIds(productId)).thenReturn(similarIds);
        when(productRepository.getProductDetail("2")).thenReturn(Optional.of(product2));
        when(productRepository.getProductDetail("3")).thenThrow(new RuntimeException("pete"));
        when(productRepository.getProductDetail("4")).thenReturn(Optional.of(product4));

        List<ProductDetail> result = service.getSimilarProducts(productId);

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).id());
        assertEquals("4", result.get(1).id());
    }

    @Test
    void propagatesExceptionWhenGettingSimilarIdsFails() {
        String productId = "1";

        when(productRepository.getSimilarProductIds(productId))
                .thenThrow(new ExternalServiceException("upstream error"));

        assertThrows(ExternalServiceException.class, () -> service.getSimilarProducts(productId));
        verify(productRepository, never()).getProductDetail(anyString());
    }
}
