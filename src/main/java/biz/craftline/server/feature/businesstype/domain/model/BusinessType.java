package biz.craftline.server.feature.businesstype.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BusinessType{

     Long id;

     String businessName;

     String description;

     int status ;

    java.util.List<BusinessService> services;
}
