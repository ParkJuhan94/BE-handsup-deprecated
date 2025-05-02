package dev.handsup.auction.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.handsup.auction.domain.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
