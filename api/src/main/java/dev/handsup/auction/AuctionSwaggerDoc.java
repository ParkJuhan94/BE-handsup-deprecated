package dev.handsup.auction;

public class AuctionSwaggerDoc {

    private AuctionSwaggerDoc() {
        throw new UnsupportedOperationException("prevent instantiation of utility class");
    }

    public static final class SortOption {

        public static final String DESCRIPTION = """
            정렬 기준 (아래 값 중 하나 입력):

            | 요청값 (value) | 설명 (description) |
            |----------------|--------------------|
            | BOOKMARK       | 북마크 많은 순     |
            | END_DATE       | 마감 임박 순       |
            | BIDDING        | 입찰 많은 순       |
            | CREATED        | 최근 등록된 순     |
            """;

        private SortOption() {
        }


    }

}
