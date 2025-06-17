package dev.handsup.bookmark.domain;

import static jakarta.persistence.ConstraintMode.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import dev.handsup.auction.domain.Auction;
import dev.handsup.common.domain.TimeBaseEntity;
import dev.handsup.user.domain.User;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Bookmark extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(NO_CONSTRAINT))
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "auction_id",
        nullable = false,
        foreignKey = @ForeignKey(NO_CONSTRAINT))
    private Auction auction;

    @Builder
    private Bookmark(User user, Auction auction) {
        this.user = user;
        this.auction = auction;
    }

    public static Bookmark of(
        User user,
        Auction auction
    ) {
        return Bookmark.builder()
            .user(user)
            .auction(auction).build();
    }
}
