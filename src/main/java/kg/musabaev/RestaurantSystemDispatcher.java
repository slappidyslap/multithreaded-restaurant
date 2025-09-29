package kg.musabaev;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

public class RestaurantSystemDispatcher extends Thread {

    private final ExecutorService chefPool;
    private final OrdersManager ordersManager;
    private final Restaurant restaurant;

    public RestaurantSystemDispatcher(Restaurant restaurant) {
        super("RestaurantSystemDispatcher");
        this.restaurant = restaurant;
        this.ordersManager = restaurant.getOrdersManager();
        chefPool = Executors.newFixedThreadPool(parseInt(getProperty("WORKING_CHEFS")));
    }

    @Override
    public void run() {
        Order orderToCook = ordersManager.takeOrderFromIncomingQueue();
        Chef randomChef = restaurant.getRandomChef();
        chefPool.submit(randomChef.createTask(orderToCook));
        if (!chefPool.isShutdown()) chefPool.shutdown();
    }
}
