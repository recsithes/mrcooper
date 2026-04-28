import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Doctor {
    private final int doctorId;
    private final String name;
    private final String specialty;
    private boolean onLeave;
    private final Map<Integer, Slot> slots;

    Doctor(int doctorId, String name, String specialty) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialty = specialty;
        this.onLeave = false;
        this.slots = new HashMap<>();
    }

    int getDoctorId() {
        return doctorId;
    }

    String getName() {
        return name;
    }

    String getSpecialty() {
        return specialty;
    }

    boolean isOnLeave() {
        return onLeave;
    }

    void markOnLeave(boolean onLeave) {
        this.onLeave = onLeave;
    }

    void addSlot(Slot slot) {
        slots.put(slot.getSlotId(), slot);
    }

    Slot getSlot(int slotId) {
        return slots.get(slotId);
    }

    List<Slot> getAvailableSlots(LocalDate date, boolean onlineBooking) {
        List<Slot> result = new ArrayList<>();
        for (Slot slot : slots.values()) {
            if (!slot.getDate().equals(date)) {
                continue;
            }
            if (onlineBooking && slot.isAvailableForOnlineBooking()) {
                result.add(slot);
            } else if (!onlineBooking && slot.isAvailableForInternalBooking()) {
                result.add(slot);
            }
        }
        return result;
    }
}
