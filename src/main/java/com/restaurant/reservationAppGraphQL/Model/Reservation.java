package com.restaurant.reservationAppGraphQL.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Data
public class Reservation {
    @Id
    @GeneratedValue
    private Integer ID;
    private LocalDateTime date;
    private int duration;
    private String fullName;
    private String phone;
    private String email;
    private int numberOfSeats;

    @JsonIgnore
    private String verificationCode;
    @ManyToOne
    @JoinColumn(name = "table_number")
    @JsonIgnore
    private RestaurantTable restaurantTable;

    @JsonProperty(value = "seatsNumber")
    int getSeatsNumber(){
        return restaurantTable.getNumber();
    }

    public static Boolean validate(final Reservation reservation){
        return reservation.date.isAfter(LocalDateTime.now())
                & reservation.duration <= 360
                & reservation.duration >= 30
                & reservation.fullName.matches("[A-Za-z]{3,30} [A-Za-z]{3,30}")
                & reservation.phone.matches("[0-9]{9}")
                & reservation.email.matches("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")
                & reservation.email.length() < 255
                & reservation.numberOfSeats > 0;
    }
}
