package com.restaurant.reservationAppGraphQL;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ReservationAppGraphQlApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationAppGraphQlApplication.class, args);
	}

}
