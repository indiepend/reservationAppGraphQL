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
import com.restaurant.reservationAppGraphQL.Service.ReservationValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DgsComponent
@RequiredArgsConstructor
public class ReservationFetcher {

    private final ReservationRepository reservationRepository;
    private final EmailService emailService;
    private final RestaurantTableFetcher restaurantTableFetcher;

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
        Stream<Reservation> reservationStream;
        try {
            reservationStream = reservationRepository
                    .findAll()
                    .stream()
                    .filter(
                            reservation ->
                                    reservation
                                            .getDate().toLocalDate()
                                            .equals(LocalDate.parse(date)));
        } catch(DateTimeParseException ex){
            throw new GraphQLClientException(500, "/graphql", "error while parsing date " + ex.getMessage(), "req");
        }
        if(page != null)
            reservationStream = reservationStream
                    .skip(page.getPageNumber() * page.getMaxRowsNumber())
                    .limit(page.getMaxRowsNumber());
        return reservationStream.collect(Collectors.toList());
    }

    @DgsMutation
    public Reservation newReservation(@InputArgument Reservation reservation){
        if(!ReservationValidatorService.validate(reservation))
            throw new GraphQLClientException(500, "/graphql", "validation error ", "req");
        RestaurantTable freeTable = restaurantTableFetcher.findFirstFreeTable(
                reservation.getNumberOfSeats(),
                reservation.getDate(),
                reservation.getDuration()
        );
        reservation.setRestaurantTable(freeTable);
        freeTable.addReservation(reservation);
        reservationRepository.saveAndFlush(reservation);
        emailService.sendReservationEmail(newReservationSubject, reservation);
        return reservation;
    }

    @DgsMutation(field = "reservationCancelRequest")
    public String reservationCancelReq(@InputArgument Long id, @InputArgument String status){
        reservationRepository.findById(id).ifPresentOrElse(
                reservation -> {
                    if(reservation.getDate().isBefore(LocalDateTime.now().minusHours(hoursToCancelReservation))){
                            if(status.equals("requested cancellation")) {
                                String verificationCode = String.format("%06d", Math.round(Math.random() * 1000000));
                                reservation.setVerificationCode(verificationCode);
                                reservationRepository.saveAndFlush(reservation);
                                emailService.sendReservationEmail(cancelReqSubject, reservation);
                            } else throw new GraphQLClientException(500, "/graphql", "required status is invalid", "req");
                    } else throw new GraphQLClientException(500, "/graphql", "time to cancel reservation is up ", "req");
                }, () -> { throw new GraphQLClientException(500, "/graphql", "no such reservation id found", "req"); });
        return "OK";
    }

    @DgsMutation
    public String reservationCancel(@InputArgument Long id, @InputArgument String verificationCode){
        reservationRepository.findById(id).ifPresentOrElse(
                reservation -> {
                    if(reservation.getDate().isBefore(LocalDateTime.now().minusHours(hoursToCancelReservation))) {
                        if (reservation.getVerificationCode().equals(verificationCode)) {
                            emailService.sendReservationEmail(resCancelledSubject, reservation);
                            reservationRepository.delete(reservation);
                        } else throw new GraphQLClientException(500, "/graphql", "verification code is invalid ", "req");
                    } else throw new GraphQLClientException(500, "/graphql", "time to cancel reservation is up ", "req");
                    },() -> { throw new GraphQLClientException(500, "/graphql", "no such reservation id found", "req"); });
        return "OK";
    }
}
