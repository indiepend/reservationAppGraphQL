package com.restaurant.reservationAppGraphQL.Repository;

import com.restaurant.reservationAppGraphQL.Model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, String> {
}
