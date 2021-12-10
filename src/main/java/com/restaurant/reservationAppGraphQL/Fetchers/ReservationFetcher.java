package com.restaurant.reservationAppGraphQL.Fetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.client.GraphQLClientException;
import com.restaurant.reservationAppGraphQL.Model.Page;
import com.restaurant.reservationAppGraphQL.Model.Reservation;
import com.restaurant.reservationAppGraphQL.Model.RestaurantTable;
import com.restaurant.reservationAppGraphQL.Repository.ReservationRepository;
import com.restaurant.reservationAppGraphQL.Service.EmailService;
import com.restaurant.reservationAppGraphQL.Service.ReservationIDGenerator;
import com.restaurant.reservationAppGraphQL.Service.ReservationValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
@RequiredArgsConstructor
public class ReservationFetcher {

    private final ReservationRepository reservationRepository;
    private final EmailService emailService;
    private final RestaurantTableFetcher restaurantTableFetcher;
    private final ReservationIDGenerator reservationIDGenerator;

    @Value("${application.email.subject.new-reservation}")
    private String newReservationSubject;
    @Value("${application.email.subject.cancel-req}")
    private String cancelReqSubject;
    @Value("${application.email.subject.reservation-cancelled}")
    private String resCancelledSubject;
    @Value("${application.hours-to-cancel}")
    private int hoursToCancelReservation;

    @DgsQuery
    public List<Reservation> getReservations(@InputArgument Page page, @InputArgument String date){
        List<Reservation> reservationList;
        try {
            reservationList = reservationRepository
                    .findAll()
                    .stream()
                    .filter(
                            reservation ->
                                    reservation
                                            .getDate().toLocalDate()
                                            .equals(LocalDate.parse(date)))
                    .collect(Collectors.toList());
        } catch(DateTimeParseException ex){
            throw new GraphQLClientException(500, "/graphql", "error while parsing date " + ex.getMessage(), "getReservations.date");
        }
        if(page != null)
            reservationList = reservationList
                    .stream()
                    .skip(page.getPageNumber() * page.getMaxRowsNumber())
                    .limit(page.getMaxRowsNumber())
                    .collect(Collectors.toList());
        return reservationList;
    }

    @DgsMutation
    public Reservation newReservation(@InputArgument Reservation reservation){
        if(!ReservationValidatorService.validate(reservation))
            throw new GraphQLClientException(500, "/graphql", "validation error", "newReservation.reservation");
        RestaurantTable freeTable = restaurantTableFetcher.findFirstFreeTable(
                reservation.getNumberOfSeats(),
                reservation.getDate(),
                reservation.getDuration()
        );
        reservation.setExternalId(reservationIDGenerator.generate());
        reservation.setRestaurantTable(freeTable);
        freeTable.addReservation(reservation);
        Reservation newReservation = reservationRepository.saveAndFlush(reservation);
        emailService.sendReservationEmail(newReservationSubject, newReservation);
        return newReservation;
    }

    @DgsMutation(field = "reservationCancelRequest")
    public String reservationCancelReq(@InputArgument String id, @InputArgument String status){
        reservationRepository.findByExternalId(id).ifPresentOrElse(
                reservation -> {
                    if(reservation.getDate().isAfter(LocalDateTime.now().plusHours(hoursToCancelReservation))){
                            if(status.equals("requested cancellation")) {
                                String verificationCode = String.format("%06d", Math.round(Math.random() * 1000000));
                                reservation.setVerificationCode(verificationCode);
                                reservationRepository.saveAndFlush(reservation);
                                emailService.sendReservationEmail(cancelReqSubject, reservation);
                            } else throw new GraphQLClientException(500, "/graphql", "required status is invalid", "reservationCancelRequest.status");
                    } else throw new GraphQLClientException(500, "/graphql", "time to cancel reservation is up ", "reservationCancelRequest");
                }, () -> { throw new GraphQLClientException(500, "/graphql", "no such reservation id found", "reservationCancelRequest.id"); });
        return "OK";
    }

    @DgsMutation
    public String reservationCancel(@InputArgument String id, @InputArgument String verificationCode){
        reservationRepository.findByExternalId(id).ifPresentOrElse(
                reservation -> {
                    if(reservation.getDate().isAfter(LocalDateTime.now().plusHours(hoursToCancelReservation))) {
                        if (reservation.getVerificationCode().equals(verificationCode)) {
                            emailService.sendReservationEmail(resCancelledSubject, reservation);
                            reservationRepository.delete(reservation);
                        } else throw new GraphQLClientException(500, "/graphql", "verification code is invalid ", "reservationCancel");
                    } else throw new GraphQLClientException(500, "/graphql", "time to cancel reservation is up ", "reservationCancel");
                    },() -> { throw new GraphQLClientException(500, "/graphql", "no such reservation id found", "reservationCancel"); });
        return "OK";
    }
}
