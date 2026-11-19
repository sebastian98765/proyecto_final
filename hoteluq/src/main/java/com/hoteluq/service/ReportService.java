package com.hoteluq.service;

import com.hoteluq.model.Reservation;
import com.hoteluq.model.Room;
import com.hoteluq.model.enums.ReservationStatus;
import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public class ReportService {
    public String summary(List<Reservation> data, LocalDate from, LocalDate to) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        if (to.isBefore(from)) throw new IllegalArgumentException("Periodo inválido");
        List<Reservation> selected = data.stream()
                .filter(r -> !r.getCheckIn().isBefore(from) && !r.getCheckIn().isAfter(to))
                .toList();
        long confirmed = selected.stream().filter(r -> r.getStatus() == ReservationStatus.CONFIRMED).count();
        long cancelled = selected.stream().filter(r -> r.getStatus() == ReservationStatus.CANCELLED).count();
        double income = selected.stream().filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .mapToDouble(Reservation::getTotal).sum();
        StringBuilder result = new StringBuilder("HOTELUQ - REPORTE DE RESERVAS\n")
                .append("Generado: ").append(LocalDateTime.now()).append('\n')
                .append("Periodo: ").append(from).append(" a ").append(to).append('\n')
                .append("Registros: ").append(selected.size()).append('\n')
                .append("Confirmadas: ").append(confirmed).append('\n')
                .append("Canceladas: ").append(cancelled).append('\n')
                .append("Ingresos de reservas confirmadas: $").append(String.format(Locale.US, "%.2f", income)).append(" COP\n");
        for (Reservation r : selected) {
            result.append(r.getId()).append(" | ").append(r.getCustomer().getName())
                    .append(" | Hab. ").append(r.getRoom().getNumber())
                    .append(" | ").append(r.getCheckIn()).append(" a ").append(r.getCheckOut())
                    .append(" | ").append(r.getStatus()).append(" | $").append(r.getTotal()).append('\n');
        }
        return result.toString();
    }

    /** Ocupación por noches para un periodo; una habitación cuenta una vez por noche. */
    public String occupancy(List<Reservation> reservations, List<Room> rooms, LocalDate from, LocalDate to) {
        Objects.requireNonNull(reservations);
        Objects.requireNonNull(rooms);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        if (to.isBefore(from)) throw new IllegalArgumentException("Periodo inválido");
        long days = java.time.temporal.ChronoUnit.DAYS.between(from, to.plusDays(1));
        long capacity = days * rooms.size();
        long occupied = 0;
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            LocalDate current = day;
            occupied += reservations.stream()
                    .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                    .filter(r -> !current.isBefore(r.getCheckIn()) && current.isBefore(r.getCheckOut()))
                    .map(r -> r.getRoom().getNumber()).distinct()
                    .filter(number -> rooms.stream().anyMatch(room -> room.getNumber().equals(number)))
                    .count();
        }
        double percent = capacity == 0 ? 0 : occupied * 100.0 / capacity;
        return "HOTELUQ - OCUPACIÓN\nPeriodo: " + from + " a " + to
                + "\nHabitaciones: " + rooms.size() + "\nNoches disponibles: " + capacity
                + "\nNoches ocupadas: " + occupied
                + "\nOcupación: " + String.format(Locale.US, "%.2f", percent) + "%\n";
    }

    public Path exportTxt(String content, Path path) throws IOException {
        Objects.requireNonNull(content);
        Objects.requireNonNull(path);
        Files.writeString(path, content);
        return path;
    }
}
