package com.restaurant.reservationAppGraphQL.Model;

import lombok.Data;

@Data
public class Page {
    private final int pageNumber;
    private final int maxRowsNumber;
}
