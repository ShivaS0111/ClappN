package biz.craftline.server.feature.businessstore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for store dashboard metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreMetricsDTO {
    
    private double todayRevenue;
    private int todayOrders;
    private int activeCustomers;
    private int totalProducts;
    private int lowStockItems;
    private int pendingOrders;
    private double monthlyRevenue;
    private double monthlyGrowth;
    private int totalEmployees;
}

