package edu.ehei.gitdock.gitdockproject.dto; // ⚠️ À adapter
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncRequestMessageDTO {
    private Long projectId;
    private String repoUrl;
    private Long userId;
    private String platform;
    private String projectName;
}