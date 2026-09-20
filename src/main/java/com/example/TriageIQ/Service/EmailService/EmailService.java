package com.example.TriageIQ.Service.EmailService;

import com.example.TriageIQ.Entity.Ticket;
import com.example.TriageIQ.Entity.User;
import com.example.TriageIQ.Entity.Enum.Role;

public interface EmailService {
    void sendVerificationEmail(User user, String token);

    void sendTicketResolvedEmail(User user, Ticket ticket);

    void sendStaffInviteEmail(User user, String temporaryPassword, Role role);
}
