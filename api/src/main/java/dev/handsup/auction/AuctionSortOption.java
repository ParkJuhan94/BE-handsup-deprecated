package dev.handsup.auction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.annotation.JsonValue;

@Getter
@RequiredArgsConstructor
public enum AuctionSortOption {
    BOOKMARK("북마크 많은 순"),
    END_DATE("마감 임박 순"),
    BIDDING("입찰 많은 순"),
    CREATED("최근 등록 순");

    private final String description;

    @JsonValue
    public String getNameForSwagger() {
        return name(); // Swagger에 BOOKMARK 등 enum name 그대로 노출
    }
}