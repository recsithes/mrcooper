import java.util.ArrayList;
import java.util.List;

class Patient {
    private final int patientId;
    private final String name;
    private final int age;
    private final String phone;
    private final List<Consultation> medicalHistory;

    Patient(int patientId, String name, int age, String phone) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.medicalHistory = new ArrayList<>();
    }

    int getPatientId() {
        return patientId;
    }

    String getName() {
        return name;
    }

    void addConsultation(Consultation consultation) {
        medicalHistory.add(consultation);
    }

    @Override
    public String toString() {
        return String.format("Patient{id=%d, name='%s', age=%d, phone='%s'}", patientId, name, age, phone);
    }
}
