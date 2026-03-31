package biz.craftline.server.feature.usermanagement.api.dto;

import lombok.Data;

@Data
public class PermissionRequest {
    private String name;
    private String description;
    private String category;
    private String type;
    private String level;
    // Getters and setters
}

