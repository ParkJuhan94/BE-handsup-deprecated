package dev.handsup.common.initializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import dev.handsup.auction.domain.product.Product;
import dev.handsup.auction.domain.product.ProductImage;
import dev.handsup.auction.repository.product.ProductImageRepository;
import dev.handsup.auction.repository.product.ProductRepository;

@Component
@Profile("local")
@RequiredArgsConstructor
public class DummyProductImageInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        List<Product> allProducts = productRepository.findAll();
        List<ProductImage> allImages = new ArrayList<>();

        for (Product product : allProducts) {
            int mainIndex = random.nextInt(5);

            for (int i = 0; i < 5; i++) {
                boolean isMain = (i == mainIndex);
                String imageUrl =
                    "https://example.com/images/" + product.getId() + "_" + i + ".jpg";

                ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(imageUrl)
                    .isMain(isMain)
                    .build();

                allImages.add(image);
            }
        }

        productImageRepository.saveAll(allImages);
    }
}