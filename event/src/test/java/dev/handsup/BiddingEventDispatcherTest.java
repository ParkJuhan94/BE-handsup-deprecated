package dev.handsup;

import static org.junit.jupiter.api.Assertions.*;
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
import dev.handsup.common.entity.DeadLetterLog;
import dev.handsup.common.entity.DeadLetterLog.DeadLetterStatus;
import dev.handsup.common.repository.DeadLetterLogRepository;
import dev.handsup.common.service.RedisDuplicateChecker;
import dev.handsup.common.service.SlackNotificationService;
import dev.handsup.event.common.EventHandler;
import dev.handsup.event.consumer.BiddingEventDispatcher;
import dev.handsup.fixture.AuctionFixture;
import dev.handsup.fixture.BiddingFixture;
import dev.handsup.fixture.UserFixture;
import dev.handsup.user.domain.User;

@DisplayName("[Bidding Event Dispatcher 테스트]")
@ExtendWith(MockitoExtension.class)
class BiddingEventDispatcherTest {

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

    @DisplayName("[이벤트를 받으면 DeadLetterLog 를 저장한다.]")
    @Test
    void handleDLT() throws Exception {
        // given
        User bidder = UserFixture.user1();
        Auction auction = AuctionFixture.auction();
        Bidding bidding = BiddingFixture.bidding(auction, bidder);
        BiddingEvent fakeEvent = BiddingMapper.toBiddingEvent(bidding, BiddingEventType.REGISTERED);
        String json = "{\"fake\":\"payload\"}";

        given(objectMapper.writeValueAsString(any())).willReturn(json);

        // when
        String exceptionMessage = "Some error";
        String exceptionClass = "java.lang.Exception";
        DeadLetterLog deadLetterLog = dispatcher.handleDLT(fakeEvent, "bidding-events.dlt", exceptionClass,
            exceptionMessage);

        // then
        verify(deadLetterLogRepository, times(1)).save(any(DeadLetterLog.class));
        verify(slackNotificationService, times(1)).send(contains(exceptionMessage));
        assertAll(
            () -> assertEquals("bidding-events", deadLetterLog.getOriginTopic()),
            () -> assertEquals(json, deadLetterLog.getPayload()),
            () -> assertEquals(exceptionClass, deadLetterLog.getExceptionClass()),
            () -> assertEquals(exceptionMessage, deadLetterLog.getErrorMessage()),
            () -> assertEquals(DeadLetterStatus.FAILED, deadLetterLog.getStatus())
        );
    }

}