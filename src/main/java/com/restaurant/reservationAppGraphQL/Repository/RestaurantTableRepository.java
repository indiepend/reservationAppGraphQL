package com.restaurant.reservationAppGraphQL.Repository;

import com.restaurant.reservationAppGraphQL.Model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Integer> {
}
