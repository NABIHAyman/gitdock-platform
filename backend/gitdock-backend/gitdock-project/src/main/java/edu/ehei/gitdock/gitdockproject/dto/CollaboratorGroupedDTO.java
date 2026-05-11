package edu.ehei.gitdock.gitdockproject.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CollaboratorGroupedDTO {
    private Long projectId;
    private String projectName;
    private List<CollaboratorDTO> collaborators;

    @Data
    @Builder
    public static class CollaboratorDTO {
        private Long id;
        private String firstName; // Info venant de gitdock-auth
        private String lastName;  // Info venant de gitdock-auth
        private String email;     // Info venant de gitdock-auth
        private String role;      // Info venant de gitdock-auth
        private String status;    // Info venant de gitdock-auth
        public String getFullName() {
            return firstName + " " + lastName;
        }
    }
}