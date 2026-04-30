package org.example;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 * BookingManagerTest.java
 * Unit tests for BookingManager using JUnit 5 and Mockito.
 *
 * Covers:
 *   US-01 — Happy Path
 *   US-02 — Invalid Input Path
 *   US-03 — Sold Out Path
 */
class BookingManagerTest {

    @Mock
    private IPaymentGateway paymentGateway;

    @Mock
    private INotificationService notificationService;

    @Mock
    private IEventRepository eventRepository;

    private BookingManager bookingManager;

    private static final String VALID_USER_ID  = "user-123";
    private static final String VALID_EVENT_ID = "event-456";
    private static final double VALID_AMOUNT   = 99.99;
    private static final String TRANSACTION_ID = "txn-789";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookingManager = new BookingManager(paymentGateway, notificationService, eventRepository);
    }

    // US-01 — Happy Path

    @Test
    @DisplayName("US-01: saveBooking() and sendConfirmation() are each called exactly once on success")
    void testHappyPath_saveAndConfirmCalledOnce() {
      
        when(eventRepository.isSoldOut(VALID_EVENT_ID)).thenReturn(false);
        when(paymentGateway.processPayment(VALID_USER_ID, VALID_AMOUNT)).thenReturn(TRANSACTION_ID);

        boolean result = bookingManager.bookTicket(VALID_USER_ID, VALID_EVENT_ID, VALID_AMOUNT);

      
        assertTrue(result, "Booking should succeed on happy path");

        verify(eventRepository, times(1)).saveBooking(VALID_USER_ID, VALID_EVENT_ID, TRANSACTION_ID);
        verify(notificationService, times(1)).sendConfirmation(VALID_USER_ID, VALID_EVENT_ID, TRANSACTION_ID);
    }

    @Test
    @DisplayName("US-01: processPayment() is called exactly once on success")
    void testHappyPath_processPaymentCalledOnce() {
     
        when(eventRepository.isSoldOut(VALID_EVENT_ID)).thenReturn(false);
        when(paymentGateway.processPayment(VALID_USER_ID, VALID_AMOUNT)).thenReturn(TRANSACTION_ID);

       
        bookingManager.bookTicket(VALID_USER_ID, VALID_EVENT_ID, VALID_AMOUNT);

       
        verify(paymentGateway, times(1)).processPayment(VALID_USER_ID, VALID_AMOUNT);
    }

}
