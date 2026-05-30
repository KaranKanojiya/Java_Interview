package interview.level2_java8.datetime;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * Q17. What is the Java 8 DateTime API?
 *
 * Java 8 introduced java.time package (JSR-310) to replace the problematic java.util.Date/Calendar.
 *
 * Old API problems:
 *   ❌ Date is mutable (not thread-safe)
 *   ❌ Month starts at 0 (January = 0)
 *   ❌ Year starts at 1900
 *   ❌ No timezone support in Date
 *   ❌ SimpleDateFormat is not thread-safe
 *
 * New API (java.time):
 *   ✅ All classes are IMMUTABLE and thread-safe
 *   ✅ Clear separation: LocalDate, LocalTime, LocalDateTime, ZonedDateTime
 *   ✅ DateTimeFormatter is thread-safe
 *   ✅ Month is 1-based (January = 1)
 *
 * Key classes:
 *   LocalDate      → date only (2024-03-15)
 *   LocalTime      → time only (14:30:00)
 *   LocalDateTime  → date + time, no timezone
 *   ZonedDateTime  → date + time + timezone
 *   Instant        → machine timestamp (epoch seconds)
 *   Duration       → time-based amount (hours, minutes, seconds)
 *   Period         → date-based amount (years, months, days)
 */
public class DateTimeApiDemo {

    public static void main(String[] args) {

        // === LocalDate ===
        System.out.println("=== LocalDate ===");
        LocalDate today = LocalDate.now();
        LocalDate specific = LocalDate.of(2024, 3, 15);
        LocalDate parsed = LocalDate.parse("2024-12-25");

        System.out.println("Today: " + today);
        System.out.println("Specific: " + specific);
        System.out.println("Parsed: " + parsed);
        System.out.println("Year: " + today.getYear() + ", Month: " + today.getMonthValue() + ", Day: " + today.getDayOfMonth());
        System.out.println("Day of week: " + today.getDayOfWeek());
        System.out.println("Is leap year: " + today.isLeapYear());

        // Arithmetic — returns NEW object (immutable)
        System.out.println("Tomorrow: " + today.plusDays(1));
        System.out.println("Last month: " + today.minusMonths(1));

        // === LocalTime ===
        System.out.println("\n=== LocalTime ===");
        LocalTime now = LocalTime.now();
        LocalTime meeting = LocalTime.of(14, 30, 0);
        System.out.println("Now: " + now);
        System.out.println("Meeting: " + meeting);
        System.out.println("After 2 hours: " + meeting.plusHours(2));

        // === LocalDateTime ===
        System.out.println("\n=== LocalDateTime ===");
        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime combined = LocalDateTime.of(specific, meeting);
        System.out.println("Now: " + dateTime);
        System.out.println("Combined: " + combined);

        // === ZonedDateTime ===
        System.out.println("\n=== ZonedDateTime ===");
        ZonedDateTime mumbai = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime newYork = ZonedDateTime.now(ZoneId.of("America/New_York"));
        System.out.println("Mumbai: " + mumbai);
        System.out.println("New York: " + newYork);

        // === Instant — machine timestamp ===
        System.out.println("\n=== Instant ===");
        Instant instant = Instant.now();
        System.out.println("Instant: " + instant);
        System.out.println("Epoch seconds: " + instant.getEpochSecond());
        System.out.println("Epoch millis: " + instant.toEpochMilli());

        // Convert old Date to new API
        Date oldDate = new Date();
        Instant fromOld = oldDate.toInstant();
        LocalDateTime fromInstant = LocalDateTime.ofInstant(fromOld, ZoneId.systemDefault());
        System.out.println("Old Date → LocalDateTime: " + fromInstant);

        // === Period & Duration ===
        System.out.println("\n=== Period (date-based) & Duration (time-based) ===");
        Period period = Period.between(LocalDate.of(2020, 1, 1), today);
        System.out.println("Period since 2020: " + period.getYears() + "y " + period.getMonths() + "m " + period.getDays() + "d");

        Duration duration = Duration.between(LocalTime.of(9, 0), LocalTime.of(17, 30));
        System.out.println("Work duration: " + duration.toHours() + "h " + duration.toMinutesPart() + "m");

        // === ChronoUnit — calculating differences ===
        System.out.println("\n=== ChronoUnit ===");
        long daysBetween = ChronoUnit.DAYS.between(specific, today);
        System.out.println("Days between " + specific + " and today: " + daysBetween);

        // === DateTimeFormatter ===
        System.out.println("\n=== DateTimeFormatter (thread-safe!) ===");
        DateTimeFormatter custom = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        String formatted = dateTime.format(custom);
        System.out.println("Custom format: " + formatted);

        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE;
        System.out.println("ISO format: " + today.format(iso));

        // === TemporalAdjusters ===
        System.out.println("\n=== TemporalAdjusters ===");
        System.out.println("First day of month: " + today.with(TemporalAdjusters.firstDayOfMonth()));
        System.out.println("Last day of month: " + today.with(TemporalAdjusters.lastDayOfMonth()));
        System.out.println("Next Monday: " + today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)));
    }
}
