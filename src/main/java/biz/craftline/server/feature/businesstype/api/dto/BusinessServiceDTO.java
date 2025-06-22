package biz.craftline.server.feature.businesstype.api.dto;

import biz.craftline.server.feature.businesstype.domain.model.BusinessService;

public class BusinessServiceDTO {
    private Long id;

    private String name;

    private String desc;

    private int status;

    private long businessType;

    private float amount;

    private Long currency;

    public BusinessServiceDTO(Long id, String name, String desc, int status, long businessType, float amount, Long currency) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.status = status;
        this.businessType = businessType;
        this.amount = amount;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getBusinessType() {
        return businessType;
    }

    public void setBusinessType(long businessType) {
        this.businessType = businessType;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public Long getCurrency() {
        return currency;
    }

    public void setCurrency(Long currency) {
        this.currency = currency;
    }

    public BusinessService toDomain(){
        return new BusinessService( id, name, desc, status, businessType, amount, currency );
    }

    public static BusinessServiceDTO toDTO(BusinessService service){
        return new BusinessServiceDTO(
                service.getId(),
                service.getServiceName(),
                service.getDescription(),
                service.getStatus(),
                service.getBusinessType(),
                service.getAmount(),
                service.getCurrency()
        );
    }

}
