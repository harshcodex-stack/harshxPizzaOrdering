import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PizzaOrderingSystem {

    public static void main(String[] args) {
        PizzaShop pizzaShop = new PizzaShop();
        Scanner scanner = new Scanner(System.in);
        User currentUser = null;

        pizzaShop.addPromotion(new TimedPromotion("Weekend Treat", "20% off on weekends!", 0.20,
                LocalDateTime.now().withHour(18).withMinute(0).withSecond(0),
                LocalDateTime.now().plusDays(2).withHour(23).withMinute(59).withSecond(59)));

        pizzaShop.loadUserData();
        pizzaShop.loadOrderData();
        pizzaShop.loadHighlyRatedPizzas();

        while (true) {
            pizzaShop.updatePromotions();
            if (currentUser == null) {
                System.out.println("\nWelcome to Pizza Shop!");
                System.out.print("Enter your name to start (or 'exit'): ");
                String name = scanner.nextLine();
                if ("exit".equalsIgnoreCase(name))
                    break;
                currentUser = pizzaShop.createUser(name);
                System.out.println("Welcome, " + currentUser.getName() + "!");
            }

            displayMenu();
            int choice = -1;
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    pizzaShop.createCustomPizza(scanner, currentUser);
                    break;
                case 2:
                    pizzaShop.viewFavorites(scanner, currentUser);
                    break;
                case 3:
                    pizzaShop.placeOrder(scanner, currentUser, pizzaShop);
                    break;
                case 4:
                    pizzaShop.trackOrder(scanner, currentUser);
                    break;
                case 5:
                    pizzaShop.provideFeedback(scanner, currentUser);
                    break;
                case 6:
                    System.out.println("Logging out...");
                    currentUser = null;
                    pizzaShop.saveUserData();
                    pizzaShop.saveOrderData();
                    pizzaShop.updateAndSaveHighlyRatedPizzas();

                    break;
                case 7:
                    System.out.println("Exiting...");
                    pizzaShop.saveUserData();
                    pizzaShop.saveOrderData();
                    pizzaShop.updateAndSaveHighlyRatedPizzas();
                    return;
                case 8:
                    pizzaShop.viewHighlyRatedPizzas();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("\nOptions:");
        System.out.println("1. Create Custom Pizza");
        System.out.println("2. View Favorites");
        System.out.println("3. Place Order");
        System.out.println("4. Track Order");
        System.out.println("5. Provide Feedback");
        System.out.println("6. Logout");
        System.out.println("7. Exit");
        System.out.println("8. View Highly Rated Pizzas");
        System.out.print("Enter your choice: ");
    }
}

// Enums
enum Crust {
    THIN_CRUST, THICK_CRUST, STUFFED_CRUST
}

enum Sauce {
    TOMATO, BBQ, PESTO
}

enum Topping {
    PEPPERONI, MUSHROOMS, ONIONS, OLIVES, BACON, PINEAPPLE
}

enum Cheese {
    MOZZARELLA, CHEDDAR, PARMESAN
}

enum OrderStatus {
    ORDER_PLACED("Order received"),
    PREPARING("Chef is preparing your pizza"),
    BAKING("Your pizza is in the oven"),
    QUALITY_CHECK("Final quality check"),
    OUT_FOR_DELIVERY("On the way to you"),
    DELIVERED("Delivered successfully"),
    PICKED_UP("Picked up by customer");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


abstract class CustomizationHandler {
    private CustomizationHandler nextHandler;
    protected Scanner scanner;

    public CustomizationHandler(Scanner scanner) {
        this.scanner = scanner;
    }

    public void setNext(CustomizationHandler handler) {
        this.nextHandler = handler;
    }

    protected void handleNext(PizzaBuilder pizzaBuilder) {
        if (nextHandler != null) {
            nextHandler.handle(pizzaBuilder);
        }
    }

    public abstract void handle(PizzaBuilder pizzaBuilder);
}

class CrustHandler extends CustomizationHandler {
    public CrustHandler(Scanner scanner) {
        super(scanner);
    }

    @Override
    public void handle(PizzaBuilder pizzaBuilder) {
        System.out.println("\nAvailable Crusts:");
        Crust[] crusts = Crust.values();
        for (int i = 0; i < crusts.length; i++) {
            System.out.println((i + 1) + ". " + crusts[i]);
        }
        System.out.print("Choose crust (enter number): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        pizzaBuilder.setCrust(crusts[choice - 1]);
        handleNext(pizzaBuilder);
    }
}

class SauceHandler extends CustomizationHandler {
    public SauceHandler(Scanner scanner) {
        super(scanner);
    }

    @Override
    public void handle(PizzaBuilder pizzaBuilder) {
        System.out.println("\nAvailable Sauces:");
        Sauce[] sauces = Sauce.values();
        for (int i = 0; i < sauces.length; i++) {
            System.out.println((i + 1) + ". " + sauces[i]);
        }
        System.out.print("Choose sauce (enter number): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        pizzaBuilder.setSauce(sauces[choice - 1]);
        handleNext(pizzaBuilder);
    }
}

class ToppingsHandler extends CustomizationHandler {
    public ToppingsHandler(Scanner scanner) {
        super(scanner);
    }

    @Override
    public void handle(PizzaBuilder pizzaBuilder) {
        List<Topping> selectedToppings = new ArrayList<>();
        while (true) {
            System.out.println("\nAvailable Toppings:");
            Topping[] toppings = Topping.values();
            for (int i = 0; i < toppings.length; i++) {
                System.out.println((i + 1) + ". " + toppings[i]);
            }
            System.out.println((toppings.length + 1) + ". Done Selecting Toppings");
            System.out.print("Choose topping: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == toppings.length + 1)
                break;
            if (choice > 0 && choice <= toppings.length) {
                selectedToppings.add(toppings[choice - 1]);
            }
        }
        pizzaBuilder.setToppings(selectedToppings);
        handleNext(pizzaBuilder);
    }
}

class CheeseHandler extends CustomizationHandler {
    public CheeseHandler(Scanner scanner) {
        super(scanner);
    }

    @Override
    public void handle(PizzaBuilder pizzaBuilder) {
        System.out.println("\nAvailable Cheese Options:");
        Cheese[] cheeses = Cheese.values();
        for (int i = 0; i < cheeses.length; i++) {
            System.out.println((i + 1) + ". " + cheeses[i]);
        }
        System.out.print("Choose cheese (enter number): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        pizzaBuilder.setCheese(cheeses[choice - 1]);
        handleNext(pizzaBuilder);
    }
}


class PizzaBuilder {
    private String name;
    private Crust crust;
    private Sauce sauce;
    private List<Topping> toppings;
    private Cheese cheese;

    public PizzaBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public PizzaBuilder setCrust(Crust crust) {
        this.crust = crust;
        return this;
    }

    public PizzaBuilder setSauce(Sauce sauce) {
        this.sauce = sauce;
        return this;
    }

    public PizzaBuilder setToppings(List<Topping> toppings) {
        this.toppings = toppings;
        return this;
    }

    public PizzaBuilder setCheese(Cheese cheese) {
        this.cheese = cheese;
        return this;
    }

    public Pizza build() {
        return new Pizza(name, crust, sauce, toppings, cheese);
    }
}

// Interfaces
interface OrderObserver {
    void update(Order order);
}

interface Command {
    void execute();
}

interface PaymentStrategy {
    boolean pay(double amount);
}

class FeedbackCommand implements Command {
    private Order order;
    private String feedback;
    private int rating;

    public FeedbackCommand(Order order, String feedback, int rating) {
        this.order = order;
        this.feedback = feedback;
        this.rating = rating;
    }

    @Override
    public void execute() {
        order.setFeedback(feedback);
        order.setRating(rating);
        System.out.println("Feedback submitted for Order #" + order.getOrderId());
    }
}

// Payment
class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    private String expiryDate;
    private String cvv;

    public CreditCardPayment(String cardNumber, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    @Override
    public boolean pay(double amount) {
        if (cvv == null || cvv.length() < 3) {
            System.out.println("Invalid CVV");
            return false;
        }
        System.out.println("Processing credit card payment of $" + amount +
                " with card ending in " + cardNumber.substring(Math.max(0, cardNumber.length() - 4)) +
                " (expires: " + expiryDate + ")");
        return true;
    }
}

class DigitalWalletPayment implements PaymentStrategy {
    private String email;

    public DigitalWalletPayment(String email) {
        this.email = email;
    }

    @Override
    public boolean pay(double amount) {
        System.out.println("Processing digital wallet payment of $" + amount + " for user: " + email);
        return true;
    }
}

class LoyaltyPointsPayment implements PaymentStrategy {
    private User user;

    public LoyaltyPointsPayment(User user) {
        this.user = user;
    }

    @Override
    public boolean pay(double amount) {
        if (user.getLoyaltyPoints() >= amount) {
            System.out.println("Paying $" + amount + " using loyalty points.");
            user.decreaseLoyaltyPoints(amount);
            return true;
        } else {
            System.out.println("Insufficient loyalty points.");
            return false;
        }
    }
}

// Classes
class PizzaShop {
    private Map<String, Pizza> availablePizzas = new HashMap<>();
    private List<Order> orders = new ArrayList<>();
    private Map<User, List<Pizza>> favoritePizzas = new HashMap<>();
    private int nextOrderId = 1;
    private List<Promotion> promotions = new ArrayList<>();
    private Map<User, UserProfile> userProfiles = new HashMap<>();
    private List<TimedPromotion> activePromotions = new ArrayList<>();
    private static final String HIGHLY_RATED_PIZZAS_FILE = "highly_rated_pizzas.txt";
    private static final String USER_DATA_FILE = "user_data.txt";
    private static final String ORDER_DATA_FILE = "order_data.txt";

    private Map<String, Double> highlyRatedPizzas = new HashMap<>();

    public PizzaShop() {
        availablePizzas.put("Margherita",
                new Pizza("Margherita", Crust.THIN_CRUST, Sauce.TOMATO, List.of(Topping.OLIVES), Cheese.MOZZARELLA));
        availablePizzas.put("Pepperoni Feast", new Pizza("Pepperoni Feast", Crust.THICK_CRUST, Sauce.TOMATO,
                List.of(Topping.PEPPERONI), Cheese.MOZZARELLA));
        promotions.add(new Promotion("Summer Special", "Get 10% off on all orders!", 0.10));
    }

    public User createUser(String name) {
        User user = new User(name);
        userProfiles.put(user, new UserProfile(user));
        favoritePizzas.put(user, new ArrayList<>());
        return user;
    }

    
    public void createCustomPizza(Scanner scanner, User user) {
        System.out.println("Create Your Custom Pizza:");
        System.out.print("Enter a name for your pizza: ");
        String pizzaName = scanner.nextLine();

        PizzaBuilder pizzaBuilder = new PizzaBuilder().setName(pizzaName);

        // Setup the chain
        CustomizationHandler crustHandler = new CrustHandler(scanner);
        CustomizationHandler sauceHandler = new SauceHandler(scanner);
        CustomizationHandler toppingsHandler = new ToppingsHandler(scanner);
        CustomizationHandler cheeseHandler = new CheeseHandler(scanner);

        crustHandler.setNext(sauceHandler);
        sauceHandler.setNext(toppingsHandler);
        toppingsHandler.setNext(cheeseHandler);

        // Start the chain
        crustHandler.handle(pizzaBuilder);

        Pizza customPizza = pizzaBuilder.build();
        availablePizzas.put(pizzaName, customPizza);
        System.out.println(pizzaName + " pizza created successfully!");

        System.out.print("Do you want to save this as a favorite? (yes/no): ");
        String saveFav = scanner.nextLine();
        if ("yes".equalsIgnoreCase(saveFav)) {
            addFavorite(user, customPizza);
        }
    }

    public void viewFavorites(Scanner scanner, User user) {
        List<Pizza> favorites = favoritePizzas.get(user);
        if (favorites == null || favorites.isEmpty()) {
            System.out.println("You have no favorite pizzas yet.");
        } else {
            System.out.println("\nYour Favorite Pizzas:");
            for (int i = 0; i < favorites.size(); i++) {
                System.out.println((i + 1) + ". " + favorites.get(i));
            }
            System.out.print("Enter the number to reorder or 0 to go back: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice > 0 && choice <= favorites.size()) {
                placeOrderWithFavorite(user, favorites.get(choice - 1));
            }
        }
    }

    private void placeOrderWithFavorite(User user, Pizza favorite) {
        Scanner scanner = new Scanner(System.in);
        OrderBuilder orderBuilder = new OrderBuilder(nextOrderId++, user).addItem(favorite);
        System.out.print("Pickup or Delivery? (pickup/delivery): ");
        String orderType = scanner.nextLine();
        orderBuilder.setOrderType(orderType.equalsIgnoreCase("pickup") ? "Pickup" : "Delivery");
        Order order = orderBuilder.build();
        if ("Delivery".equalsIgnoreCase(order.getOrderType())) {
            System.out.print("Enter your delivery address: ");
            String deliveryAddress = scanner.nextLine();
            order.setDeliveryDetails(deliveryAddress);
        }
        orders.add(order);
        userProfiles.get(user).addOrder(order);
        order.addObserver(user);
        System.out.println("\nOrder placed with favorite pizza!");
        System.out.println(order);
        processPayment(new Scanner(System.in), user, order.calculateTotal());

        simulateOrderProgression(order);
        System.out.println("Order placed successfully! Order ID: " + order.getOrderId());
    }

    public void simulateOrderProgression(Order order) {
        if (order != null) {
            try {
                startPreparingOrder(order);
                TimeUnit.SECONDS.sleep(2);
                startBakingOrder(order);
                TimeUnit.SECONDS.sleep(3);
                markOrderQualityCheck(order);
                TimeUnit.SECONDS.sleep(1);
                if ("Delivery".equalsIgnoreCase(order.getOrderType())) {
                    markOrderOutForDelivery(order);
                    TimeUnit.SECONDS.sleep(3);
                    markOrderDelivered(order);
                } else {
                    markOrderPickedUp(order);
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted during order progression: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

    }

    public void addFavorite(User user, Pizza pizza) {
        favoritePizzas.get(user).add(pizza);
        System.out.println(pizza.getName() + " added to your favorites.");
    }

    public void placeOrder(Scanner scanner, User user, PizzaShop pizzaShop) {
        OrderBuilder orderBuilder = new OrderBuilder(nextOrderId++, user);
        boolean addingItems = true;

        while (addingItems) {
            System.out.println("\nPlace Your Order:");
            System.out.println("1. Add a pre-existing pizza to order");
            System.out.println("2. Add a favorite pizza to order");
            System.out.println("3. Finish adding items");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("\nAvailable Pizzas:");
                    availablePizzas.forEach((name, pizza) -> System.out.println("- " + name));
                    System.out.print("Enter the name of the pizza to add: ");
                    String pizzaName = scanner.nextLine();
                    Pizza pizzaToAdd = availablePizzas.get(pizzaName);
                    if (pizzaToAdd != null) {
                        orderBuilder.addItem(pizzaToAdd);
                        System.out.println(pizzaName + " added to your order.");
                    } else {
                        System.out.println("Pizza not found.");
                    }
                    break;
                case 2:
                    viewFavoritesForOrder(scanner, user, orderBuilder);
                    break;
                case 3:
                    addingItems = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
        System.out.print("Pickup or Delivery? (pickup/delivery): ");
        String orderType = scanner.nextLine();
        orderBuilder.setOrderType(orderType.equalsIgnoreCase("pickup") ? "Pickup" : "Delivery");

        Order order = orderBuilder.build();
        if ("Delivery".equalsIgnoreCase(order.getOrderType())) {
            System.out.print("Enter your delivery address: ");
            String deliveryAddress = scanner.nextLine();
            order.setDeliveryDetails(deliveryAddress);
            System.out.println("Estimated Delivery Time: "
                    + order.getEstimatedDeliveryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        orders.add(order);
        userProfiles.get(user).addOrder(order);
        order.addObserver(user);
        System.out.println("\nOrder Summary:");
        System.out.println(order);
        double totalPrice = order.calculateTotal();
        for (Promotion promo : promotions) {
            totalPrice *= (1 - promo.getDiscountRate());
            System.out.println("Applied promotion: " + promo.getDescription());
        }
        for (TimedPromotion promo : activePromotions) {
            if (promo.isActive()) {
                totalPrice *= (1 - promo.getDiscountRate());
                System.out.println("Applied promotion: " + promo.getDescription());
            }
        }
        System.out.println("Total amount: $" + String.format("%.2f", totalPrice));
        processPayment(scanner, user, totalPrice);

        simulateOrderProgression(order);
        System.out.println("Order placed successfully! Order ID: " + order.getOrderId());
    }

    private void viewFavoritesForOrder(Scanner scanner, User user, OrderBuilder orderBuilder) {
        List<Pizza> favorites = favoritePizzas.get(user);
        if (favorites == null || favorites.isEmpty()) {
            System.out.println("You have no favorite pizzas.");
        } else {
            System.out.println("\nYour Favorite Pizzas:");
            for (int i = 0; i < favorites.size(); i++) {
                System.out.println((i + 1) + ". " + favorites.get(i).getName());
            }
            System.out.print("Enter the number to add to order or 0 to go back: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice > 0 && choice <= favorites.size()) {
                orderBuilder.addItem(favorites.get(choice - 1));
                System.out.println(favorites.get(choice - 1).getName() + " added to your order.");
            }
        }
    }

    private void processPayment(Scanner scanner, User user, double amount) {
        System.out.println("\nPayment Options:");
        System.out.println("1. Credit Card");
        System.out.println("2. Digital Wallet");
        System.out.println("3. Loyalty Points (You have: " + user.getLoyaltyPoints() + " points)");
        System.out.print("Choose payment method: ");
        int paymentChoice = scanner.nextInt();
        scanner.nextLine();

        PaymentStrategy paymentStrategy = null;
        switch (paymentChoice) {
            case 1:
                System.out.print("Enter card number: ");
                String cardNumber = scanner.nextLine();
                System.out.print("Enter expiry date: ");
                String expiryDate = scanner.nextLine();
                System.out.print("Enter CVV: ");
                String cvv = scanner.nextLine();
                paymentStrategy = new CreditCardPayment(cardNumber, expiryDate, cvv);
                break;
            case 2:
                System.out.print("Enter your email: ");
                String email = scanner.nextLine();
                paymentStrategy = new DigitalWalletPayment(email);
                break;
            case 3:
                paymentStrategy = new LoyaltyPointsPayment(user);
                break;
            default:
                System.out.println("Invalid payment method.");
                return;
        }

        if (paymentStrategy != null && paymentStrategy.pay(amount)) {
            user.increaseLoyaltyPoints(amount * 0.1);
            System.out.println("Payment successful.");
        } else {
            System.out.println("Payment failed.");
        }
    }

    public void trackOrder(Scanner scanner, User user) {
        System.out.print("Enter the Order ID to track: ");
        if (scanner.hasNextInt()) {
            int orderId = scanner.nextInt();
            scanner.nextLine();
            Order orderToTrack = null;
            for (Order order : userProfiles.get(user).getOrders()) {
                if (order.getOrderId() == orderId) {
                    orderToTrack = order;
                    break;
                }
            }
            if (orderToTrack != null) {
                System.out.println("Order Status: " + orderToTrack.getStatus().getDescription());
                if (orderToTrack.getEstimatedDeliveryTime() != null) {
                    System.out.println("Estimated Delivery Time: " + orderToTrack.getEstimatedDeliveryTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            } else {
                System.out.println("Order not found for this user.");
            }
        } else {
            System.out.println("Invalid Order ID.");
            scanner.nextLine();
        }
    }

    public void startPreparingOrder(Order order) {
        order.setStatus(OrderStatus.PREPARING);
        System.out.println("Order #" + order.getOrderId() + ": " + order.getStatus().getDescription());
    }

    public void startBakingOrder(Order order) {
        order.setStatus(OrderStatus.BAKING);
        System.out.println("Order #" + order.getOrderId() + ": " + order.getStatus().getDescription());
    }

    public void markOrderQualityCheck(Order order) {
        order.setStatus(OrderStatus.QUALITY_CHECK);
        System.out.println("Order #" + order.getOrderId() + ": " + order.getStatus().getDescription());
    }

    public void markOrderOutForDelivery(Order order) {
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        System.out.println("Order #" + order.getOrderId() + ": " + order.getStatus().getDescription());
    }

    public void markOrderDelivered(Order order) {
        order.setStatus(OrderStatus.DELIVERED);
        System.out.println("Order #" + order.getOrderId() + ": " + order.getStatus().getDescription());
    }

    public void markOrderPickedUp(Order order) {
        order.setStatus(OrderStatus.PICKED_UP);
        System.out.println("Order #" + order.getOrderId() + ": " + order.getStatus().getDescription());
    }

    public void provideFeedback(Scanner scanner, User user) {
        System.out.print("Enter the Order ID to provide feedback for: ");
        if (scanner.hasNextInt()) {
            int orderId = scanner.nextInt();
            scanner.nextLine();
            Order orderForFeedback = null;
            for (Order order : userProfiles.get(user).getOrders()) {
                if (order.getOrderId() == orderId) {
                    orderForFeedback = order;
                    break;
                }
            }
            if (orderForFeedback != null) {
                System.out.print("Enter your feedback: ");
                String feedback = scanner.nextLine();
                System.out.print("Enter your rating (1-5): ");
                int rating = scanner.nextInt();
                scanner.nextLine();
                Command feedbackCommand = new FeedbackCommand(orderForFeedback, feedback, rating);
                feedbackCommand.execute();
            } else {
                System.out.println("Order not found for this user.");
            }
        } else {
            System.out.println("Invalid Order ID.");
            scanner.nextLine();
        }
    }

    public void viewHighlyRatedPizzas() {
        System.out.println("\nHighly Rated Pizzas:");
        loadHighlyRatedPizzas();
        if (highlyRatedPizzas.isEmpty()) {
            System.out.println("No highly rated pizzas yet.");
        } else {
            highlyRatedPizzas.forEach((pizzaName, rating) -> System.out
                    .println("- " + pizzaName + " (Average Rating: " + String.format("%.2f", rating) + ")"));
        }
    }

    private double getAverageRating(String pizzaName) {
        return orders.stream()
                .filter(order -> order.getItems().stream().anyMatch(pizza -> pizza.getName().equals(pizzaName)))
                .filter(order -> order.getRating() > 0)
                .mapToInt(Order::getRating)
                .average()
                .orElse(0.0);
    }

    private Crust chooseCrust(Scanner scanner) {
        System.out.println("\nAvailable Crusts:");
        Crust[] crusts = Crust.values();
        for (int i = 0; i < crusts.length; i++) {
            System.out.println((i + 1) + ". " + crusts[i]);
        }
        System.out.print("Choose crust (enter number): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        return crusts[choice - 1];
    }

    private Sauce chooseSauce(Scanner scanner) {
        System.out.println("\nAvailable Sauces:");
        Sauce[] sauces = Sauce.values();
        for (int i = 0; i < sauces.length; i++) {
            System.out.println((i + 1) + ". " + sauces[i]);
        }
        System.out.print("Choose sauce (enter number): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        return sauces[choice - 1];
    }

    private List<Topping> chooseToppings(Scanner scanner) {
        List<Topping> selectedToppings = new ArrayList<>();
        while (true) {
            System.out.println("\nAvailable Toppings:");
            Topping[] toppings = Topping.values();
            for (int i = 0; i < toppings.length; i++) {
                System.out.println((i + 1) + ". " + toppings[i]);
            }
            System.out.println((toppings.length + 1) + ". Done Selecting Toppings");
            System.out.print("Choose topping (enter number or " + (toppings.length + 1) + " if finished): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == toppings.length + 1)
                break;
            if (choice > 0 && choice <= toppings.length) {
                selectedToppings.add(toppings[choice - 1]);
            } else {
                System.out.println("Invalid topping choice. Please try again.");
            }
        }
        return selectedToppings;
    }

    private Cheese chooseCheese(Scanner scanner) {
        System.out.println("\nAvailable Cheese Options:");
        Cheese[] cheeses = Cheese.values();
        for (int i = 0; i < cheeses.length; i++) {
            System.out.println((i + 1) + ". " + cheeses[i]);
        }
        System.out.print("Choose cheese (enter number): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        return cheeses[choice - 1];
    }

    public void addPromotion(TimedPromotion promotion) {
        activePromotions.add(promotion);
    }

    public void updatePromotions() {
        activePromotions.removeIf(TimedPromotion::isExpired);
    }

    // File Loading and Saving

    public void loadUserData() {
        File file = new File(USER_DATA_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        User user = new User(parts[0]);
                        user.setLoyaltyPoints(Double.parseDouble(parts[1]));
                        userProfiles.put(user, new UserProfile(user));
                        favoritePizzas.put(user, new ArrayList<>());
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading user data: " + e.getMessage());
            }
        }
    }

    public void saveUserData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_DATA_FILE))) {
            for (Map.Entry<User, UserProfile> entry : userProfiles.entrySet()) {
                User user = entry.getKey();
                writer.write(user.getName() + "," + user.getLoyaltyPoints());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }

    public void loadOrderData() {
        File file = new File(ORDER_DATA_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Order order = Order.fromString(line, userProfiles);
                    if (order != null)
                        orders.add(order);
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading order data: " + e.getMessage());
            }
        }
    }

    public void saveOrderData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDER_DATA_FILE))) {
            for (Order order : orders) {
                writer.write(order.toStringForFile());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving order data: " + e.getMessage());
        }
    }

    public void loadHighlyRatedPizzas() {
        File file = new File(HIGHLY_RATED_PIZZAS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        String pizzaName = parts[0];
                        double averageRating = Double.parseDouble(parts[1]);
                        highlyRatedPizzas.put(pizzaName, averageRating);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading highly rated pizzas: " + e.getMessage());
            }
        }
    }

    private void saveHighlyRatedPizzas() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGHLY_RATED_PIZZAS_FILE))) {
            for (Map.Entry<String, Double> entry : highlyRatedPizzas.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving highly rated pizzas: " + e.getMessage());
        }
    }

    public void updateAndSaveHighlyRatedPizzas() {
        availablePizzas.keySet().forEach(pizzaName -> {
            double avgRating = getAverageRating(pizzaName);
            if (avgRating >= 4) {
                highlyRatedPizzas.put(pizzaName, avgRating);
            } else {
                highlyRatedPizzas.remove(pizzaName);
            }
        });
        saveHighlyRatedPizzas();
    }
}

// Delivery
class DeliveryEstimator {
    public int estimateDeliveryTime(String address) {

        return 30 + (int) (Math.random() * 15);
    }
}

// Data Classes
class Pizza {
    private String name;
    private Crust crust;
    private Sauce sauce;
    private List<Topping> toppings;
    private Cheese cheese;
    private double basePrice = 7.99;

    public Pizza(String name, Crust crust, Sauce sauce, List<Topping> toppings, Cheese cheese) {
        this.name = name;
        this.crust = crust;
        this.sauce = sauce;
        this.toppings = toppings;
        this.cheese = cheese;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return basePrice + (0.5 * toppings.size());
    }

    @Override
    public String toString() {
        return name + " (Crust: " + crust + ", Sauce: " + sauce + ", Toppings: " + toppings + ", Cheese: " + cheese
                + ")";
    }
}

class Order implements OrderObserver {
    private int orderId;
    private User customer;
    private List<Pizza> items;
    private OrderStatus status;
    private String orderType;
    private String feedback;
    private int rating;
    private List<OrderObserver> observers = new ArrayList<>();
    private String deliveryAddress;
    private LocalDateTime estimatedDeliveryTime;

    public Order(int orderId, User customer) {
        this.orderId = orderId;
        this.customer = customer;
        this.items = new ArrayList<>();
        this.status = OrderStatus.ORDER_PLACED;
    }

    public static Order fromString(String data, Map<User, UserProfile> userProfiles) {
        String[] parts = data.split("\\|");
        if (parts.length != 9)
            return null;
        int orderId = Integer.parseInt(parts[0]);
        User user = userProfiles.keySet().stream()
                .filter(u -> u.getName().equals(parts[1]))
                .findFirst()
                .orElse(null);
        if (user == null)
            return null;

        Order order = new Order(orderId, user);
        order.status = OrderStatus.valueOf(parts[2]);
        order.orderType = parts[3];
        if (!parts[4].equals("null")) {
            order.deliveryAddress = parts[4];
        }
        if (!parts[5].equals("null")) {
            order.estimatedDeliveryTime = LocalDateTime.parse(parts[5]);
        }
        order.feedback = parts[6];
        order.rating = Integer.parseInt(parts[7]);
        String[] pizzaNames = parts[8].split(";");
        for (String pizzaName : pizzaNames) {
            if (pizzaName != null && !pizzaName.isEmpty()) {
                order.addItem(new Pizza(pizzaName, null, null, null, null));
            }
        }
        return order;
    }

    public int getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        notifyObservers();
    }

    public void addItem(Pizza pizza) {
        this.items.add(pizza);
    }

    public List<Pizza> getItems() {
        return items;
    }

    public double calculateTotal() {
        return items.stream().mapToDouble(Pizza::getPrice).sum();
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void addObserver(OrderObserver observer) {
        this.observers.add(observer);
    }

    public void removeObserver(OrderObserver observer) {
        this.observers.remove(observer);
    }

    private void notifyObservers() {
        for (OrderObserver observer : observers) {
            observer.update(this);
        }
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public LocalDateTime getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setDeliveryDetails(String address) {
        this.deliveryAddress = address;
        if ("Delivery".equalsIgnoreCase(orderType)) {
            DeliveryEstimator estimator = new DeliveryEstimator();
            int minutes = estimator.estimateDeliveryTime(address);
            this.estimatedDeliveryTime = LocalDateTime.now().plusMinutes(minutes);
        }
    }

    public String toStringForFile() {
        StringBuilder sb = new StringBuilder();
        String pizzaNames = items.stream().map(Pizza::getName).collect(Collectors.joining(";"));
        sb.append(orderId).append("|")
                .append(customer.getName()).append("|")
                .append(status).append("|")
                .append(orderType).append("|")
                .append(deliveryAddress != null ? deliveryAddress : "null").append("|")
                .append(estimatedDeliveryTime != null ? estimatedDeliveryTime : "null").append("|")
                .append(feedback).append("|")
                .append(rating).append("|")
                .append(pizzaNames);
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Order ID: ").append(orderId)
                .append("\nCustomer: ").append(customer.getName())
                .append("\nItems:\n");
        for (Pizza pizza : items) {
            sb.append("- ").append(pizza).append("\n");
        }
        sb.append("Total: $").append(String.format("%.2f", calculateTotal()))
                .append("\nStatus: ").append(status.getDescription())
                .append("\nType: ").append(orderType);
        if (estimatedDeliveryTime != null) {
            sb.append("\nEstimated Delivery: ")
                    .append(estimatedDeliveryTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        return sb.toString();
    }

    @Override
    public void update(Order order) {
        System.out.println("Dear " + customer.getName() + ", your Order #" + order.getOrderId()
                + " status has been updated to: " + order.getStatus().getDescription());
    }
}

class OrderBuilder {
    private int orderId;
    private User customer;
    private List<Pizza> items = new ArrayList<>();
    private String orderType;

    public OrderBuilder(int orderId, User customer) {
        this.orderId = orderId;
        this.customer = customer;
    }

    public OrderBuilder addItem(Pizza pizza) {
        this.items.add(pizza);
        return this;
    }

    public OrderBuilder setOrderType(String orderType) {
        this.orderType = orderType;
        return this;
    }

    public Order build() {
        Order order = new Order(orderId, customer);
        for (Pizza item : items) {
            order.addItem(item);
        }
        order.setOrderType(orderType);
        return order;
    }
}

// User and Profile
class User implements OrderObserver {
    private String name;
    private double loyaltyPoints;

    public User(String name) {
        this.name = name;
        this.loyaltyPoints = 0;
    }

    public String getName() {
        return name;
    }

    public double getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void increaseLoyaltyPoints(double points) {
        this.loyaltyPoints += points;
        System.out.println("You earned " + String.format("%.2f", points) + " loyalty points.");
    }

    public void setLoyaltyPoints(double loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public void decreaseLoyaltyPoints(double points) {
        this.loyaltyPoints -= points;
    }

    @Override
    public void update(Order order) {
        System.out.println("Dear " + name + ", your Order #" + order.getOrderId() + " status has been updated to: "
                + order.getStatus().getDescription());
    }
}

class UserProfile {
    private User user;
    private List<Order> orders = new ArrayList<>();

    public UserProfile(User user) {
        this.user = user;
    }

    public void addOrder(Order order) {
        this.orders.add(order);
    }

    public List<Order> getOrders() {
        return orders;
    }

    public User getUser() {
        return user;
    }
}

// Promotion
class Promotion {
    private String name;
    private String description;
    private double discountRate;

    public Promotion(String name, String description, double discountRate) {
        this.name = name;
        this.description = description;
        this.discountRate = discountRate;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getDiscountRate() {
        return discountRate;
    }
}

// Timed Promotion
class TimedPromotion extends Promotion {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public TimedPromotion(String name, String description, double discountRate,
            LocalDateTime startTime, LocalDateTime endTime) {
        super(name, description, discountRate);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endTime);
    }

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }
}