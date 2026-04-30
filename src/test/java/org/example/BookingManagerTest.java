package org.example;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    // ── Mocks ──────────────────────────────────────────────────────────────────
    @Mock
    private IPaymentGateway paymentGateway;

    @Mock
    private INotificationService notificationService;

    @Mock
    private IEventRepository eventRepository;

    // ── Subject under test ─────────────────────────────────────────────────────
    private BookingManager bookingManager;

    // ── Test data ──────────────────────────────────────────────────────────────
    private static final String VALID_USER_ID  = "user-123";
    private static final String VALID_EVENT_ID = "event-456";
    private static final double VALID_AMOUNT   = 99.99;
    private static final String TRANSACTION_ID = "txn-789";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookingManager = new BookingManager(paymentGateway, notificationService, eventRepository);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // US-01 — Happy Path
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("US-01: saveBooking() and sendConfirmation() are each called exactly once on success")
    void testHappyPath_saveAndConfirmCalledOnce() {
        // Arrange
        when(eventRepository.isSoldOut(VALID_EVENT_ID)).thenReturn(false);
        when(paymentGateway.processPayment(VALID_USER_ID, VALID_AMOUNT)).thenReturn(TRANSACTION_ID);

        // Act
        boolean result = bookingManager.bookTicket(VALID_USER_ID, VALID_EVENT_ID, VALID_AMOUNT);

        // Assert
        assertTrue(result, "Booking should succeed on happy path");

        verify(eventRepository, times(1)).saveBooking(VALID_USER_ID, VALID_EVENT_ID, TRANSACTION_ID);
        verify(notificationService, times(1)).sendConfirmation(VALID_USER_ID, VALID_EVENT_ID, TRANSACTION_ID);
    }

    @Test
    @DisplayName("US-01: processPayment() is called exactly once on success")
    void testHappyPath_processPaymentCalledOnce() {
        // Arrange
        when(eventRepository.isSoldOut(VALID_EVENT_ID)).thenReturn(false);
        when(paymentGateway.processPayment(VALID_USER_ID, VALID_AMOUNT)).thenReturn(TRANSACTION_ID);

        // Act
        bookingManager.bookTicket(VALID_USER_ID, VALID_EVENT_ID, VALID_AMOUNT);

        // Assert
        verify(paymentGateway, times(1)).processPayment(VALID_USER_ID, VALID_AMOUNT);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // US-02 — Invalid Input Path
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("US-02: No methods called when userId is null")
    void testInvalidInput_nullUserId() {
        // Act
        boolean result = bookingManager.bookTicket(null, VALID_EVENT_ID, VALID_AMOUNT);

        // Assert
        assertFalse(result, "Booking should fail with null userId");

        verify(paymentGateway, never()).processPayment(anyString(), anyDouble());
        verify(eventRepository, never()).saveBooking(anyString(), anyString(), anyString());
        verify(notificationService, never()).sendConfirmation(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("US-02: No methods called when userId is empty")
    void testInvalidInput_emptyUserId() {
        // Act
        boolean result = bookingManager.bookTicket("", VALID_EVENT_ID, VALID_AMOUNT);

        // Assert
        assertFalse(result, "Booking should fail with empty userId");

        verify(paymentGateway, never()).processPayment(anyString(), anyDouble());
        verify(eventRepository, never()).saveBooking(anyString(), anyString(), anyString());
        verify(notificationService, never()).sendConfirmation(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("US-02: No methods called when eventId is null")
    void testInvalidInput_nullEventId() {
        // Act
        boolean result = bookingManager.bookTicket(VALID_USER_ID, null, VALID_AMOUNT);

        // Assert
        assertFalse(result);

        verify(paymentGateway, never()).processPayment(anyString(), anyDouble());
        verify(eventRepository, never()).saveBooking(anyString(), anyString(), anyString());
        verify(notificationService, never()).sendConfirmation(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("US-02: No methods called when amount is zero or negative")
    void testInvalidInput_zeroAmount() {
        // Act
        boolean result = bookingManager.bookTicket(VALID_USER_ID, VALID_EVENT_ID, 0);

        // Assert
        assertFalse(result, "Booking should fail with zero amount");

        verify(paymentGateway, never()).processPayment(anyString(), anyDouble());
        verify(eventRepository, never()).saveBooking(anyString(), anyString(), anyString());
        verify(notificationService, never()).sendConfirmation(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("US-02: No methods called when amount is negative")
    void testInvalidInput_negativeAmount() {
        // Act
        boolean result = bookingManager.bookTicket(VALID_USER_ID, VALID_EVENT_ID, -50.0);

        // Assert
        assertFalse(result, "Booking should fail with negative amount");

        verify(paymentGateway, never()).processPayment(anyString(), anyDouble());
        verify(eventRepository, never()).saveBooking(anyString(), anyString(), anyString());
        verify(notificationService, never()).sendConfirmation(anyString(), anyString(), anyString());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // US-03 — Sold Out Path
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("US-03: isSoldOut() is called, but subsequent methods are never called when sold out")
    void testSoldOut_onlyIsSoldOutCalled() {
        // Arrange
        when(eventRepository.isSoldOut(VALID_EVENT_ID)).thenReturn(true);

        // Act
        boolean result = bookingManager.bookTicket(VALID_USER_ID, VALID_EVENT_ID, VALID_AMOUNT);

        // Assert
        assertFalse(result, "Booking should fail when event is sold out");

        verify(eventRepository, times(1)).isSoldOut(VALID_EVENT_ID);

        verify(paymentGateway, never()).processPayment(anyString(), anyDouble());
        verify(eventRepository, never()).saveBooking(anyString(), anyString(), anyString());
        verify(notificationService, never()).sendConfirmation(anyString(), anyString(), anyString());
    }
}