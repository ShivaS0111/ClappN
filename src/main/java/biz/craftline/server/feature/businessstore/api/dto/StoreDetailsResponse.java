package biz.craftline.server.feature.businessstore.api.dto;


import biz.craftline.server.feature.customermanagement.domain.model.Customer;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreDetailsResponse {

    private StoreInfo info;
    private List<Employee> employeeList;
    private List<StoreOfferedServiceDTO> services;
    private List<StoreOfferedProductDTO> products;
    private List<Customer> customers;
    private List<Order> orders;
}

