package dev.handsup.image.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

public record UploadImagesRequest(
    @NotNull(message = "이미지는 필수 입력 항목입니다.")
    @Size(min = 1, max = 10, message = "이미지는 1장 이상 10장 이하로 선택하세요.")
    List<MultipartFile> images,

    @NotNull(message = "대표 이미지 인덱스를 선택해주세요.")
    Integer mainIndex
) {

}
