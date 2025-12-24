package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.BrandDTO;
import biz.craftline.server.feature.businesstype.domain.model.Brand;
import org.springframework.stereotype.Component;


@Component
public class BrandDToMapper {

    public BrandDTO toDTO(Brand source) {
        if (source == null) {
            return null;
        }
        BrandDTO dest = new BrandDTO();
        dest.setId(source.getId());
        dest.setName(source.getName());
        dest.setDescription(source.getDescription());
        return dest;

    }

    public Brand toDomain(BrandDTO source) {
        if (source == null) {
            return null;
        }
        Brand dest = new Brand();
        dest.setId(source.getId());
        dest.setName(source.getName());
        dest.setDescription(source.getDescription());
        return dest;
    }
}
