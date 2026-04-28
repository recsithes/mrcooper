import java.time.LocalDateTime;

class Appointment {
    private final int appointmentId;
    private final int patientId;
    private int doctorId;
    private int slotId;
    private LocalDateTime slotStartTime;
    private AppointmentStatus status;
    private int rescheduleCount;
    private String notes;

    Appointment(int appointmentId, int patientId, int doctorId, int slotId, LocalDateTime slotStartTime) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.slotId = slotId;
        this.slotStartTime = slotStartTime;
        this.status = AppointmentStatus.BOOKED;
        this.rescheduleCount = 0;
        this.notes = "";
    }

    int getAppointmentId() {
        return appointmentId;
    }

    int getPatientId() {
        return patientId;
    }

    int getDoctorId() {
        return doctorId;
    }

    int getSlotId() {
        return slotId;
    }

    LocalDateTime getSlotStartTime() {
        return slotStartTime;
    }

    AppointmentStatus getStatus() {
        return status;
    }

    int getRescheduleCount() {
        return rescheduleCount;
    }

    void markCancelled() {
        this.status = AppointmentStatus.CANCELLED;
    }

    void markCompleted(String notes) {
        this.status = AppointmentStatus.COMPLETED;
        this.notes = notes;
    }

    void reschedule(int newDoctorId, int newSlotId, LocalDateTime newStartTime) {
        this.doctorId = newDoctorId;
        this.slotId = newSlotId;
        this.slotStartTime = newStartTime;
        this.rescheduleCount++;
        this.status = AppointmentStatus.BOOKED;
    }

    @Override
    public String toString() {
        return String.format(
            "Appointment{id=%d, patientId=%d, doctorId=%d, slotId=%d, start=%s, status=%s, reschedules=%d, notes='%s'}",
            appointmentId, patientId, doctorId, slotId, slotStartTime, status, rescheduleCount, notes
        );
    }
}
