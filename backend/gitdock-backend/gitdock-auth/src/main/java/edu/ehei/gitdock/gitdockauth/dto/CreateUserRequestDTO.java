package edu.ehei.gitdock.gitdockauth.dto;

import lombok.Data;

@Data
public class CreateUserRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private Long projectId;

}
