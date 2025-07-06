package biz.craftline.server.feature.businessstore.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BusinessDTO {

    private Long id;

    private String businessName;

    private String description;

    private int status;

    private long createdBy;

    private Set<StoreDTO> stores = new HashSet<>();

}
