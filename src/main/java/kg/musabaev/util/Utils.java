package kg.musabaev.util;

import kg.musabaev.Order;
import kg.musabaev.OrderStatus;

import java.util.Random;

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

    public static void requireEqualRefs(Object obj1, Object obj2) {
        if (obj1 != obj2) throw new RuntimeException("object 1 and object 2 must be equal by reference");
    }

    public static String currentThreadName() {
        return Thread.currentThread().getName();
    }

    public static void simulateDelay(Random random, int from, int to) {
        try {
            if (random == null) random = new Random(System.currentTimeMillis());
            int max = to;
            int min = from;
            int randomDelay = random.nextInt(max - min + 1) + min;
            Thread.sleep(randomDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(Thread.currentThread().getName() + "interrupted", e);
        }
    }
}
