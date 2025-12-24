package biz.craftline.server.feature.usermanagement.domain.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Permission {
    private Long id;
    private String name;
    private String description;

    private String resourceType;

    private String action;

    public Permission(String name) {
        this.name = name;
    }

    public Permission(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
