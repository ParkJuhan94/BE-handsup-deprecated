package dev.handsup.search.dto;

import static lombok.AccessLevel.*;

import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class SearchMapper {

    public static PopularAuctionKeywordsResponse toPopularAuctionKeywordsResponse(
        List<PopularKeywordResponse> responses) {
        return PopularAuctionKeywordsResponse.from(responses);
    }

}
