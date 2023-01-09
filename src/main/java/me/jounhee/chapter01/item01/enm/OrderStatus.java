package me.jounhee.chapter01.item01.enm;

public enum OrderStatus {

    PREPARING(0), SHIPPED(1), DELIVERING(2), DELIVERED(3);

    private int number;

    OrderStatus(int number) {
        this.number = number;
    }
}
