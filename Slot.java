import java.time.LocalDate;
import java.time.LocalDateTime;

class Slot {
    private final int slotId;
    private final int doctorId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final boolean emergency;
    private boolean booked;

    Slot(int slotId, int doctorId, LocalDateTime startTime, LocalDateTime endTime, boolean emergency) {
        this.slotId = slotId;
        this.doctorId = doctorId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.emergency = emergency;
        this.booked = false;
    }

    int getSlotId() {
        return slotId;
    }

    int getDoctorId() {
        return doctorId;
    }

    LocalDateTime getStartTime() {
        return startTime;
    }

    LocalDate getDate() {
        return startTime.toLocalDate();
    }

    boolean isEmergency() {
        return emergency;
    }

    boolean isBooked() {
        return booked;
    }

    boolean isAvailableForOnlineBooking() {
        return !booked && !emergency;
    }

    boolean isAvailableForInternalBooking() {
        return !booked;
    }

    void markBooked() {
        this.booked = true;
    }

    void release() {
        this.booked = false;
    }

    String summary() {
        return String.format(
            "slotId=%d, date=%s, time=%s-%s%s",
            slotId,
            getDate(),
            startTime.toLocalTime(),
            endTime.toLocalTime(),
            emergency ? " [Emergency Reserved]" : ""
        );
    }
}
