package dev.handsup.auction.service;

import java.util.List;

import dev.handsup.auction.domain.product.ProductImage;

public interface ProductImageService {

    List<ProductImage> saveImages(Long productId, List<String> imageUrls, int mainIndex);
}
