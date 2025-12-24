package biz.craftline.server.feature.businesstype.api.request;

public record SearchServiceByBusinessRequest (
    String keyword,
    long business_type_id){
}
