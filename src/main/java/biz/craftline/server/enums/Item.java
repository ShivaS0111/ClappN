package biz.craftline.server.enums;

public enum Item {
    // keep explicit numeric mapping consistent with store_item_price.item_type usage
    // SERVICE uses type=1, ProductLot (lot) uses type=2
    PRODUCT(3L, "Product"),
    SERVICE(1L, "Service"),
    ProductLot(2L, "ProductLot"),
    PACKAGE(4L, "PACKAGE");

    private final Long type;
    private final String name;

    Item(Long type, String name) {
        this.type = type;
        this.name = name;
    }

    public Long getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
