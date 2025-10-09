package kg.musabaev.util;

import kg.musabaev.Order;
import kg.musabaev.OrderStatus;

public class Utils {

    public static void requireOrderStatus(Order order, OrderStatus expected) {
        if (order.getStatus() == expected) return;
        throw new IllegalStateException(
                "expected order status: " + expected + ", but was: " + order.getStatus()
        );
    }

    public static void requireBeNull(Object obj, String message) {
        if (obj != null) throw new RuntimeException(message);
    }

    public static String currentThreadName() {
        return Thread.currentThread().getName();
    }
}
