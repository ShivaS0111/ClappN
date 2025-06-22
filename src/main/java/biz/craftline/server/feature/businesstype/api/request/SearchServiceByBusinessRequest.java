package biz.craftline.server.feature.businesstype.api.request;

import lombok.Builder;
import lombok.Data;

public record SearchServiceByBusinessRequest (
    String keyword,
    long business_id){
}
