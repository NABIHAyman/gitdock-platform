package edu.ehei.gitdock.gitdocksync.dto; // ⚠️ À adapter
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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