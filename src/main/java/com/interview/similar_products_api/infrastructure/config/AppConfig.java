package com.interview.similar_products_api.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AppConfig {

    @Bean
    public RestClient restClient(@Value("${external-api.url:http://localhost:3001}") String externalApiUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                java.util.Objects.requireNonNull(httpClient));
        requestFactory.setReadTimeout(java.util.Objects.requireNonNull(Duration.ofSeconds(5)));

        return RestClient.builder()
                .baseUrl(java.util.Objects.requireNonNull(externalApiUrl))
                .requestFactory(requestFactory)
                .build();
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
