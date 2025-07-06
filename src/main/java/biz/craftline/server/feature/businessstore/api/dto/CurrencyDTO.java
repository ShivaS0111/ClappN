package biz.craftline.server.feature.businessstore.api.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CurrencyDTO {
    private Long id;
    private String code;
    private String name;
    private String symbol;
}
