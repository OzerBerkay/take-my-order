package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.exception.ReviewAlreadyDeactivatedException;
import com.berkay.order.service.domain.exception.ReviewTimeExpiredException;
import com.berkay.order.service.domain.valueobject.OrderReviewId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderReviewTest {

    @Test
    @DisplayName("Should initialize review with valid rating successfully")
    void shouldInitializeReviewSuccessfully() {
        OrderReview review = OrderReview.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(5)
                .comment("Harika yemek!")
                .customerNameMasked("B** Ö***")
                .build();

        review.initializeReview();

        assertNotNull(review.getId());
        assertTrue(review.isActive());
        assertNotNull(review.getCreatedAt());
        assertEquals(5, review.getRating());
        assertEquals("Harika yemek!", review.getComment());
    }

    @Test
    @DisplayName("Should throw OrderDomainException when rating is less than 1")
    void shouldThrowExceptionWhenRatingLessThanOne() {
        OrderReview review = OrderReview.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(0)
                .build();

        assertThrows(OrderDomainException.class, review::initializeReview);
    }

    @Test
    @DisplayName("Should throw OrderDomainException when rating is greater than 5")
    void shouldThrowExceptionWhenRatingGreaterThanFive() {
        OrderReview review = OrderReview.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(6)
                .build();

        assertThrows(OrderDomainException.class, review::initializeReview);
    }

    @Test
    @DisplayName("Should reply to review successfully within 7 days")
    void shouldReplyToReviewSuccessfully() {
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(5)
                .active(true)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(2))
                .build();

        review.reply("Afiyet olsun, tekrar bekleriz!");

        assertEquals("Afiyet olsun, tekrar bekleriz!", review.getRestaurantReply());
        assertNotNull(review.getRestaurantRepliedAt());
    }

    @Test
    @DisplayName("Should throw ReviewTimeExpiredException when replying after 7 days")
    void shouldThrowExceptionWhenReplyingAfterSevenDays() {
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(5)
                .active(true)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(8))
                .build();

        assertThrows(ReviewTimeExpiredException.class, () -> review.reply("Afiyet olsun!"));
    }

    @Test
    @DisplayName("Should throw OrderDomainException when review is already replied")
    void shouldThrowExceptionWhenReviewAlreadyReplied() {
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(5)
                .active(true)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1))
                .restaurantReply("Ilk yanit")
                .restaurantRepliedAt(ZonedDateTime.now(ZoneId.of("UTC")).minusHours(1))
                .build();

        assertThrows(OrderDomainException.class, () -> review.reply("Ikinci yanit denemesi"));
    }

    @Test
    @DisplayName("Should throw OrderDomainException when replying to an inactive review")
    void shouldThrowExceptionWhenReplyingToInactiveReview() {
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(5)
                .active(false)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1))
                .build();

        assertThrows(OrderDomainException.class, () -> review.reply("Yanit"));
    }

    @Test
    @DisplayName("Should throw OrderDomainException when reply text is blank")
    void shouldThrowExceptionWhenReplyIsBlank() {
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(5)
                .active(true)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1))
                .build();

        assertThrows(OrderDomainException.class, () -> review.reply("   "));
    }

    @Test
    @DisplayName("Should deactivate review successfully with reason and moderator ID")
    void shouldDeactivateReviewSuccessfully() {
        UUID moderatorId = UUID.randomUUID();
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(1)
                .active(true)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1))
                .build();

        review.deactivate("Topluluk kurallari ihlali", moderatorId);

        assertFalse(review.isActive());
        assertEquals("Topluluk kurallari ihlali", review.getDeactivationReason());
        assertEquals(moderatorId, review.getDeactivatedBy());
        assertNotNull(review.getDeactivatedAt());
    }

    @Test
    @DisplayName("Should throw ReviewAlreadyDeactivatedException when deactivating an already inactive review (irreversible)")
    void shouldThrowExceptionWhenDeactivatingAlreadyDeactivatedReview() {
        UUID moderatorId = UUID.randomUUID();
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(1)
                .active(false)
                .deactivatedAt(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1))
                .build();

        assertThrows(ReviewAlreadyDeactivatedException.class,
                () -> review.deactivate("Ikinci kez silme denemesi", moderatorId));
    }

    @Test
    @DisplayName("Should throw OrderDomainException when deactivation reason is blank")
    void shouldThrowExceptionWhenDeactivationReasonIsBlank() {
        UUID moderatorId = UUID.randomUUID();
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(1)
                .active(true)
                .build();

        assertThrows(OrderDomainException.class, () -> review.deactivate("  ", moderatorId));
    }

    @Test
    @DisplayName("canReply should return true when active, unreplied, and within 7 days")
    void canReplyShouldReturnTrueWithinSevenDays() {
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(4)
                .active(true)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(3))
                .build();

        assertTrue(review.canReply());
    }

    @Test
    @DisplayName("canReply should return false when review has already been replied")
    void canReplyShouldReturnFalseWhenAlreadyReplied() {
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(4)
                .active(true)
                .restaurantReply("Teşekkürler")
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1))
                .build();

        assertFalse(review.canReply());
    }

    @Test
    @DisplayName("canReply should return false when review is older than 7 days")
    void canReplyShouldReturnFalseWhenOlderThanSevenDays() {
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(2)
                .active(true)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(8))
                .build();

        assertFalse(review.canReply());
    }

    @Test
    @DisplayName("canReply should return false when review is inactive")
    void canReplyShouldReturnFalseWhenInactive() {
        OrderReview review = OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .rating(2)
                .active(false)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1))
                .build();

        assertFalse(review.canReply());
    }
}
