# Hospital Appointment System (UC-009)

## Problem statement
Build a hospital appointment system to handle:
- Patient registration
- Doctor schedule/slot management
- Appointment booking, cancellation, and rescheduling
- Consultation completion and medical history update

The implementation should follow UC-009 flows and business rules from the statement, including:
- Unique patient and appointment IDs
- Slot-based booking with double-booking prevention
- Suggesting next available date when preferred date has no slots
- Suggesting alternative doctor if selected doctor is on leave
- Late-cancellation fee message for cancellations within 1 hour
- Max 20 appointments/day per doctor
- Max 2 reschedules per appointment
- Emergency slots (15%) blocked from online booking
- No same patient booking with same doctor on same day

## Approach / logic used
The system is implemented in Java using object-oriented classes:
- `Hospital`: orchestrates all operations and enforces business rules.
- `Doctor`: stores doctor details, leave status, and slots.
- `Slot`: holds date/time, booking status, and emergency flag.
- `Patient`: stores patient details and consultation history.
- `Appointment`: tracks booking state, slot, doctor/patient mapping, and reschedule count.
- `Consultation`: stores notes/prescription after appointment completion.

Core logic:
1. Seed doctors and slots for today/tomorrow.
2. Register patients with generated IDs.
3. Search slots by specialty + preferred date.
4. Book appointment only if slot is valid, not booked, not emergency, and all constraints pass.
5. On cancellation, release slot and check 1-hour late-cancel rule.
6. On reschedule, release old slot and book new slot while enforcing reschedule limit and constraints.
7. On completion, mark appointment completed and append consultation to medical history.

## Steps to execute the code
1. Open terminal in the project folder:
   `c:\Users\sithe\OneDrive\Desktop\mrcooper`
2. Compile:
   `javac *.java`
3. Run:
   `java Main`
4. Use the menu options shown in the console:
   - Register patient
   - View doctors/slots
   - Book/cancel/reschedule/complete appointments

