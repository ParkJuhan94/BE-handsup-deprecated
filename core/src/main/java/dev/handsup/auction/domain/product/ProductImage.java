package dev.handsup.auction.domain.product;

import static jakarta.persistence.ConstraintMode.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import dev.handsup.common.domain.TimeBaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class ProductImage extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "product_image_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id",
        nullable = false,
        foreignKey = @ForeignKey(NO_CONSTRAINT))
    private Product product;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_main", nullable = false)
    private boolean isMain;

    @Builder
    public ProductImage(Product product, String imageUrl, boolean isMain) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.isMain = isMain;
    }
}
