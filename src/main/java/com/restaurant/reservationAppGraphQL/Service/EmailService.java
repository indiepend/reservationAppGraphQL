package com.restaurant.reservationAppGraphQL.Service;

import com.restaurant.reservationAppGraphQL.Model.Reservation;
import com.restaurant.reservationAppGraphQL.Repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailTemplateRepository emailTemplateRepository;

    @Value("${spring.mail.variable-replacer.datetime-format}")
    private String datetimeFormat;

    @Value("${spring.mail.from}")
    private String sendMailFrom;

    @Value("${spring.mail.html.enabled}")
    private boolean htmlEnabled;

    @Async
    public void sendReservationEmail(String subject, Reservation reservation){
        emailTemplateRepository.findById(subject)
                .ifPresent(emailTemplate -> {
                    try{
                        String content = reservationVariableReplacer(emailTemplate.getContent(), reservation);
                        MimeMessage mimeMessage = mailSender.createMimeMessage();
                        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "utf-8");
                        message.setFrom(sendMailFrom);
                        message.setTo(reservation.getEmail());
                        message.setSubject(subject);
                        message.setText(content, htmlEnabled);
                        mailSender.send(mimeMessage);
                    }catch(MessagingException ex){
                        log.error("Error in method [EmailService.sendEmail] " + ex.getMessage());
                    }
                });
    }

    private String reservationVariableReplacer(String content, Reservation reservation){
        return content
                .replaceAll("\\{\\{FULLNAME}}", reservation.getFullName())
                .replaceAll("\\{\\{ID}}", reservation.getID().toString())
                .replaceAll("\\{\\{PHONE}}", reservation.getPhone())
                .replaceAll("\\{\\{TIME}}",
                        reservation.getDate().format(
                                DateTimeFormatter
                                        .ofPattern(datetimeFormat)))
                .replaceAll("\\{\\{TABLE_ID}}", reservation.getRestaurantTable().getNumber().toString())
                .replaceAll("\\{\\{VERIFICATION_CODE}}", reservation.getVerificationCode());
    }
}
