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
        List<RestaurantTable> restaurantTableList;
        restaurantTableList = restaurantTableRepository
                .findAll()
                .stream()
                .filter( restaurantTable -> restaurantTable.getMinNumberOfSeats() <= numberOfSeats)
                .filter( restaurantTable -> restaurantTable.getMaxNumberOfSeats() >= numberOfSeats)
                .filter( restaurantTable -> filterByStatus(restaurantTable, status, date, duration))
                .collect(Collectors.toList());

        if(page != null)
            restaurantTableList = restaurantTableList
                    .stream()
                    .skip(page.getPageNumber() * page.getMaxRowsNumber())
                    .limit(page.getMaxRowsNumber())
                    .collect(Collectors.toList());
        return restaurantTableList;
    }

    public RestaurantTable findFirstFreeTable(int numberOfSeats, LocalDateTime date, int duration)
    throws GraphQLClientException{
            return getRestaurantTables(null, numberOfSeats, date.toString(), duration, "free")
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> { throw new GraphQLClientException(500, "/graphql", "no free table found", "req"); });
    }

    private boolean filterByStatus(RestaurantTable restaurantTable, String status, String date, int duration)
            throws GraphQLClientException{
        switch (status){
            case "free":
                return restaurantTable
                        .getReservations()
                        .stream().noneMatch(
                                reservation -> predicateForReservation(reservation, date, duration));
            case "taken":
                return restaurantTable
                        .getReservations()
                        .stream().anyMatch(
                                reservation -> predicateForReservation(reservation, date, duration));
            case "all":
                return true;
            default:
                throw new GraphQLClientException(500, "/graphql", "there is no such status available", "req");
        }
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
