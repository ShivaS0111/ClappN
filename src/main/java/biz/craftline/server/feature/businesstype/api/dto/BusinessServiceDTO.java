package biz.craftline.server.feature.businesstype.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BusinessServiceDTO {
    private Long id;

    private String name;

    private String desc;

    private Integer status;

    private Long businessId;

    private BusinessTypeDTO businessType;

    /** Optional business default price; omit/null if unset. Store may override. */
    private Float amount;

    private Long currency;

    private List<CategoryDTO> categories;
    private Long duration;//in minutes

    private String thumbnailUrl;

    private String galleryUrls;

}
