package kg.musabaev;

import java.util.Random;
import java.util.concurrent.Semaphore;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static kg.musabaev.util.Utils.currentThreadName;
import static kg.musabaev.util.Utils.delay;

public class ClientThreadGenerator {

    private final Semaphore semaphore;
    private final Restaurant restaurant;
    private final Random random;

    public ClientThreadGenerator(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.semaphore = new Semaphore(parseInt(getProperty("AVAILABLE_TABLES")));
        this.random = new Random();
    }

    public void start() {
        int clientId = 1;
        while (restaurant.isOpen()) {
            try {
                // Запрашиваем разрешение войти в ресторан
                semaphore.acquire();

                Thread clientThread = new Thread(() -> {
                    try {
                        Client newClient = Client.chooseTable(restaurant);
                        newClient.orderFood();
                        newClient.awaitDeliveredOrder();
                        newClient.startEating();
                        newClient.payForOrder();
                        newClient.leaveRestaurant();
                    } finally {
                        // Клиент покинул ресторан — освободить место
                        semaphore.release();
                    }
                }, clientId + "-сlient");

                clientThread.start();
                clientId++;

                delay(random, 10_000, 20_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(currentThreadName() + "interrupted", e);
            }
        }
    }
}
