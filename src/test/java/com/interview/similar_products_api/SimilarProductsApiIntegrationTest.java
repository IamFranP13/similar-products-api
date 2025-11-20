package com.interview.similar_products_api;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SimilarProductsApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("external-api.url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void shouldReturnSimilarProducts_whenFlowIsSuccessful() throws Exception {
        // 1. Mock similar IDs response
        mockWebServer.enqueue(new MockResponse()
                .setBody("[\"2\", \"3\"]")
                .addHeader("Content-Type", "application/json"));

        // 2. Mock product 2 details
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "id": "2",
                          "name": "Product 2",
                          "price": 10.00,
                          "availability": true
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        // 3. Mock product 3 details
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "id": "3",
                          "name": "Product 3",
                          "price": 20.00,
                          "availability": true
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(get("/product/1/similar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[0].name").value("Product 2"))
                .andExpect(jsonPath("$[1].id").value("3"))
                .andExpect(jsonPath("$[1].name").value("Product 3"));
    }

    @Test
    void shouldReturnNotFound_whenUpstreamReturns404ForSimilarIds() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        mockMvc.perform(get("/product/999/similar"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product Not found"));
    }
}
