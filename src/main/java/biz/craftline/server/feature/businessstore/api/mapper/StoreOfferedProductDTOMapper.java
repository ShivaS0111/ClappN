package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedProductDTO;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreOfferedProductRequest;
import biz.craftline.server.feature.businessstore.domain.model.Currency;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import org.springframework.stereotype.Component;

@Component
public class StoreOfferedProductDTOMapper {
    public StoreOfferedProductDTO toDTO(StoreOfferedProduct product) {
        StoreOfferedProductDTO dto = new StoreOfferedProductDTO();
        dto.setId(product.getId());
        dto.setStoreId(product.getStoreId());
        dto.setProductId(product.getProduct().getId());
        dto.setProductName(product.getAliasName());
        dto.setDescription(product.getDescription());
        // Defensive: check for null price/currency
        if (product.getPrice() != null && product.getPrice().getCurrency() != null) {
            dto.setCurrency(product.getPrice().getCurrency().getCode());
        }
        dto.setPrice(product.getPrice() != null ? product.getPrice().getPrice() : null);
        dto.setStatus(product.getStatus());
        // Map other fields as needed
        return dto;
    }

    public StoreOfferedProduct toDomain(AddNewStoreOfferedProductRequest req) {
        StoreOfferedProduct product = new StoreOfferedProduct();
        product.setStoreId(req.getStoreId());
        product.setAliasName(req.getProductName());
        product.setDescription(req.getDescription());

        // Create and set BusinessProduct
        BusinessProduct businessProduct = new BusinessProduct();
        businessProduct.setId(req.getProductId());
        businessProduct.setName(req.getProductName());
        businessProduct.setDescription(req.getDescription());
        product.setProduct(businessProduct);

        // Create and set StoreItemPrice
        StoreItemPrice price = new StoreItemPrice();
        price.setPrice(req.getPrice());
        // Defensive: set only code if available
        Currency currency = new Currency();
        currency.setCode(req.getCurrency());
        price.setCurrency(currency);
        // lotId will be set after persistence, if needed
        product.setPrice(price);

        product.setStatus(req.getStatus());

        // Map other fields as needed
        return product;
    }
}
