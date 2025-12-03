package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.RequestStatus;
import com.commander.aqm.aqm_back_end.model.SupportRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SupportTicketDto {
    private Long id;
    private String username;
    private String email;
    private String subject;
    private String message;
    private RequestStatus status;
    private String adminReply;
    private LocalDateTime submittedAt;

    public static SupportTicketDto from(SupportRequest ticket) {
        return SupportTicketDto.builder()
                .id(ticket.getId())
                .username(ticket.getUser().getUsername())
                .email(ticket.getUser().getEmail())
                .subject(ticket.getSubject())
                .message(ticket.getMessage())
                .status(ticket.getStatus())
                .adminReply(ticket.getAdminReply())
                .submittedAt(ticket.getSubmittedAt())
                .build();
    }
}