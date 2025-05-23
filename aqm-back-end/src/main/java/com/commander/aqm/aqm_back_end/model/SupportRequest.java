package com.commander.aqm.aqm_back_end.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    private String subject;
    private String message;

    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private String adminReply;
}
