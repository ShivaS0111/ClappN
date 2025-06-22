package biz.craftline.server.feature.businesstype.infra.config;

import biz.craftline.server.feature.businesstype.api.mapper.BusinessServiceDTOMapper;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessTypeDTOMapper;
import biz.craftline.server.feature.businesstype.infra.entity.mapper.BusinessServiceEntityMapper;
import biz.craftline.server.feature.businesstype.infra.entity.mapper.BusinessTypeEntityMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusinessTypeMapperConfig {

    @Bean
    BusinessServiceDTOMapper businessServiceDTOMapper() { return new BusinessServiceDTOMapper();}

    @Bean
    BusinessServiceEntityMapper businessServiceEntityMapper() { return new BusinessServiceEntityMapper();}

    @Bean
    BusinessTypeDTOMapper businessTypeDTOMapper() { return new BusinessTypeDTOMapper(); }

    @Bean
    BusinessTypeEntityMapper businessTypeEntityMapper() { return new BusinessTypeEntityMapper(); }
}
