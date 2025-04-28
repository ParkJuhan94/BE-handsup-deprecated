package dev.handsup.common.util;

import org.springframework.data.domain.Pageable;

public class CacheKeyGenerator {

    public static String recommendAuctionsKey(String si, String gu, String dong,
        Pageable pageable) {
        return String.format("recommend:auctions:%s:%s:%s:%s:%d:%d",
            nullToEmpty(si),
            nullToEmpty(gu),
            nullToEmpty(dong),
            extractSortField(pageable),
            pageable.getPageNumber(),
            pageable.getPageSize());
    }

    private static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    private static String extractSortField(Pageable pageable) {
        return pageable.getSort().stream()
            .findFirst()
            .map(order -> order.getProperty())
            .orElse("");
    }
}
