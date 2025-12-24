package biz.craftline.server.enums;

public enum Status {
    ACTIVE(1),
    INACTIVE(0),
    ARCHIVED(2),
    DISCONTINUED(3),
    DELETED(-1);

    private final int code;

    Status(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
