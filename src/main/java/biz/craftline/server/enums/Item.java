package biz.craftline.server.enums;

public enum Item {
    PRODUCT(1L, "Product"),
    SERVICE(2L, "Service"),
    ProductLot(3L, "ProductLot"),
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
