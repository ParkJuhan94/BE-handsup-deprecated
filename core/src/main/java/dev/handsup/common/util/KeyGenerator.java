package dev.handsup.common.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class KeyGenerator {

    public static String generateRecommendAuctionsKey(String si, String gu, String dong,
        Pageable pageable) {
        return String.format("recommend:auctions:%s:%s:%s:%s:%d:%d",
            nullToEmpty(si),
            nullToEmpty(gu),
            nullToEmpty(dong),
            extractSortField(pageable),
            pageable.getPageNumber(),
            pageable.getPageSize());
    }

    public static String generateBiddingEventKey(String eventId) {
        return String.format("bidding:event:%s", eventId);
    }

    private static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    private static String extractSortField(Pageable pageable) {
        return pageable.getSort().stream()
            .findFirst()
            .map(Sort.Order::getProperty)
            .orElse("");
    }
}
