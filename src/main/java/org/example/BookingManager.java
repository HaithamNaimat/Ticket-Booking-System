/**
        * BookingManager.java
 * Responsible for processing event ticket bookings.
        * Uses Dependency Injection for all external dependencies.
 */
package org.example;
;public class BookingManager {

    private final IPaymentGateway paymentGateway;
    private final INotificationService notificationService;
    private final IEventRepository eventRepository;

    /**
     * Constructor-based Dependency Injection.
     * All dependencies are provided externally, making this class fully testable.
     */
    public BookingManager(IPaymentGateway paymentGateway,
                          INotificationService notificationService,
                          IEventRepository eventRepository) {
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
        this.eventRepository = eventRepository;
    }

    /**
     * Processes a ticket booking request.
     *
     * @param userId  The ID of the user making the booking (must not be null or empty)
     * @param eventId The ID of the event to book (must not be null or empty)
     * @param amount  The payment amount (must be greater than 0)
     * @return true if booking was successful, false otherwise
     */
    public boolean bookTicket(String userId, String eventId, double amount) {

        // US-02: Validate input — if invalid, do nothing and return false
        if (userId == null || userId.trim().isEmpty() ||
                eventId == null || eventId.trim().isEmpty() ||
                amount <= 0) {
            return false;
        }

        // US-03: Check if event is sold out
        if (eventRepository.isSoldOut(eventId)) {
            return false;
        }

        // US-01: Process payment
        String transactionId = paymentGateway.processPayment(userId, amount);
        if (transactionId == null || transactionId.isEmpty()) {
            return false;
        }

        // US-01: Save booking and send confirmation
        eventRepository.saveBooking(userId, eventId, transactionId);
        notificationService.sendConfirmation(userId, eventId, transactionId);

        return true;
    }
}

// ──────────────────────────────────────────────
// Interface definitions (each would normally be in its own file)
// ──────────────────────────────────────────────

interface IPaymentGateway {
    /**
     * Processes a payment and returns a transaction ID.
     * Returns null or empty string on failure.
     */
    String processPayment(String userId, double amount);
}

interface INotificationService {
    /**
     * Sends a booking confirmation to the user.
     */
    void sendConfirmation(String userId, String eventId, String transactionId);
}

interface IEventRepository {
    /**
     * Checks whether the event is sold out.
     */
    boolean isSoldOut(String eventId);

    /**
     * Persists the booking record.
     */
    void saveBooking(String userId, String eventId, String transactionId);
}
