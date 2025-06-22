package biz.craftline.server.feature.businessstore.infra.config;

import biz.craftline.server.feature.businessstore.api.mapper.BusinessDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedServiceDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreServicePriceDTOMapper;
import biz.craftline.server.feature.businessstore.infra.entity.mapper.BusinessEntityMapper;
import biz.craftline.server.feature.businessstore.infra.entity.mapper.StoreEntityMapper;
import biz.craftline.server.feature.businessstore.infra.entity.mapper.StoreOfferedServiceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.entity.mapper.StoreServicePriceEntityMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusinessServicesMapperConfig {

    //@Bean BusinessDTOMapper businessDTOMapper() { return Mappers.getMapper(BusinessDTOMapper.class);}
    //@Bean BusinessEntityMapper businessEntityMapper() { return Mappers.getMapper(BusinessEntityMapper.class);}

    //@Bean StoreDTOMapper storeDTOMapper() { return Mappers.getMapper(StoreDTOMapper.class);}
    //@Bean StoreEntityMapper storeEntityMapper() { return Mappers.getMapper(StoreEntityMapper.class);}

    //@Bean StoreOfferedServiceDTOMapper storeOfferedServiceDTOMapper() { return Mappers.getMapper(StoreOfferedServiceDTOMapper.class);}
    //@Bean StoreOfferedServiceEntityMapper storeOfferedServiceEntityMapper() { return Mappers.getMapper(StoreOfferedServiceEntityMapper.class);}

    //@Bean StoreServicePriceDTOMapper storeServicePriceDTOMapper() { return Mappers.getMapper(StoreServicePriceDTOMapper.class);}
    //@Bean StoreServicePriceEntityMapper storeServicePriceEntityMapper() { return Mappers.getMapper(StoreServicePriceEntityMapper.class);}
}
