package edu.ehei.gitdock.gitdockproject.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private String url;
    private String platform;
    private String visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> memberIds;
    private ManagerDTO manager; // Ces infos proviendront de gitdock-auth

    @Data
    @Builder
    public static class ManagerDTO {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;

    }
}