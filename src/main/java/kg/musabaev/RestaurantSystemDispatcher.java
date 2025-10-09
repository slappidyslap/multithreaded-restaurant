package kg.musabaev;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static kg.musabaev.util.Utils.currentThreadName;

public class RestaurantSystemDispatcher extends Thread {

    private final ExecutorService chefPool;
    private final OrdersManager ordersManager;
    private final Restaurant restaurant;
    private final Logger logger;

    public RestaurantSystemDispatcher(Restaurant restaurant) {
        super("RestaurantSystemDispatcher");
        this.restaurant = restaurant;
        this.ordersManager = restaurant.getOrdersManager();
        this.chefPool = Executors.newFixedThreadPool(parseInt(getProperty("WORKING_CHEFS")));
        this.logger = Logger.getLogger(RestaurantSystemDispatcher.class.getName());
    }

    @Override
    public void run() {
        logger.info(currentThreadName() + " started");

        Order orderToCook = ordersManager.takeOrderFromIncomingQueue();
        Chef randomChef = restaurant.getRandomChef();
        chefPool.submit(randomChef.createTask(orderToCook));
        if (!chefPool.isShutdown()) chefPool.shutdown();

        logger.info(format("%s took new %s-order and gave it to %s-chef",
                currentThreadName(),
                orderToCook.getId(),
                randomChef.getName()));
    }
}
