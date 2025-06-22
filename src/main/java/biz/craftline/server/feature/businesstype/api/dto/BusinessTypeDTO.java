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
public class BusinessTypeDTO {
     Long id;
     String businessName;
     String description;
     int status;
     List<BusinessServiceDTO> services;



}
