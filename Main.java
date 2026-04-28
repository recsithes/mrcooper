import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        Hospital hospital = new Hospital();
        hospital.seedData();

        Scanner scanner = new Scanner(System.in);
        // CLI loop: dispatches each menu option to its matching flow method.
        while (true) {
            printMenu();
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1":
                    registerPatientFlow(hospital, scanner);
                    break;
                case "2":
                    hospital.listDoctors();
                    break;
                case "3":
                    showSlotsFlow(hospital, scanner);
                    break;
                case "4":
                    bookFlow(hospital, scanner);
                    break;
                case "5":
                    cancelFlow(hospital, scanner);
                    break;
                case "6":
                    rescheduleFlow(hospital, scanner);
                    break;
                case "7":
                    completeFlow(hospital, scanner);
                    break;
                case "8":
                    hospital.printAppointments();
                    break;
                case "0":
                    System.out.println("Goodbye.");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== Hospital Appointment System ===");
        System.out.println("1. Register patient");
        System.out.println("2. List doctors");
        System.out.println("3. Show available slots by specialty and date");
        System.out.println("4. Book appointment");
        System.out.println("5. Cancel appointment");
        System.out.println("6. Reschedule appointment");
        System.out.println("7. Complete appointment (add consultation notes)");
        System.out.println("8. View all appointments");
        System.out.println("0. Exit");
        System.out.print("Choose option: ");
    }

    private static void registerPatientFlow(Hospital hospital, Scanner scanner) {
        System.out.print("Enter patient name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter patient age: ");
        int age = readInt(scanner);
        if (age < 0 || age > 120) {
            System.out.println("Invalid age.");
            return;
        }

        System.out.print("Enter patient phone: ");
        String phone = scanner.nextLine().trim();
        int patientId = hospital.registerPatient(name, age, phone);
        System.out.println("Patient registered with ID: " + patientId);
    }

    private static void showSlotsFlow(Hospital hospital, Scanner scanner) {
        System.out.print("Enter specialty: ");
        String specialty = scanner.nextLine().trim();
        LocalDate date = readDate(scanner, "Enter preferred date (yyyy-MM-dd): ");
        if (date == null) {
            return;
        }
        hospital.showAvailableSlotsForSpecialty(specialty, date);
    }

    private static void bookFlow(Hospital hospital, Scanner scanner) {
        System.out.print("Enter patient ID: ");
        int patientId = readInt(scanner);

        System.out.print("Enter specialty: ");
        String specialty = scanner.nextLine().trim();
        LocalDate date = readDate(scanner, "Enter preferred date (yyyy-MM-dd): ");
        if (date == null) {
            return;
        }

        // Show eligible slots first so user can select valid doctorId + slotId.
        hospital.showAvailableSlotsForSpecialty(specialty, date);
        System.out.print("Enter doctor ID to book: ");
        int doctorId = readInt(scanner);
        System.out.print("Enter slot ID to book: ");
        int slotId = readInt(scanner);
        hospital.bookAppointment(patientId, doctorId, slotId);
    }

    private static void cancelFlow(Hospital hospital, Scanner scanner) {
        System.out.print("Enter appointment ID to cancel: ");
        int appointmentId = readInt(scanner);
        hospital.cancelAppointment(appointmentId);
    }

    private static void rescheduleFlow(Hospital hospital, Scanner scanner) {
        System.out.print("Enter appointment ID to reschedule: ");
        int appointmentId = readInt(scanner);
        System.out.print("Enter new doctor ID: ");
        int doctorId = readInt(scanner);
        System.out.print("Enter new slot ID: ");
        int slotId = readInt(scanner);
        hospital.rescheduleAppointment(appointmentId, doctorId, slotId);
    }

    private static void completeFlow(Hospital hospital, Scanner scanner) {
        System.out.print("Enter appointment ID to complete: ");
        int appointmentId = readInt(scanner);
        System.out.print("Enter consultation notes: ");
        String notes = scanner.nextLine().trim();
        System.out.print("Enter prescription: ");
        String prescription = scanner.nextLine().trim();
        hospital.completeAppointment(appointmentId, notes, prescription);
    }

    private static int readInt(Scanner scanner) {
        while (true) {
            String value = scanner.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                // Keep prompting until valid numeric input is provided.
                System.out.print("Enter a valid number: ");
            }
        }
    }

    private static LocalDate readDate(Scanner scanner, String message) {
        System.out.print(message);
        String raw = scanner.nextLine().trim();
        try {
            return LocalDate.parse(raw, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
            return null;
        }
    }
}
