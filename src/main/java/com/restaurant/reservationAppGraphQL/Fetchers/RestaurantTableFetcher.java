package com.restaurant.reservationAppGraphQL.Fetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.client.GraphQLClientException;
import com.restaurant.reservationAppGraphQL.Model.Page;
import com.restaurant.reservationAppGraphQL.Model.Reservation;
import com.restaurant.reservationAppGraphQL.Model.RestaurantTable;
import com.restaurant.reservationAppGraphQL.Repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DgsComponent
@RequiredArgsConstructor
public class RestaurantTableFetcher {

    private final RestaurantTableRepository restaurantTableRepository;

    @DgsQuery
    public List<RestaurantTable> getRestaurantTables(@InputArgument Page page,
                                                     @InputArgument int numberOfSeats,
                                                     @InputArgument String date,
                                                     @InputArgument int duration,
                                                     @InputArgument String status){
        Stream<RestaurantTable> tableStream;
        tableStream = restaurantTableRepository
                .findAll()
                .stream()
                .filter( restaurantTable -> restaurantTable.getMinNumberOfSeats() >= numberOfSeats)
                .filter( restaurantTable -> restaurantTable.getMaxNumberOfSeats() <= numberOfSeats);
        switch (status){
            case "free":
                tableStream = tableStream.filter(
                        restaurantTable ->
                            restaurantTable
                                    .getReservations()
                                    .stream().noneMatch(
                                            reservation -> predicateForReservation(reservation, date, duration))
                        );
                break;
            case "taken":
                tableStream = tableStream.filter(
                        restaurantTable ->
                                restaurantTable
                                        .getReservations()
                                        .stream().anyMatch(
                                        reservation -> predicateForReservation(reservation, date, duration))
                );
            case "all":
                break;
            default:
                throw new GraphQLClientException(500, "/graphql", "there is no such status available", "req");
        }
        if(page != null)
            tableStream = tableStream
                    .skip(page.getPageNumber() * page.getMaxRowsNumber())
                    .limit(page.getMaxRowsNumber());

        return tableStream.collect(Collectors.toList());
    }

    private boolean predicateForReservation(Reservation reservation, String localDate, int duration)
    throws GraphQLClientException{
        try {
            return reservation
                    .getDate()
                    .plusMinutes(reservation.getDuration())
                    .isAfter(LocalDateTime.parse(localDate))
                    | reservation
                    .getDate()
                    .isBefore(LocalDateTime
                            .parse(localDate)
                            .plusMinutes(duration));
        } catch (DateTimeParseException ex){
            throw new GraphQLClientException(500, "/graphql", "error while parsing date " + ex.getMessage(), "req");
        }
    }
}
