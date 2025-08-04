package dev.handsup.search.dto;

import java.util.List;

public record PopularAuctionKeywordsResponse(
    List<PopularKeywordResponse> keywords
) {

    public static PopularAuctionKeywordsResponse from(List<PopularKeywordResponse> responses) {
        return new PopularAuctionKeywordsResponse(responses);
    }
}
