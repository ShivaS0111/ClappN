package biz.craftline.server.feature.usermanagement.domain.model;


import lombok.Data;

@Data
public class Permission {
    private Long id;
    private String name;
    private String description;
    // Getters and setters
}

