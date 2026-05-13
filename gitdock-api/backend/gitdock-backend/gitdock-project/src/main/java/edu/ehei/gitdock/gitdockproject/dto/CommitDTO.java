package edu.ehei.gitdock.gitdockproject.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import java.io.Serializable;

@Data
@Builder
public class CommitDTO implements Serializable {
    private Long id;
    private String hash;
    private String message;
    private String authorName;
    private LocalDateTime date;

    private Integer additions;
    private Integer deletions;
}


