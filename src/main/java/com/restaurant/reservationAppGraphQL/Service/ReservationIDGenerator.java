package com.restaurant.reservationAppGraphQL.Service;

import com.restaurant.reservationAppGraphQL.Repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ReservationIDGenerator {
    private final ReservationRepository reservationRepository;

    public String generate() {
        int idAsNumber;
        String finalId;
        do {
            idAsNumber = (int)Math.round(Math.random() * 1000000000 + 100000000);
            finalId = Base64.getUrlEncoder().encodeToString(Long.toString(idAsNumber).getBytes());
        } while (reservationRepository.findById(idAsNumber).isPresent());
        return finalId;
    }
}
