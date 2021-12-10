package com.restaurant.reservationAppGraphQL.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

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
    private String externalId;
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
}
