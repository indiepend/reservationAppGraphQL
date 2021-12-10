package com.restaurant.reservationAppGraphQL.Repository;

import com.restaurant.reservationAppGraphQL.Model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    Optional<Reservation> findByExternalId(String externalId);
}
