package edu.ehei.gitdock.gitdockproject.dto;

import lombok.Data;

@Data
public class CreateProjectRequestDTO {
    private String name;
    private String description;
    private String url;
    private String platform;
    private String visibility;
}
