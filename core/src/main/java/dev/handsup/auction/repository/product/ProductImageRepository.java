package dev.handsup.auction.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.handsup.auction.domain.product.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    boolean existsByProductIdAndIsMainTrue(Long productId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductImage pi SET pi.isMain = false WHERE pi.product.id = :productId AND pi.isMain = true")
    void updateIsMainFalse(@Param("productId") Long productId);
}
