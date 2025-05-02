package dev.handsup.auction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.handsup.auction.domain.auction_field.PurchaseTime;
import dev.handsup.auction.domain.product.Product;
import dev.handsup.auction.domain.product.ProductImage;
import dev.handsup.auction.domain.product.ProductStatus;
import dev.handsup.auction.domain.product.product_category.ProductCategory;
import dev.handsup.auction.domain.product.product_category.ProductCategoryValue;
import dev.handsup.auction.repository.product.ProductImageRepository;
import dev.handsup.auction.repository.product.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @InjectMocks
    private ProductImageServiceImpl productImageService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductImageRepository productImageRepository;

    @DisplayName("[지정한 인덱스가 대표로 설정된다.]")
    @Test
    void saveImages() {
        // given
        List<String> imageUrls = List.of(
            "https://bucket.s3.amazonaws.com/images/1.jpg",
            "https://bucket.s3.amazonaws.com/images/2.jpg",
            "https://bucket.s3.amazonaws.com/images/3.jpg"
        );
        Product product1 = Product.of(
            ProductStatus.NEW, "설명",
            PurchaseTime.UNDER_ONE_MONTH,
            ProductCategory.from(ProductCategoryValue.SPORTS_LEISURE.getLabel()),
            imageUrls);
        int mainIndex = 1;
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.of(product1));
        when(productImageRepository.existsByProductIdAndIsMainTrue(productId)).thenReturn(false);
        when(productImageRepository.saveAll(any())).thenAnswer(
            invocation -> invocation.getArgument(0));

        // when
        List<ProductImage> saved = productImageService.saveImages(productId, imageUrls, mainIndex);

        // then
        assertThat(saved).hasSize(3);
        assertThat(saved.get(mainIndex).isMain()).isTrue();
        for (int i = 0; i < saved.size(); i++) {
            if (i != mainIndex) {
                assertThat(saved.get(i).isMain()).isFalse();
            }
        }
    }

}