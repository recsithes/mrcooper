import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Hospital {
    private static final int MAX_APPOINTMENTS_PER_DAY = 20;
    private static final int MAX_RESCHEDULES = 2;
    private static final Duration LATE_CANCELLATION_WINDOW = Duration.ofHours(1);

    private final Map<Integer, Doctor> doctors;
    private final Map<Integer, Patient> patients;
    private final Map<Integer, Appointment> appointments;

    private int nextPatientId = 1001;
    private int nextAppointmentId = 5001;
    private int nextConsultationId = 9001;
    private int nextSlotId = 1;

    Hospital() {
        this.doctors = new HashMap<>();
        this.patients = new HashMap<>();
        this.appointments = new HashMap<>();
    }

    void seedData() {
        Doctor cardio = new Doctor(1, "Dr. Arjun", "Cardiology");
        Doctor ortho = new Doctor(2, "Dr. Meera", "Orthopedics");
        Doctor eye = new Doctor(3, "Dr. Kavin", "Ophthalmology");

        addDoctorWithSlots(cardio);
        addDoctorWithSlots(ortho);
        addDoctorWithSlots(eye);

        // Demo scenario for alternate flow A3 (doctor on leave)
        eye.markOnLeave(true);
    }

    private void addDoctorWithSlots(Doctor doctor) {
        doctors.put(doctor.getDoctorId(), doctor);
        createDaySlots(doctor, LocalDate.now());
        createDaySlots(doctor, LocalDate.now().plusDays(1));
    }

    private void createDaySlots(Doctor doctor, LocalDate date) {
        int totalSlots = 10;
        int emergencySlots = Math.max(1, (int) Math.ceil(totalSlots * 0.15));
        // Last 15% slots are reserved for emergency and blocked from online booking.
        for (int i = 0; i < totalSlots; i++) {
            LocalTime slotStart = LocalTime.of(9, 0).plusMinutes(i * 30L);
            LocalDateTime start = LocalDateTime.of(date, slotStart);
            LocalDateTime end = start.plusMinutes(30);
            boolean isEmergency = i >= totalSlots - emergencySlots;
            Slot slot = new Slot(nextSlotId++, doctor.getDoctorId(), start, end, isEmergency);
            doctor.addSlot(slot);
        }
    }

    synchronized int registerPatient(String name, int age, String phone) {
        Patient patient = new Patient(nextPatientId, name, age, phone);
        patients.put(nextPatientId, patient);
        return nextPatientId++;
    }

    List<Doctor> searchDoctorBySpecialty(String specialty) {
        List<Doctor> result = new ArrayList<>();
        for (Doctor doctor : doctors.values()) {
            if (doctor.getSpecialty().equalsIgnoreCase(specialty)) {
                result.add(doctor);
            }
        }
        return result;
    }

    void listDoctors() {
        System.out.println("\nDoctors:");
        for (Doctor doctor : doctors.values()) {
            System.out.printf(
                "doctorId=%d, name=%s, specialty=%s%s%n",
                doctor.getDoctorId(),
                doctor.getName(),
                doctor.getSpecialty(),
                doctor.isOnLeave() ? " [ON LEAVE]" : ""
            );
        }
    }

    void showAvailableSlotsForSpecialty(String specialty, LocalDate preferredDate) {
        List<Doctor> matchedDoctors = searchDoctorBySpecialty(specialty);
        if (matchedDoctors.isEmpty()) {
            System.out.println("E5: No doctors available in that specialty.");
            return;
        }

        boolean found = false;
        for (Doctor doctor : matchedDoctors) {
            if (doctor.isOnLeave()) {
                continue;
            }
            List<Slot> slots = doctor.getAvailableSlots(preferredDate, true);
            if (slots.isEmpty()) {
                continue;
            }
            found = true;
            for (Slot slot : slots) {
                System.out.printf(
                    "doctorId=%d (%s), %s%n",
                    doctor.getDoctorId(),
                    doctor.getName(),
                    slot.summary()
                );
            }
        }

        if (!found) {
            // Alternate Flow A1: suggest nearest next date with open online slots.
            LocalDate nextDate = findNextAvailableDate(matchedDoctors, preferredDate.plusDays(1), 30);
            if (nextDate == null) {
                System.out.println("A1: No slots available for preferred date and no upcoming slots in next 30 days.");
            } else {
                System.out.printf("A1: No slots on %s. Suggested next available date: %s%n", preferredDate, nextDate);
                for (Doctor doctor : matchedDoctors) {
                    if (doctor.isOnLeave()) {
                        continue;
                    }
                    for (Slot slot : doctor.getAvailableSlots(nextDate, true)) {
                        System.out.printf(
                            "doctorId=%d (%s), %s%n",
                            doctor.getDoctorId(),
                            doctor.getName(),
                            slot.summary()
                        );
                    }
                }
            }
        }
    }

    private LocalDate findNextAvailableDate(List<Doctor> doctorsBySpecialty, LocalDate startDate, int maxDaysAhead) {
        for (int i = 0; i < maxDaysAhead; i++) {
            LocalDate candidate = startDate.plusDays(i);
            for (Doctor doctor : doctorsBySpecialty) {
                if (doctor.isOnLeave()) {
                    continue;
                }
                if (!doctor.getAvailableSlots(candidate, true).isEmpty()) {
                    return candidate;
                }
            }
        }
        return null;
    }

    synchronized void bookAppointment(int patientId, int doctorId, int slotId) {
        Patient patient = patients.get(patientId);
        if (patient == null) {
            System.out.println("E1: Invalid patient ID - patient not found.");
            return;
        }

        Doctor doctor = doctors.get(doctorId);
        if (doctor == null) {
            System.out.println("Doctor not found.");
            return;
        }

        if (doctor.isOnLeave()) {
            System.out.println("A3: Doctor is on leave. Suggested doctors in same specialty:");
            suggestAlternativeDoctors(doctor.getSpecialty(), doctorId);
            return;
        }

        Slot slot = doctor.getSlot(slotId);
        if (slot == null) {
            System.out.println("Invalid slot for selected doctor.");
            return;
        }

        if (slot.isEmergency()) {
            System.out.println("Business Rule: Emergency slots are reserved and cannot be booked online.");
            return;
        }

        if (slot.isBooked()) {
            System.out.println("E2: Slot already booked by another patient.");
            return;
        }

        LocalDate appointmentDate = slot.getDate();
        if (countDoctorAppointmentsOnDate(doctorId, appointmentDate) >= MAX_APPOINTMENTS_PER_DAY) {
            System.out.println("Business Rule: Doctor reached daily booking limit.");
            return;
        }

        if (hasPatientSameDoctorSameDay(patientId, doctorId, appointmentDate, -1)) {
            System.out.println("Business Rule: Patient already has an appointment with this doctor on the same day.");
            return;
        }

        // Lock selected slot and create a unique appointment record.
        slot.markBooked();
        Appointment appointment = new Appointment(nextAppointmentId, patientId, doctorId, slotId, slot.getStartTime());
        appointments.put(nextAppointmentId, appointment);
        int createdId = nextAppointmentId++;

        System.out.printf("Appointment booked. Appointment ID: %d%n", createdId);
        System.out.println("Confirmation sent to patient.");
        System.out.printf("Reminder scheduled for 24 hours before: %s%n", slot.getStartTime().minusHours(24));
    }

    synchronized void cancelAppointment(int appointmentId) {
        Appointment appointment = appointments.get(appointmentId);
        if (appointment == null) {
            System.out.println("E3: Appointment ID not found for cancellation.");
            return;
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            System.out.println("Appointment already cancelled.");
            return;
        }

        Doctor doctor = doctors.get(appointment.getDoctorId());
        if (doctor != null) {
            Slot slot = doctor.getSlot(appointment.getSlotId());
            if (slot != null) {
                slot.release();
            }
        }

        appointment.markCancelled();
        Duration timeToAppointment = Duration.between(LocalDateTime.now(), appointment.getSlotStartTime());
        if (!timeToAppointment.isNegative() && timeToAppointment.compareTo(LATE_CANCELLATION_WINDOW) <= 0) {
            System.out.println("E4: Late cancellation within 1 hour - fee applies.");
        } else {
            System.out.println("Appointment cancelled successfully.");
        }
    }

    synchronized void rescheduleAppointment(int appointmentId, int newDoctorId, int newSlotId) {
        Appointment appointment = appointments.get(appointmentId);
        if (appointment == null) {
            System.out.println("E3: Appointment ID not found.");
            return;
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            System.out.println("Cancelled appointments cannot be rescheduled.");
            return;
        }

        if (appointment.getRescheduleCount() >= MAX_RESCHEDULES) {
            System.out.println("Business Rule: A patient can reschedule at most twice per appointment.");
            return;
        }

        Doctor newDoctor = doctors.get(newDoctorId);
        if (newDoctor == null) {
            System.out.println("Doctor not found.");
            return;
        }

        if (newDoctor.isOnLeave()) {
            System.out.println("A3: Doctor is on leave. Suggested doctors in same specialty:");
            suggestAlternativeDoctors(newDoctor.getSpecialty(), newDoctorId);
            return;
        }

        Slot newSlot = newDoctor.getSlot(newSlotId);
        if (newSlot == null) {
            System.out.println("Invalid new slot.");
            return;
        }
        if (newSlot.isEmergency()) {
            System.out.println("Business Rule: Emergency slots are reserved and cannot be booked online.");
            return;
        }
        if (newSlot.isBooked()) {
            System.out.println("E2: New slot already booked.");
            return;
        }

        LocalDate newDate = newSlot.getDate();
        if (countDoctorAppointmentsOnDate(newDoctorId, newDate) >= MAX_APPOINTMENTS_PER_DAY) {
            System.out.println("Business Rule: Doctor reached daily booking limit.");
            return;
        }

        if (hasPatientSameDoctorSameDay(appointment.getPatientId(), newDoctorId, newDate, appointmentId)) {
            System.out.println("Business Rule: Patient already has an appointment with this doctor on this day.");
            return;
        }

        // Release old slot only after all new-slot validations pass.
        Doctor oldDoctor = doctors.get(appointment.getDoctorId());
        if (oldDoctor != null) {
            Slot oldSlot = oldDoctor.getSlot(appointment.getSlotId());
            if (oldSlot != null) {
                oldSlot.release();
            }
        }

        newSlot.markBooked();
        appointment.reschedule(newDoctorId, newSlotId, newSlot.getStartTime());
        System.out.println("A2: Appointment rescheduled successfully.");
    }

    synchronized void completeAppointment(int appointmentId, String notes, String prescription) {
        Appointment appointment = appointments.get(appointmentId);
        if (appointment == null) {
            System.out.println("Appointment not found.");
            return;
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            System.out.println("Cancelled appointment cannot be completed.");
            return;
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            System.out.println("Appointment already completed.");
            return;
        }

        appointment.markCompleted(notes);
        // Postcondition: consultation details are appended to patient's history.
        Patient patient = patients.get(appointment.getPatientId());
        if (patient != null) {
            Consultation consultation = new Consultation(
                nextConsultationId++,
                appointmentId,
                notes,
                prescription,
                LocalDateTime.now()
            );
            patient.addConsultation(consultation);
        }
        System.out.println("Appointment marked COMPLETED. Consultation record updated.");
    }

    void printAppointments() {
        if (appointments.isEmpty()) {
            System.out.println("No appointments yet.");
            return;
        }
        for (Appointment appointment : appointments.values()) {
            System.out.println(appointment);
        }
    }

    private int countDoctorAppointmentsOnDate(int doctorId, LocalDate date) {
        int count = 0;
        for (Appointment appointment : appointments.values()) {
            if (appointment.getDoctorId() == doctorId
                && appointment.getStatus() != AppointmentStatus.CANCELLED
                && appointment.getSlotStartTime().toLocalDate().equals(date)) {
                count++;
            }
        }
        return count;
    }

    private boolean hasPatientSameDoctorSameDay(int patientId, int doctorId, LocalDate date, int ignoreAppointmentId) {
        for (Appointment appointment : appointments.values()) {
            if (appointment.getAppointmentId() == ignoreAppointmentId) {
                continue;
            }
            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }
            if (appointment.getPatientId() == patientId
                && appointment.getDoctorId() == doctorId
                && appointment.getSlotStartTime().toLocalDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    private void suggestAlternativeDoctors(String specialty, int excludeDoctorId) {
        boolean found = false;
        for (Doctor doctor : doctors.values()) {
            if (doctor.getDoctorId() == excludeDoctorId) {
                continue;
            }
            if (!doctor.getSpecialty().equalsIgnoreCase(specialty)) {
                continue;
            }
            if (doctor.isOnLeave()) {
                continue;
            }
            found = true;
            System.out.printf(
                "doctorId=%d, name=%s, specialty=%s%n",
                doctor.getDoctorId(), doctor.getName(), doctor.getSpecialty()
            );
        }
        if (!found) {
            System.out.println("No alternate doctors currently available in this specialty.");
        }
    }
}
