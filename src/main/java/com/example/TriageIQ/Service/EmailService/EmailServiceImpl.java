package com.example.TriageIQ.Service.EmailService;

import com.example.TriageIQ.Entity.Enum.Role;
import com.example.TriageIQ.Entity.Ticket;
import com.example.TriageIQ.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void sendVerificationEmail(User user, String token) {
        String verificationLink =
                baseUrl +
                        "/api/auth/verify-email?token=" +
                        token;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(user.getEmail());

        message.setSubject(
                "Verify your TriageIQ account"
        );

        message.setText(
                "Hello " + user.getName() + ",\n\n" +

                        "Thank you for registering with TriageIQ.\n\n" +

                        "Please click the link below to verify " +
                        "your email and activate your account:\n\n" +

                        verificationLink + "\n\n" +

                        "This link will expire in " +
                        "the configured time period.\n\n" +

                        "If you did not create this account, " +
                        "you can safely ignore this email.\n\n" +

                        "Regards,\n" +
                        "TriageIQ Team"
        );

        mailSender.send(message);
    }

    @Override
    @Async
    public void sendTicketResolvedEmail(User user, Ticket ticket) {
        String ticketLink = baseUrl + "/tickets/" + ticket.getId();

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(user.getEmail());

        message.setSubject(
                "Your ticket has been resolved — #" + ticket.getId()
        );

        message.setText(
                "Hi " + user.getName() + ",\n\n" +

                        "Good news — your ticket has been marked as resolved:\n\n" +

                        "\"" + ticket.getSubject() + "\"\n\n" +

                        "View the full conversation here:\n" +
                        ticketLink + "\n\n" +

                        "If this didn't fully resolve your issue, you can reopen the ticket\n" +
                        "from that page and we'll take another look.\n\n" +

                        "Regards,\n" +
                        "TriageIQ Team"
        );

        mailSender.send(message);
    }

    @Override
    @Async
    public void sendStaffInviteEmail(User user, String temporaryPassword, Role role) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(user.getEmail());

        message.setSubject("Your TriageIQ " + role.name().toLowerCase() + " account is ready");

        message.setText(
                "Hello " + user.getName() + ",\n\n" +

                        "An account has been created for you on TriageIQ with the role: " + role + ".\n\n" +

                        "Email: " + user.getEmail() + "\n" +
                        "Temporary password: " + temporaryPassword + "\n\n" +

                        "Please log in and change this password immediately — " +
                        "you will be required to set a new one before you can do anything else.\n\n" +

                        "If you were not expecting this account, please contact your administrator.\n\n" +

                        "Regards,\n" +
                        "TriageIQ Team"
        );

        mailSender.send(message);
    }
}
