package edu.ehei.gitdock.gitdockproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepoInitEvent {
    private Long projectId;
    private String repoUrl;
}