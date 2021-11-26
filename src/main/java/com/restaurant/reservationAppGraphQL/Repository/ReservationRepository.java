package com.restaurant.reservationAppGraphQL.Repository;

import com.restaurant.reservationAppGraphQL.Model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
