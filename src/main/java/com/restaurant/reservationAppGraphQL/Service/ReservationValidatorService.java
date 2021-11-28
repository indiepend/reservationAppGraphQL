package com.restaurant.reservationAppGraphQL.Service;

import com.restaurant.reservationAppGraphQL.Model.Reservation;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

public class ReservationValidatorService {
    @Value("${application.restaurant.working-hours.start}")
    private static int startOfWork;

    @Value("${application.restaurant.working-hours.is-open-for-x-hours}")
    private static int isOpenForXHours;

    /*//////////
    Let's explain last four conditions as they may be confusing
    Let's take restaurant that is open between hours 11am and 2am
    Now first condition checks if reservation's date is between 11am to midnight.
    But it should be also possible to make reservation at 1am next day
    and that's why we check if reservation's beginning is before closing upon previous day
    Then last conditions are to check if reservation ending is before closing
    *//////////
    public static Boolean validate(final Reservation reservation){
        return reservation.getDate().isAfter(LocalDateTime.now())
                & reservation.getDuration() <= 360
                & reservation.getDuration() >= 30
                & reservation.getFullName().matches("[A-Za-z]{3,30} [A-Za-z]{3,30}")
                & reservation.getPhone().matches("[0-9]{9}")
                & reservation.getEmail().matches("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")
                & reservation.getEmail().length() < 255
                & reservation.getNumberOfSeats() > 0
                & (reservation.getDate()
                    .isAfter(reservation.getDate().toLocalDate().atStartOfDay().plusHours(startOfWork))
                & reservation.getDate().plusMinutes(reservation.getDuration())
                    .isBefore(reservation.getDate().toLocalDate().atStartOfDay().plusHours(startOfWork + isOpenForXHours)))
                | (reservation.getDate()
                    .isBefore(reservation.getDate().toLocalDate().minusDays(1).atStartOfDay().plusHours(startOfWork + isOpenForXHours))
                & reservation.getDate().plusMinutes(reservation.getDuration())
                    .isBefore(reservation.getDate().toLocalDate().minusDays(1).atStartOfDay().plusHours(startOfWork + isOpenForXHours)));
    }
}
