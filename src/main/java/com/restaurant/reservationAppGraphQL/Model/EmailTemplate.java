package com.restaurant.reservationAppGraphQL.Model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class EmailTemplate {
    @Id
    private String name;
    private String content;
}
