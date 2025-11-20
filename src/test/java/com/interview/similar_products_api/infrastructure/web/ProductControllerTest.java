package com.interview.similar_products_api.infrastructure.web;

import com.interview.similar_products_api.domain.exception.ExternalServiceException;
import com.interview.similar_products_api.domain.exception.ProductNotFoundException;
import com.interview.similar_products_api.domain.model.ProductDetail;
import com.interview.similar_products_api.domain.port.in.GetSimilarProductsQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetSimilarProductsQuery getSimilarProductsQuery;

    @Test
    void shouldReturnSimilarProductsWhenFound() throws Exception {
        String productId = "1";
        ProductDetail product = new ProductDetail("2", "Product 2", new BigDecimal("10.00"), true);
        List<ProductDetail> products = List.of(product);

        when(getSimilarProductsQuery.getSimilarProducts(productId)).thenReturn(products);

        mockMvc.perform(get("/product/{productId}/similar", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[0].name").value("Product 2"))
                .andExpect(jsonPath("$[0].price").value(10.00))
                .andExpect(jsonPath("$[0].availability").value(true));
    }

    @Test
    void shouldReturnEmptyListWhenNoSimilarProducts() throws Exception {
        String productId = "1";
        when(getSimilarProductsQuery.getSimilarProducts(productId)).thenReturn(List.of());

        mockMvc.perform(get("/product/{productId}/similar", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnNotFoundWhenProductNotFoundException() throws Exception {
        String productId = "1";
        when(getSimilarProductsQuery.getSimilarProducts(productId))
                .thenThrow(new ProductNotFoundException(productId));

        mockMvc.perform(get("/product/{productId}/similar", productId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Product Not found"));
    }

    @Test
    void shouldReturnBadGatewayWhenExternalServiceException() throws Exception {
        String productId = "1";
        when(getSimilarProductsQuery.getSimilarProducts(productId))
                .thenThrow(new ExternalServiceException("Error"));

        mockMvc.perform(get("/product/{productId}/similar", productId))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Upstream service error"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenUnexpectedException() throws Exception {
        String productId = "1";
        when(getSimilarProductsQuery.getSimilarProducts(productId))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(get("/product/{productId}/similar", productId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("An unexpected error occurred"));
    }
}
