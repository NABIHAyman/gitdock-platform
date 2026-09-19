package edu.ehei.gitdock.gitdockproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO allégé pour recevoir uniquement les infos publiques d'un utilisateur
 * depuis le microservice gitdock-auth.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    @JsonProperty
    private boolean isEnabled;
}