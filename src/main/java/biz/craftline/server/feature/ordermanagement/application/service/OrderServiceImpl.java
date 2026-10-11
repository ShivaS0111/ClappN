package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.couponmanagement.application.service.PromotionApplyResult;
import biz.craftline.server.feature.couponmanagement.application.service.PromotionOrderContext;
import biz.craftline.server.feature.couponmanagement.domain.service.PromotionService;
import biz.craftline.server.feature.inventorymanagement.application.enums.TransactionType;
import biz.craftline.server.feature.inventorymanagement.domain.service.ProductLotService;
import biz.craftline.server.feature.ordermanagement.api.dto.DiscountMismatchDetails;
import biz.craftline.server.feature.ordermanagement.application.enums.OrderItemStatus;
import biz.craftline.server.feature.ordermanagement.application.enums.OrderStatus;
import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderService;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderAllocatedLotEntity;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderItemEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.OrderEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.mapper.OrderItemEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderAllocatedLotRepository;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderItemRepository;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAllocationServiceImpl allocationService;
    private final OrderAllocatedLotRepository allocatedLotRepository;
    private final ProductLotService productLotService;
    private final SecurityContextService securityContextService;
    private final OrderCatalogPriceResolver orderCatalogPriceResolver;
    private final PromotionService promotionService;
    private final StoreRepository storeRepository;

    /**
     * Global fallback when place-order / store / business do not configure the flag.
     */
    @Value("${app.orders.accept-lower-client-discount-default:true}")
    private boolean acceptLowerClientDiscountDefault;

    @Override
    public List<Order> getAllOrders() {
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds == null) {
            // SYSTEM_ADMIN — unrestricted
            return repository.findAll().stream()
                    .map(OrderEntityMapper::toModel)
                    .collect(Collectors.toList());
        }
        if (accessibleStoreIds.isEmpty()) {
            return List.of();
        }
        return repository.findByStoreIdIn(accessibleStoreIds).stream()
                .map(OrderEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrdersByStoreId(Long storeId) {
        securityContextService.validateStoreAccess(storeId);
        return repository.findByStoreId(storeId).stream()
                .map(OrderEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = repository.findByCustomerId(customerId).stream()
                .map(OrderEntityMapper::toModel)
                .collect(Collectors.toList());
        // Filter by accessible stores
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds == null) {
            return orders; // SYSTEM_ADMIN
        }
        return orders.stream()
                .filter(o -> o.getStoreId() != null && accessibleStoreIds.contains(o.getStoreId()))
                .collect(Collectors.toList());
    }

    @Override
    public Order getOrder(Long id) {
        Order order = repository.findById(id)
                .map(OrderEntityMapper::toModel)
                .orElse(null);
        if (order != null) {
            securityContextService.validateStoreAccess(order.getStoreId());
        }
        return order;
    }

    public Order placeOrder1(Order order) {
        OrderEntity entity = OrderEntityMapper.toEntity(order);
        OrderEntity saved = repository.save(entity);
        return OrderEntityMapper.toModel(saved);
    }

    @Transactional
    public Order placeOrder(Order request) {
        // Validate store access
        securityContextService.validateStoreAccess(request.getStoreId());
        // basic validation
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have items");
        }

        // Capture client-claimed discounts for verification (FE may show/apply for display).
        // Never trust these blindly — recompute and reject mismatches below.
        Double clientBillDiscount = request.getBillDiscount();
        Double clientTotalDiscount = request.getTotalDiscount();
        Double clientTotalAmount = request.getTotalAmount();
        List<Double> clientLineDiscounts = new ArrayList<>();
        for (var it : request.getItems()) {
            clientLineDiscounts.add(it.getDiscountAmount());
        }

        // Resolve server prices first (client unit price ignored)
        List<PromotionOrderContext.Line> promoLines = new ArrayList<>();
        for (var it : request.getItems()) {
            if (it.getItemIId() == null) {
                throw new IllegalArgumentException("itemId is required on each order line");
            }
            if (it.getQuantity() <= 0) {
                throw new IllegalArgumentException("quantity must be positive for itemId=" + it.getItemIId());
            }
            double unitPrice = orderCatalogPriceResolver.resolveUnitPrice(request.getStoreId(), it)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No sell price configured for itemType=" + it.getItemType()
                                    + " itemId=" + it.getItemIId()
                                    + " at store=" + request.getStoreId()
                                    + ". Set store_item_price or business catalog amount."));
            it.setPrice(unitPrice);
            promoLines.add(PromotionOrderContext.Line.builder()
                    .itemType(it.getItemType())
                    .itemId(it.getItemIId())
                    .quantity(it.getQuantity())
                    .unitPrice(BigDecimal.valueOf(unitPrice))
                    .discountAmount(BigDecimal.ZERO)
                    .build());
        }

        Long userId = null;
        try {
            userId = securityContextService.getCurrentUserId();
        } catch (Exception ignored) {
            // place-order may still proceed without user-targeted promos
        }
        // couponCode from client is an input claim; eligibility + amounts are recomputed server-side
        PromotionOrderContext promoCtx = PromotionOrderContext.builder()
                .storeId(request.getStoreId())
                .userId(userId)
                .couponCode(request.getCouponCode())
                .lines(promoLines)
                .build();
        PromotionApplyResult promoResult = promotionService.evaluateForOrder(promoCtx);

        boolean acceptLower = resolveAcceptLowerClientDiscount(
                request.getAcceptLowerClientDiscount(), request.getStoreId());
        ResolvedDiscounts resolved = resolveDiscountClaims(
                request.getDiscountResolution(),
                acceptLower,
                clientBillDiscount, clientTotalDiscount, clientTotalAmount,
                clientLineDiscounts, promoResult);

        for (int i = 0; i < request.getItems().size(); i++) {
            BigDecimal lineDisc = i < resolved.lineDiscounts.size()
                    ? resolved.lineDiscounts.get(i) : BigDecimal.ZERO;
            request.getItems().get(i).setDiscountAmount(lineDisc.doubleValue());
            if (lineDisc.compareTo(BigDecimal.ZERO) > 0) {
                request.getItems().get(i).setDiscountType("PROMOTION");
            } else {
                request.getItems().get(i).setDiscountType(null);
            }
        }
        request.setSubtotal(resolved.subtotal.doubleValue());
        request.setBillDiscount(resolved.billDiscount.doubleValue());
        request.setBillDiscountType(resolved.billDiscountType);
        request.setTotalDiscount(resolved.totalDiscount.doubleValue());
        request.setCouponCode(resolved.couponCode);
        request.setTotalAmount(resolved.payable.doubleValue());

        // persist order entity (without items first to avoid cascade issues)
        var entity = OrderEntityMapper.toEntity(request);
        entity.setItems(null); // detach items — we save them manually below
        entity.setStatus(OrderStatus.CREATED.toString());
        if (entity.getOrderDate() == null) {
            entity.setOrderDate(LocalDateTime.now());
        }
        if (entity.getTotalAmount() == null) {
            entity.setTotalAmount(BigDecimal.ZERO);
        }
        entity.setSubtotal(resolved.subtotal);
        entity.setBillDiscount(resolved.billDiscount);
        entity.setBillDiscountType(resolved.billDiscountType);
        entity.setTotalDiscount(resolved.totalDiscount);
        entity.setCouponCode(resolved.couponCode);
        entity.setTotalAmount(resolved.payable);
        var saved = repository.save(entity);

        for (int i = 0; i < request.getItems().size(); i++) {
            var it = request.getItems().get(i);
            var itemEnt = OrderItemEntityMapper.toEntity(it);
            itemEnt.setOrder(saved);
            itemEnt.setPrice(it.getPrice());
            itemEnt.setDiscountAmount(it.getDiscountAmount());
            itemEnt.setDiscountType(it.getDiscountType());
            if (itemEnt.getStatus() == null) {
                itemEnt.setStatus(OrderItemStatus.PENDING);
            }
            var savedItem = orderItemRepository.save(itemEnt);

            // Allocate inventory for physical products (itemType 1 = PRODUCT)
            if (it.getItemType() != null && it.getItemType() == 1) {
                allocationService.allocate(request.getStoreId(), it.getItemIId(), it.getQuantity(), savedItem.getId());
            }
        }

        saved.setStatus(OrderStatus.BLOCKED.toString());
        repository.save(saved);

        if (promoResult.getAppliedPromotionIds() != null && !promoResult.getAppliedPromotionIds().isEmpty()
                && resolved.totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
            promotionService.redeem(
                    promoResult.getAppliedPromotionIds(),
                    saved.getId(),
                    userId,
                    request.getStoreId(),
                    resolved.totalDiscount);
        }

        return OrderEntityMapper.toDomain(saved, orderItemRepository.findByOrder_Id(saved.getId()));
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, Order order) {
        OrderEntity existing = repository.findById(id).orElse(null);
        if (existing == null) return null;
        securityContextService.validateStoreAccess(existing.getStoreId());
        if (order.getStoreId() != null) {
            securityContextService.validateStoreAccess(order.getStoreId());
        }
        OrderEntity entity = OrderEntityMapper.toEntity(order);
        entity.setId(id);
        if (entity.getStoreId() == null) {
            entity.setStoreId(existing.getStoreId());
        }
        OrderEntity saved = repository.save(entity);
        return OrderEntityMapper.toModel(saved);
    }

    @Override
    public void deleteOrder(Long id) {
        repository.findById(id).ifPresentOrElse(entity -> {
            securityContextService.validateStoreAccess(entity.getStoreId());
            repository.deleteById(id);
        }, () -> {
            // no-op if missing
        });
    }

    private static final Set<String> CANCELLABLE = Set.of(
            OrderStatus.CREATED.name(), OrderStatus.BLOCKED.name(), OrderStatus.CONFIRMED.name());
    private static final Set<String> COMPLETABLE = Set.of(
            OrderStatus.CREATED.name(), OrderStatus.BLOCKED.name(), OrderStatus.CONFIRMED.name(),
            OrderStatus.PACKED.name(), OrderStatus.SHIPPED.name());

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        OrderEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        securityContextService.validateStoreAccess(entity.getStoreId());
        String status = entity.getStatus() != null ? entity.getStatus() : "";
        if (OrderStatus.CANCELLED.name().equals(status) || OrderStatus.COMPLETED.name().equals(status)) {
            throw new IllegalStateException("Cannot cancel order in status: " + status);
        }
        if (!CANCELLABLE.contains(status) && !status.isBlank()) {
            throw new IllegalStateException("Cannot cancel order in status: " + status);
        }
        releaseAllocations(id, false);
        entity.setStatus(OrderStatus.CANCELLED.name());
        repository.save(entity);
    }

    @Override
    @Transactional
    public void completeOrder(Long id) {
        OrderEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        securityContextService.validateStoreAccess(entity.getStoreId());
        String status = entity.getStatus() != null ? entity.getStatus() : "";
        if (OrderStatus.COMPLETED.name().equals(status) || OrderStatus.CANCELLED.name().equals(status)) {
            throw new IllegalStateException("Cannot complete order in status: " + status);
        }
        if (!COMPLETABLE.contains(status) && !status.isBlank()) {
            throw new IllegalStateException("Cannot complete order in status: " + status);
        }
        releaseAllocations(id, true);
        entity.setStatus(OrderStatus.COMPLETED.name());
        repository.save(entity);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        OrderStatus parsed;
        try {
            parsed = OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid order status: " + status + ". Allowed: " + java.util.Arrays.toString(OrderStatus.values()));
        }
        if (parsed == OrderStatus.CANCELLED) {
            cancelOrder(id);
            return getOrder(id);
        }
        if (parsed == OrderStatus.COMPLETED) {
            completeOrder(id);
            return getOrder(id);
        }
        OrderEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        securityContextService.validateStoreAccess(entity.getStoreId());
        String current = entity.getStatus() != null ? entity.getStatus() : "";
        if (OrderStatus.CANCELLED.name().equals(current) || OrderStatus.COMPLETED.name().equals(current)) {
            throw new IllegalStateException("Cannot change status from terminal state: " + current);
        }
        entity.setStatus(parsed.name());
        return OrderEntityMapper.toModel(repository.save(entity));
    }

    /**
     * Priority: place-order field → store setting → business setting → global default (true).
     */
    boolean resolveAcceptLowerClientDiscount(Boolean requestFlag, Long storeId) {
        if (requestFlag != null) {
            return requestFlag;
        }
        if (storeId != null) {
            StoreEntity store = storeRepository.findById(storeId).orElse(null);
            if (store != null) {
                if (store.getAcceptLowerClientDiscount() != null) {
                    return store.getAcceptLowerClientDiscount();
                }
                if (store.getBusiness() != null && store.getBusiness().getAcceptLowerClientDiscount() != null) {
                    return store.getBusiness().getAcceptLowerClientDiscount();
                }
            }
        }
        return acceptLowerClientDiscountDefault;
    }

    /**
     * FE may apply coupons/promos for display and send claimed amounts.
     * Server recomputes entitlement. On mismatch:
     * <ul>
     *   <li>Client claimed <b>more</b> than entitled → not proceedable; must RECONFIGURE</li>
     *   <li>Client claimed <b>less</b> → proceedable; confirm APPLY_SERVER_FULL or APPLY_CLIENT_CLAIMED</li>
     * </ul>
     * Matching claims (or no claims) use server amounts.
     */
    private static ResolvedDiscounts resolveDiscountClaims(
            String discountResolution,
            boolean acceptLowerClientDiscount,
            Double clientBillDiscount,
            Double clientTotalDiscount,
            Double clientTotalAmount,
            List<Double> clientLineDiscounts,
            PromotionApplyResult server) {
        BigDecimal tol = new BigDecimal("0.02");
        boolean anyClaim = (clientBillDiscount != null && clientBillDiscount != 0.0)
                || (clientTotalDiscount != null && clientTotalDiscount != 0.0)
                || clientTotalAmount != null
                || (clientLineDiscounts != null && clientLineDiscounts.stream()
                .anyMatch(d -> d != null && d != 0.0));

        ResolvedDiscounts serverResolved = ResolvedDiscounts.fromServer(server);
        if (!anyClaim) {
            return serverResolved;
        }

        BigDecimal claimedTotalDisc = clientTotalDiscount != null
                ? BigDecimal.valueOf(clientTotalDiscount) : sumLineDiscounts(clientLineDiscounts)
                .add(clientBillDiscount != null ? BigDecimal.valueOf(clientBillDiscount) : BigDecimal.ZERO);
        BigDecimal serverTotalDisc = nz(server.getTotalDiscount());
        BigDecimal claimedPayable = clientTotalAmount != null
                ? BigDecimal.valueOf(clientTotalAmount)
                : nz(server.getSubtotal()).subtract(claimedTotalDisc).max(BigDecimal.ZERO);

        boolean amountsMatch = amountsMatchClient(clientBillDiscount, clientTotalDiscount, clientTotalAmount,
                clientLineDiscounts, server, tol);
        if (amountsMatch) {
            return serverResolved;
        }

        // Higher discount claim, or lower payable than entitled → not proceedable
        boolean clientHigherDiscount = claimedTotalDisc.subtract(serverTotalDisc).compareTo(tol) > 0;
        boolean clientLowerPayable = nz(server.getPayable()).subtract(claimedPayable).compareTo(tol) > 0;
        boolean proceedable = !clientHigherDiscount && !clientLowerPayable;

        String resolution = discountResolution != null
                ? discountResolution.trim().toUpperCase(Locale.ROOT) : null;

        // Optional one-shot: accept lower client claim without 409 acknowledge
        if (resolution == null && acceptLowerClientDiscount && proceedable) {
            resolution = DiscountMismatchDetails.OPT_APPLY_CLIENT_CLAIMED;
        }

        if (DiscountMismatchDetails.OPT_APPLY_SERVER_FULL.equals(resolution)) {
            return serverResolved;
        }
        if (DiscountMismatchDetails.OPT_APPLY_CLIENT_CLAIMED.equals(resolution)) {
            if (!proceedable) {
                throw new DiscountMismatchException(buildMismatchDetails(
                        false, clientBillDiscount, clientTotalDiscount, clientTotalAmount,
                        clientLineDiscounts, claimedTotalDisc, claimedPayable, server));
            }
            return ResolvedDiscounts.fromClientClaim(
                    server, clientBillDiscount, clientTotalDiscount, claimedPayable, clientLineDiscounts);
        }

        throw new DiscountMismatchException(buildMismatchDetails(
                proceedable, clientBillDiscount, clientTotalDiscount, clientTotalAmount,
                clientLineDiscounts, claimedTotalDisc, claimedPayable, server));
    }

    private static DiscountMismatchDetails buildMismatchDetails(
            boolean proceedable,
            Double clientBillDiscount,
            Double clientTotalDiscount,
            Double clientTotalAmount,
            List<Double> clientLineDiscounts,
            BigDecimal claimedTotalDisc,
            BigDecimal claimedPayable,
            PromotionApplyResult server) {
        DiscountMismatchDetails.AmountSnapshot clientSnap = DiscountMismatchDetails.AmountSnapshot.builder()
                .subtotal(nz(server.getSubtotal()))
                .billDiscount(clientBillDiscount != null ? BigDecimal.valueOf(clientBillDiscount) : BigDecimal.ZERO)
                .totalDiscount(claimedTotalDisc.setScale(4, RoundingMode.HALF_UP))
                .totalAmount(claimedPayable.setScale(4, RoundingMode.HALF_UP))
                .couponCode(server.getCouponCode())
                .lineDiscounts(toBigDecimals(clientLineDiscounts, server.getLineDiscounts().size()))
                .build();
        DiscountMismatchDetails.AmountSnapshot serverSnap = DiscountMismatchDetails.AmountSnapshot.builder()
                .subtotal(nz(server.getSubtotal()))
                .billDiscount(nz(server.getBillDiscount()))
                .totalDiscount(nz(server.getTotalDiscount()))
                .totalAmount(nz(server.getPayable()))
                .couponCode(server.getCouponCode())
                .lineDiscounts(server.getLineDiscounts() != null ? server.getLineDiscounts() : List.of())
                .build();

        List<DiscountMismatchDetails.ResolutionOption> options = new ArrayList<>();
        String reasonCode;
        String reason;
        if (!proceedable) {
            reasonCode = DiscountMismatchDetails.REASON_CLIENT_HIGHER;
            reason = "Client applied a higher discount (or lower payable) than entitled. "
                    + "Reconfigure promotions/cart; cannot confirm the claimed amounts.";
            options.add(DiscountMismatchDetails.ResolutionOption.builder()
                    .code(DiscountMismatchDetails.OPT_RECONFIGURE)
                    .label("Reconfigure")
                    .description("Adjust coupon/items so claimed discount does not exceed server entitlement, then place again.")
                    .build());
        } else {
            reasonCode = DiscountMismatchDetails.REASON_CLIENT_LOWER;
            reason = "Client applied a lower discount than entitled (store discretion). "
                    + "Confirm full entitled discount or keep the lower applied amount.";
            options.add(DiscountMismatchDetails.ResolutionOption.builder()
                    .code(DiscountMismatchDetails.OPT_APPLY_SERVER_FULL)
                    .label("Apply full entitled discount")
                    .description("Use server-computed discount: totalDiscount=" + serverSnap.getTotalDiscount()
                            + ", payable=" + serverSnap.getTotalAmount())
                    .build());
            options.add(DiscountMismatchDetails.ResolutionOption.builder()
                    .code(DiscountMismatchDetails.OPT_APPLY_CLIENT_CLAIMED)
                    .label("Keep applied (lower) discount")
                    .description("Proceed with client-claimed amounts (store discretion): totalDiscount="
                            + clientSnap.getTotalDiscount() + ", payable=" + clientSnap.getTotalAmount())
                    .build());
        }

        return DiscountMismatchDetails.builder()
                .proceedable(proceedable)
                .reasonCode(reasonCode)
                .reason(reason)
                .client(clientSnap)
                .server(serverSnap)
                .options(options)
                .build();
    }

    private static boolean amountsMatchClient(
            Double clientBillDiscount,
            Double clientTotalDiscount,
            Double clientTotalAmount,
            List<Double> clientLineDiscounts,
            PromotionApplyResult server,
            BigDecimal tol) {
        if (clientBillDiscount != null
                && !moneyEquals(BigDecimal.valueOf(clientBillDiscount), server.getBillDiscount(), tol)) {
            return false;
        }
        if (clientTotalDiscount != null
                && !moneyEquals(BigDecimal.valueOf(clientTotalDiscount), server.getTotalDiscount(), tol)) {
            return false;
        }
        if (clientTotalAmount != null
                && !moneyEquals(BigDecimal.valueOf(clientTotalAmount), server.getPayable(), tol)) {
            return false;
        }
        if (clientLineDiscounts != null) {
            for (int i = 0; i < clientLineDiscounts.size(); i++) {
                Double claimed = clientLineDiscounts.get(i);
                if (claimed == null) {
                    continue;
                }
                BigDecimal serverLine = i < server.getLineDiscounts().size()
                        ? server.getLineDiscounts().get(i) : BigDecimal.ZERO;
                if (!moneyEquals(BigDecimal.valueOf(claimed), serverLine, tol)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean moneyEquals(BigDecimal a, BigDecimal b, BigDecimal tol) {
        return nz(a).subtract(nz(b)).abs().compareTo(tol) <= 0;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static BigDecimal sumLineDiscounts(List<Double> lines) {
        if (lines == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (Double d : lines) {
            if (d != null) {
                sum = sum.add(BigDecimal.valueOf(d));
            }
        }
        return sum;
    }

    private static List<BigDecimal> toBigDecimals(List<Double> clientLines, int size) {
        List<BigDecimal> out = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Double v = clientLines != null && i < clientLines.size() ? clientLines.get(i) : null;
            out.add(v != null ? BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        }
        return out;
    }

    private static final class ResolvedDiscounts {
        final BigDecimal subtotal;
        final BigDecimal billDiscount;
        final String billDiscountType;
        final BigDecimal totalDiscount;
        final String couponCode;
        final BigDecimal payable;
        final List<BigDecimal> lineDiscounts;

        private ResolvedDiscounts(BigDecimal subtotal, BigDecimal billDiscount, String billDiscountType,
                                  BigDecimal totalDiscount, String couponCode, BigDecimal payable,
                                  List<BigDecimal> lineDiscounts) {
            this.subtotal = subtotal;
            this.billDiscount = billDiscount;
            this.billDiscountType = billDiscountType;
            this.totalDiscount = totalDiscount;
            this.couponCode = couponCode;
            this.payable = payable;
            this.lineDiscounts = lineDiscounts;
        }

        static ResolvedDiscounts fromServer(PromotionApplyResult server) {
            return new ResolvedDiscounts(
                    nz(server.getSubtotal()),
                    nz(server.getBillDiscount()),
                    server.getBillDiscountType(),
                    nz(server.getTotalDiscount()),
                    server.getCouponCode(),
                    nz(server.getPayable()),
                    server.getLineDiscounts() != null ? server.getLineDiscounts() : List.of());
        }

        static ResolvedDiscounts fromClientClaim(
                PromotionApplyResult server,
                Double clientBillDiscount,
                Double clientTotalDiscount,
                BigDecimal claimedPayable,
                List<Double> clientLineDiscounts) {
            List<BigDecimal> lines = toBigDecimals(clientLineDiscounts,
                    server.getLineDiscounts() != null ? server.getLineDiscounts().size() : 0);
            BigDecimal bill = clientBillDiscount != null
                    ? BigDecimal.valueOf(clientBillDiscount) : BigDecimal.ZERO;
            BigDecimal totalDisc = clientTotalDiscount != null
                    ? BigDecimal.valueOf(clientTotalDiscount)
                    : sumLineDiscounts(clientLineDiscounts).add(bill);
            return new ResolvedDiscounts(
                    nz(server.getSubtotal()),
                    bill.setScale(4, RoundingMode.HALF_UP),
                    server.getBillDiscountType(),
                    totalDisc.setScale(4, RoundingMode.HALF_UP),
                    server.getCouponCode(),
                    claimedPayable.setScale(4, RoundingMode.HALF_UP),
                    lines);
        }
    }

    /** confirmSale=true converts blocked stock to sold; false only unblocks (cancel). */
    private void releaseAllocations(Long orderId, boolean confirmSale) {
        List<OrderItemEntity> items = orderItemRepository.findByOrder_Id(orderId);
        if (items.isEmpty()) {
            return;
        }
        List<Long> itemIds = items.stream().map(OrderItemEntity::getId).toList();
        List<OrderAllocatedLotEntity> allocations = allocatedLotRepository.findByOrderItemIdIn(itemIds);
        for (OrderAllocatedLotEntity alloc : allocations) {
            int qty = alloc.getAllocatedQuantity();
            productLotService.recordTransaction(
                    alloc.getProductLotId(), TransactionType.UNBLOCK, qty,
                    confirmSale ? "ORDER_COMPLETE_UNBLOCK" : "ORDER_CANCEL_UNBLOCK",
                    String.valueOf(orderId), 0L);
            if (confirmSale) {
                productLotService.recordTransaction(
                        alloc.getProductLotId(), TransactionType.SOLD, qty,
                        "ORDER_COMPLETE_SOLD", String.valueOf(orderId), 0L);
            }
        }
    }
}
