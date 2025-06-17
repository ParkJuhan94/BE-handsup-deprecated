package dev.handsup;

import static dev.handsup.kafka.exception.KafkaErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.handsup.auction.domain.Auction;
import dev.handsup.bidding.domain.Bidding;
import dev.handsup.bidding.dto.BiddingMapper;
import dev.handsup.bidding.event.BiddingEvent;
import dev.handsup.bidding.event.BiddingEvent.BiddingEventType;
import dev.handsup.common.repository.DeadLetterLogRepository;
import dev.handsup.common.service.RedisDuplicateChecker;
import dev.handsup.common.service.SlackNotificationService;
import dev.handsup.event.common.EventHandler;
import dev.handsup.event.consumer.BiddingEventDispatcher;
import dev.handsup.fixture.AuctionFixture;
import dev.handsup.fixture.BiddingFixture;
import dev.handsup.fixture.UserFixture;
import dev.handsup.kafka.exception.KafkaException;
import dev.handsup.user.domain.User;

@DisplayName("[Bidding Event Dispatcher 테스트]")
@ExtendWith(MockitoExtension.class)
class BiddingEventDispatcherTest {

    private static final User bidder = UserFixture.user1();
    private static final Auction auction = AuctionFixture.auction();
    private static final Bidding bidding = BiddingFixture.bidding(auction, bidder);
    public static final BiddingEvent fakeEvent = BiddingMapper.toBiddingEvent(bidding, BiddingEventType.REGISTERED);
    private static final String EVENT_ID = fakeEvent.eventId();

    @Mock
    private List<EventHandler<BiddingEvent>> handlers;
    @Mock
    private DeadLetterLogRepository deadLetterLogRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RedisDuplicateChecker redisDuplicateChecker;
    @Mock
    private SlackNotificationService slackNotificationService;
    @InjectMocks
    private BiddingEventDispatcher dispatcher;

    @DisplayName("[중복 아님 - 정상 핸들러 호출]")
    @Test
    void nonDuplicateEventHandledSuccessfully() throws Exception {
        // given
        given(redisDuplicateChecker.checkDuplicateAndCacheIfAbsent(EVENT_ID)).willReturn(false);
        EventHandler<BiddingEvent> handler = mock(EventHandler.class);
        given(handler.supports(fakeEvent)).willReturn(true);
        given(handlers.stream()).willReturn(List.of(handler).stream());

        // when
        dispatcher.listen(fakeEvent, "bidding-events");

        // then
        verify(handler).handle(fakeEvent);
        verifyNoInteractions(deadLetterLogRepository, slackNotificationService);
    }

    @DisplayName("[중복 이벤트 && isRetry == true]")
    @Test
    void duplicateEventWithRetry() {
        // given
        String retryTopic = "bidding-events-retry-0";
        given(redisDuplicateChecker.checkDuplicateAndCacheIfAbsent(EVENT_ID)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> dispatcher.listen(fakeEvent, retryTopic)).isInstanceOf(KafkaException.class)
            .hasMessageContaining(DUPLICATE_RETRY_EVENT.getMessage());

        verifyNoInteractions(handlers); // 핸들러는 호출되면 안됨
        verify(deadLetterLogRepository, never()).save(any());
        verify(slackNotificationService, never()).send(any());
    }

    @DisplayName("[중복 이벤트 && isRetry == false]")
    @Test
    void duplicateEventWithoutRetry() throws Exception {
        // given
        given(redisDuplicateChecker.checkDuplicateAndCacheIfAbsent(any())).willReturn(true);

        // when
        dispatcher.listen(fakeEvent, "bidding-events");

        // then
        verify(deadLetterLogRepository).save(any());
        verify(slackNotificationService).send(contains("중복"));
        verifyNoInteractions(handlers); // 핸들러는 호출되면 안됨
    }
}