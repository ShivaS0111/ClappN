package biz.craftline.server.feature.businessstore.infra.mapper;

import biz.craftline.server.feature.businessstore.domain.model.Currency;
import biz.craftline.server.feature.businessstore.infra.entity.CurrencyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


@Component
public class CurrencyEntityMapper {

    @Lazy
    @Autowired
    StoreEntityMapper storeEntityMapper;

    public Currency toDomain(CurrencyEntity entity){
        if (entity == null) {
            return null;
        }
        Currency.CurrencyBuilder builder = Currency.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .symbol(entity.getSymbol());

        return builder.build();
    }

    public CurrencyEntity toEntity(Currency currency){
        if (currency == null) {
            return null;
        }

        CurrencyEntity.CurrencyEntityBuilder builder = CurrencyEntity.builder()
                .code(currency.getCode())
                .name(currency.getName())
                .symbol(currency.getSymbol());

        if (currency.getId() != null) {
            builder.id(currency.getId());
        }

        return builder.build();
    }
}
