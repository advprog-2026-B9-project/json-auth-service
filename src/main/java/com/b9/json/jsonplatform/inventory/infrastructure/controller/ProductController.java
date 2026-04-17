package com.b9.json.jsonplatform.inventory.infrastructure.controller;

import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.inventory.application.service.ProductService;
import com.b9.json.jsonplatform.inventory.domain.model.Product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final AuthService authService;

    public ProductController(ProductService productService, AuthService authService) {
        this.productService = productService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @Validated @RequestBody Product product,
            @RequestHeader("X-User-Name") String ownerUsername) {

        Product createdProduct = productService.createProduct(product, ownerUsername);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<List<Product>> getMyProducts(
            @RequestHeader("X-User-Name") String ownerUsername) {

        List<Product> products = productService.getMyProducts(ownerUsername);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable UUID id,
            @Validated @RequestBody Product product,
            @RequestHeader("X-User-Name") String ownerUsername) {

        Product updatedProduct = productService.updateProduct(id, product, ownerUsername);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID id,
            @RequestHeader("X-User-Name") String ownerUsername) {

        productService.deleteProduct(id, ownerUsername);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<ProductDetailResponse>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String jastiper) {

        List<Product> products = productService.getAllProducts(name, jastiper);
        List<User> allUsers = authService.findAllUsers();
        List<ProductDetailResponse> responseList = products.stream().map(product -> {

            User jastiperProfile = allUsers.stream()
                    .filter(user -> user.getUsername().equals(product.getOwnerUsername()))
                    .findFirst()
                    .orElse(null);

            String fullName = (jastiperProfile != null) ? jastiperProfile.getFullName() : "Anonim";
            String phoneNumber = (jastiperProfile != null) ? jastiperProfile.getPhoneNumber() : "-";

            return new ProductDetailResponse(product, fullName, phoneNumber);

        }).collect(Collectors.toList());

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }
}