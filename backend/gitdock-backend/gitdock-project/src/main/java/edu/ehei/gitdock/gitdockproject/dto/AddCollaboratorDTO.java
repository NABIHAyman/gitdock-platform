package edu.ehei.gitdock.gitdockproject.dto;
import lombok.Data;

@Data
public class AddCollaboratorDTO {
    private String email;
    private String role;
    private String firstName;
    private String lastName;
}