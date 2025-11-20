
package com.interview.similar_products_api.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.interview.similar_products_api.domain.model.ProductDetail;
import com.interview.similar_products_api.domain.port.in.GetSimilarProductsQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@RestController
@RequestMapping("/product")
@Tag(name = "Similar Products", description = "Operations related to similar products")
public class ProductController {

    private final GetSimilarProductsQuery getSimilarProductsQuery;

    public ProductController(GetSimilarProductsQuery getSimilarProductsQuery) {
        this.getSimilarProductsQuery = getSimilarProductsQuery;
    }

    @GetMapping("/{productId}/similar")
    @Operation(summary = "Similar products", description = "Returns the list of similar products for a given productId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProductDetail.class)))),
            @ApiResponse(responseCode = "404", description = "Product Not found")
    })
    public ResponseEntity<List<ProductDetail>> getSimilarProducts(
            @Parameter(description = "Product identifier", required = true) @PathVariable String productId) {
        List<ProductDetail> similarProducts = getSimilarProductsQuery.getSimilarProducts(productId);
        return ResponseEntity.ok(similarProducts);
    }
}
