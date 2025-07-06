package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.CurrencyDTO;
import biz.craftline.server.feature.businessstore.domain.model.Currency;
import org.springframework.stereotype.Component;

@Component
public class CurrencyMapper {

    Currency toDomain( CurrencyDTO dto){
        if (dto == null) {
            return null;
        }
        return Currency.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .name(dto.getName())
                .symbol(dto.getSymbol())
                .build();
    }

    CurrencyDTO toDTO(Currency currency){
        if (currency == null) {
            return null;
        }
        return CurrencyDTO.builder()
                .id(currency.getId())
                .code(currency.getCode())
                .name(currency.getName())
                .symbol(currency.getSymbol())
                .build();
    }
}
