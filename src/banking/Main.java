package banking;

import SQLManager.QueryManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    // Bank Identification Number
    private static final String BIN = "400000";

    // Current menu
    private static Menu curMenu;

    // SQLConnection
    public static QueryManager cardsDataBase = new QueryManager(
            "jdbc:postgresql://packy.db.elephantsql.com:5432/cmhrlftk",
            "cmhrlftk",
            "ImVjClUtPft6FK_lcpj0Zc5mDMOAI5uA");
    private static void exit() {
        System.out.println("Bye!\n");
        System.exit(0);
    }

    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        // Printing Existing Cards in the DataBase
        System.out.println("CARDS DATABASE\n");
        ResultSet resultSet = cardsDataBase.executeReadQuery("SELECT * FROM public.cards");
        System.out.printf("%-30.30s  %-30.30s  %-30.30s%n", "number", "pin", "balance");
        while (resultSet.next()) {
            System.out.printf("%-30.30s  %-30.30s  %-30.30s%n", resultSet.getString("number"), resultSet.getString("pin"), resultSet.getString("balance"));
        }
        System.out.println();
        Menu mainMenu = new Menu();
        mainMenu.addOption(new Option(mainMenu.getOptionsCount(), "Exit", () -> exit()));
        mainMenu.addOption(new Option(mainMenu.getOptionsCount(), "Create account", () -> {
            String generatedCard = CardGenerator.cardNumber(BIN);
            String generatedPIN = CardGenerator.pinNumber();
            cardsDataBase.executeWriteQuery("INSERT INTO cards (number, pin, balance) VALUES ('" + generatedCard + "', '" +
                    generatedPIN + "', " + 0 + ")");
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

            User user = null;
            ResultSet result = cardsDataBase.executeReadQuery("SELECT EXISTS(SELECT * FROM cards WHERE number = '" + userCardNumInput + "' AND pin = '" + userPinInput + "')");
            result.next();
            if (result.getString("exists").equals("t")) {
                result = cardsDataBase.executeReadQuery("SELECT * FROM cards WHERE number = '" + userCardNumInput + "' AND pin = '" + userPinInput + "'");
                result.next();
                user = new User(result.getString("number"), result.getString("pin"), result.getLong("balance"));
            }
            if (user != null) {
                MenuManager.setLoggedInUser(user);
                System.out.println("\nYou have successfully logged in\n");
                curMenu = MenuManager.getMenu("USER_PANEL");
            }
            else
                System.out.println("\nWrong card number or PIN!\n");
        } ));

        Menu userPanel = new Menu();
        userPanel.addOption(new Option(userPanel.getOptionsCount(), "Exit", () -> exit()));
        userPanel.addOption(new Option(userPanel.getOptionsCount(), "Balance", () ->
                System.out.println("Balance: " + MenuManager.getLoggedInUser().getBalance() + "\n")));
        userPanel.addOption(new Option(userPanel.getOptionsCount(), "Log out", () -> {
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