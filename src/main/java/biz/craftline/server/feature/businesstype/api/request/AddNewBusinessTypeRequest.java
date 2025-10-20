package biz.craftline.server.feature.businesstype.api.request;


import lombok.Data;

@Data
public class AddNewBusinessTypeRequest {
    String businessName;
    String description;
    int status;

    public AddNewBusinessTypeRequest(String businessName, String description) {
        this.businessName = businessName;
        this.description = description;
    }

}
