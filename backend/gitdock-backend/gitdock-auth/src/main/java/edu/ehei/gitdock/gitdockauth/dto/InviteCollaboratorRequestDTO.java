package edu.ehei.gitdock.gitdockauth.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteCollaboratorRequestDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String projectName; // Pour personnaliser le mail
    private Long companyId;     // On assigne le gars à la même compagnie que le projet !
}