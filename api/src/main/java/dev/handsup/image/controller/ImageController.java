package dev.handsup.image.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import dev.handsup.auction.service.ProductImageService;
import dev.handsup.auth.annotation.NoAuth;
import dev.handsup.image.dto.UploadImagesRequest;
import dev.handsup.image.dto.UploadImagesResponse;
import dev.handsup.image.service.S3Service;

@Tag(name = "상품 이미지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final S3Service s3Service;
    private final ProductImageService productImageService;

    @NoAuth
    @Operation(summary = "상품 이미지 업로드 API", description = "상품의 이미지를 등록하고, 대표 이미지를 지정한다.")
    @ApiResponse(useReturnTypeSchema = true)
    @PostMapping("/{productId}")
    public ResponseEntity<UploadImagesResponse> uploadImages(
        @PathVariable Long productId,
        @ModelAttribute @Valid UploadImagesRequest uploadImagesRequest
    ) {
        List<String> imageUrls = s3Service.uploadImages(uploadImagesRequest.images());

        productImageService.saveImages(
            productId,
            imageUrls,
            uploadImagesRequest.mainIndex()
        );

        return ResponseEntity.ok(UploadImagesResponse.from(imageUrls));
    }
}
