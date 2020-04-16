package banking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    // Bank Identification Number
    private static final String BIN = "400000";

    // Temporary account storage
    private static Map<String, User> ClientCards = new HashMap<>();

    // Current menu
    private static Menu curMenu;

    public static Map<String, User> getClientCards() { return ClientCards; }

    private static void exit() {
        System.out.println("Bye!\n");
        System.exit(0);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Menu mainMenu = new Menu();
        mainMenu.addOption(new Option(mainMenu.getOptionsCount(), "Exit", () -> exit()));
        mainMenu.addOption(new Option(mainMenu.getOptionsCount(), "Create account", () -> {
            String generatedCard = CardGenerator.cardNumber(BIN);
            String generatedPIN = CardGenerator.pinNumber();
            ClientCards.put(generatedCard, new User(generatedCard, generatedPIN));
            System.out.println("Your card have been created\n" +
                    "Your card number:\n" +
                    generatedCard + "\n" +
                    "Your card PIN:\n" +
                    generatedPIN + "\n");
        }));
        mainMenu.addOption(new Option(mainMenu.getOptionsCount(), "Log into account", () -> {
            System.out.println("Enter your card number:");
            String userCardNumInput = scanner.next();
            System.out.println("Enter your PIN:");
            String userPinInput = scanner.next();
            User user = ClientCards.get(userCardNumInput);
            if (user != null && userPinInput.equals(user.getPIN())) {
                MenuManager.setLoggedInUser(user);
                System.out.println("\nYou have successfully logged in\n");
                curMenu = MenuManager.getMenu("USER_PANEL");
            }
            else
                System.out.println("\nWrong card number or PIN!\n");
        } ));

        Menu userPanel = new Menu();
        userPanel.addOption(new Option(mainMenu.getOptionsCount(), "Exit", () -> exit()));
        userPanel.addOption(new Option(mainMenu.getOptionsCount(), "Balance", () ->
                System.out.println("Balance: " + MenuManager.getLoggedInUser().getBalance() + "\n")));
        userPanel.addOption(new Option(mainMenu.getOptionsCount(), "Log out", () -> {
                MenuManager.setLoggedInUser(null);
                System.out.println("You have successfully logged out!\n");
                curMenu = MenuManager.getMenu("MAIN_MENU");
        }));
        MenuManager.addMenu("MAIN_MENU", mainMenu);
        MenuManager.addMenu("USER_PANEL", userPanel);
        curMenu = mainMenu;
        while (true) {
            System.out.print(curMenu.toString());
            int optionInd = scanner.nextInt();
            System.out.println();
            curMenu.getOptions().get(optionInd).invokeAction();
        }
    }
}