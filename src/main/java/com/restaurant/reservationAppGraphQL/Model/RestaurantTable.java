package com.restaurant.reservationAppGraphQL.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Data
@Entity
public class RestaurantTable {
    @Id
    private Integer number;
    private int minNumberOfSeats;
    private int maxNumberOfSeats;
    @OneToMany(mappedBy = "restaurantTable")
    @JsonIgnore
    private List<Reservation> reservations;

    public void addReservation(Reservation reservation){
        reservations.add(reservation);
    }
}
