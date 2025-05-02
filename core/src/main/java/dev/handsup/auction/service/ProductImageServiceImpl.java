package dev.handsup.auction.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import dev.handsup.auction.domain.product.Product;
import dev.handsup.auction.domain.product.ProductImage;
import dev.handsup.auction.exception.AuctionErrorCode;
import dev.handsup.auction.repository.product.ProductImageRepository;
import dev.handsup.auction.repository.product.ProductRepository;
import dev.handsup.common.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    @Transactional
    public List<ProductImage> saveImages(Long productId, List<String> imageUrls, int mainIndex) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException(AuctionErrorCode.NOT_FOUND_PRODUCT));

        boolean hasMain = productImageRepository.existsByProductIdAndIsMainTrue(productId);
        if (hasMain) {
            productImageRepository.updateIsMainFalse(productId);
        }

        List<ProductImage> productImages = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            boolean isMain = (i == mainIndex);
            productImages.add(ProductImage.builder()
                .product(product)
                .imageUrl(imageUrls.get(i))
                .isMain(isMain)
                .build());
        }

        return productImageRepository.saveAll(productImages);
    }
}
