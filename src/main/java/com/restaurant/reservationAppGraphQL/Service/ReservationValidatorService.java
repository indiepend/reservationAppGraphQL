package com.restaurant.reservationAppGraphQL.Service;

import com.restaurant.reservationAppGraphQL.Model.Reservation;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@ConfigurationProperties(prefix = "application.restaurant.working-hours")
@EnableConfigurationProperties
@Data
public class ReservationValidatorService {
    private Map<String, Long> start;
    private Map<String, Long> isOpenForXHours;

    public Boolean validate(Reservation reservation){
        return reservation.getDate().isAfter(LocalDateTime.now())
                & reservation.getDuration() <= 360
                & reservation.getDuration() >= 30
                & reservation.getFullName().matches("[A-Za-z]{3,30} [A-Za-z]{3,30}")
                & reservation.getPhone().matches("[0-9]{9}")
                & reservation.getEmail().matches("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")
                & reservation.getEmail().length() < 255
                & reservation.getNumberOfSeats() > 0
                & (isReservationsTimeInWorkingHours(reservation.getDate(), reservation.getDuration(), false)
                | isReservationsTimeInWorkingHours(reservation.getDate(), reservation.getDuration(), true));
    }

    private Boolean isReservationsTimeInWorkingHours(LocalDateTime reservationDate, int duration, boolean checkPreviousDay){
        LocalDate checkedDay;
        if(checkPreviousDay)
            checkedDay = reservationDate.toLocalDate().minusDays(1);
        else
            checkedDay = reservationDate.toLocalDate();

        long startOfWorkForDayOfWeek = start.get(checkedDay.getDayOfWeek().name());//
        long isOpenForXHoursForDayOfWeek = isOpenForXHours.get(checkedDay.getDayOfWeek().name());//

        LocalDateTime restaurantOpening = checkedDay.atStartOfDay()//
                .plusHours(startOfWorkForDayOfWeek);
        LocalDateTime restaurantClosing = checkedDay.atStartOfDay()//
                .plusHours(startOfWorkForDayOfWeek + isOpenForXHoursForDayOfWeek);
        LocalDateTime endOfReservation = reservationDate.plusMinutes(duration);

        return reservationDate.isAfter(restaurantOpening)
                & reservationDate.isBefore(restaurantClosing)
                & endOfReservation.isBefore(restaurantClosing);
    }
}
