import java.time.LocalDateTime;

class Consultation {
    private final int consultationId;
    private final int appointmentId;
    private final String notes;
    private final String prescription;
    private final LocalDateTime dateTime;

    Consultation(int consultationId, int appointmentId, String notes, String prescription, LocalDateTime dateTime) {
        this.consultationId = consultationId;
        this.appointmentId = appointmentId;
        this.notes = notes;
        this.prescription = prescription;
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return String.format(
            "Consultation{id=%d, appointmentId=%d, notes='%s', prescription='%s', dateTime=%s}",
            consultationId, appointmentId, notes, prescription, dateTime
        );
    }
}
