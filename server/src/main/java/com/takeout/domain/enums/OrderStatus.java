package com.takeout.domain.enums;

import java.util.Arrays;

public enum OrderStatus {

    UNPAID(10, "待支付"),
    PAID_WAIT_ACCEPT(20, "已支付待接单"),
    ACCEPTED(30, "商家已接单"),
    COOKING(40, "制作中"),
    DELIVERING(50, "配送中"),
    COMPLETED(60, "已完成"),
    CANCELLED(70, "已取消"),
    REFUNDING(80, "退款中"),
    REFUNDED(90, "已退款");

    private final int code;
    private final String text;

    OrderStatus(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static OrderStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported order status: " + code));
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
