package biz.craftline.server.feature.businesstype.api.request;


public class AddNewBusinessTypeRequest {
    String businessName;
    String description;

    public AddNewBusinessTypeRequest(String businessName, String description) {
        this.businessName = businessName;
        this.description = description;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
