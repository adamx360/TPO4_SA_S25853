/**
 *
 *  @author Śliwa Adam S25853
 *
 */

package zad1;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Locale;

public class Time {
    public static String passed(String from, String to) {
        try {
            DateTimeFormatter dateF = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.forLanguageTag("pl"));
            DateTimeFormatter dateTimeF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.forLanguageTag("pl"));
            Temporal fromTemp;
            Temporal toTemp;
            if (from.contains("T") && to.contains("T")) {
                fromTemp = LocalDateTime.parse(from, dateTimeF);
                toTemp = LocalDateTime.parse(to, dateTimeF);
            } else {
                fromTemp = LocalDate.parse(from, dateF);
                toTemp = LocalDate.parse(to, dateF);
            }
            LocalDate fromDate = LocalDate.from(fromTemp);
            LocalDate toDate = LocalDate.from(toTemp);
            if (from.split("-")[1].equals("02") && from.split("-")[2].equals("29") && !fromDate.isLeapYear()) {
                return ("*** java.time.format.DateTimeParseException: Text '" + from + "' could not be parsed: Invalid date 'February 29' as '" + fromDate.getYear() + "' is not a leap year");
            }
            if (to.split("-")[1].equals("02") && to.split("-")[2].equals("29") && !toDate.isLeapYear()) {
                return ("*** java.time.format.DateTimeParseException: Text '" + to + "' could not be parsed: Invalid date 'February 29' as '" + toDate.getYear() + "' is not a leap year");
            }
            return generateTimeReport(fromTemp, toTemp);
        } catch (
                DateTimeParseException e) {
            return "*** " + e;
        }
    }

    private static String generateCalendarReport(Temporal from, Temporal to) {
        LocalDate fromD = LocalDate.from(from);
        LocalDate toD = LocalDate.from(to);
        ZoneId z = ZoneId.systemDefault();
        Period per = Period.between(fromD, toD.atStartOfDay(z).toLocalDate());
        String ans = "";
        ans += (per.getYears() == 1 ? per.getYears() + " rok" : per.getYears() > 1 ? (per.getYears() > 4 ? per.getYears() + " lat" : per.getYears() + " lata") : "");
        if (per.getYears() > 0 && (per.getMonths() > 0 || per.getDays() > 0)) ans += ", ";
        ans += (per.getMonths() == 1 ? per.getMonths() + " miesiąc" : per.getMonths() >= 5 ? per.getMonths() + " miesięcy" : (per.getMonths() > 1?per.getMonths() + " miesiące":  ""));
        if (per.getMonths() > 0 && per.getDays() > 0) ans += ", ";
        ans += (per.getDays() == 1 ? per.getDays() + " dzień" : per.getDays() > 1 ? per.getDays() + " dni" : "");
        return ans;
    }

    private static String generateTimeReport(Temporal from, Temporal to) {
        DateTimeFormatter dOWFor = DateTimeFormatter.ofPattern("EEEE", new Locale("pl", "PL"));
        DateTimeFormatter mFor = DateTimeFormatter.ofPattern("MMMM", new Locale("pl", "PL"));
        ZoneId z;
        ZonedDateTime zDT;
        ZonedDateTime convZDT;
        LocalDate fromD;
        LocalDate toD;
        if (from instanceof LocalDateTime && to instanceof LocalDateTime) {
            z = ZoneId.systemDefault();
            zDT = ZonedDateTime.now();
            convZDT = zDT.withZoneSameInstant(ZoneId.of("Europe/Warsaw"));
            fromD = LocalDate.from(((LocalDateTime) from).atZone(ZoneId.from(convZDT)));
            toD = LocalDate.from(((LocalDateTime) to).atZone(ZoneId.from(convZDT)));
        } else {
            z = ZoneId.systemDefault();
            fromD = LocalDate.from(from);
            toD = LocalDate.from(to);
        }
        String fromDStr = fromD.getDayOfMonth() + " " + fromD.format(mFor) + " " + fromD.getYear() + " (" + fromD.format(dOWFor) + ")";
        String toDStr = toD.getDayOfMonth() + " " + toD.format(mFor) + " " + toD.getYear() + " (" + toD.format(dOWFor) + ")";
        long dBet = ChronoUnit.DAYS.between(fromD, toD);
        double wBet = (double) dBet / 7;
        String wBetStr = wBet % 1 == 0 ? String.format("%.0f", wBet) : String.format("%.2f", wBet);
        wBetStr = wBetStr.replace(',', '.');
        String calRep = generateCalendarReport(from, to);
        StringBuilder rep = new StringBuilder();
        rep.append("Od ").append(fromDStr);
        if (from instanceof LocalDateTime) {
            LocalDateTime fromDateTime = LocalDateTime.from(from);
            ZonedDateTime fromZonedDateTime = fromDateTime.atZone(z);
            rep.append(" godz. ").append(fromZonedDateTime.getHour()).append(":").append(String.format("%02d", fromZonedDateTime.getMinute()));
        }
        rep.append(" do ").append(toDStr);
        if (to instanceof LocalDateTime) {
            LocalDateTime toDateTime = LocalDateTime.from(to);
            ZonedDateTime toZonedDateTime = toDateTime.atZone(z);
            rep.append(" godz. ").append(toZonedDateTime.getHour()).append(":").append(String.format("%02d", toZonedDateTime.getMinute()));
        }
        rep.append("\n");
        rep.append(" - mija: ").append(dBet == 1 ? "1 dzień" : dBet + " dni").append(", tygodni ").append(wBetStr).append("\n");
        if (from instanceof LocalDateTime && to instanceof LocalDateTime) {
            LocalDateTime fromDT = LocalDateTime.from(from);
            LocalDateTime toDT = LocalDateTime.from(to);

            ZonedDateTime fromZDT = fromDT.atZone(z);
            ZonedDateTime toZDT = toDT.atZone(z);

            long hBet = ChronoUnit.HOURS.between(fromZDT, toZDT);
            long mBet = ChronoUnit.MINUTES.between(fromZDT, toZDT);
            rep.append(" - godzin: ").append(hBet).append(", minut: ").append(mBet).append("\n");
        }
        if (!calRep.equals("")) rep.append(" - kalendarzowo: ").append(calRep).append("\n");
        return rep.toString();
    }

    public static String now() {
        LocalDateTime currTime = LocalDateTime.now();
        DateTimeFormatter form = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        return currTime.format(form);
    }
}