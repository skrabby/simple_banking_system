package banking;

import SQLManager.QueryManager;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    // Bank Identification Number
    private static final String BIN = "400000";

    // Current menu
    private static Menu curMenu;

    /* Configuration SQL (SQLITE/POSTGRES Only)
       SQLite - local
       Postgres - online (elephantsql.com)
     */
    public static String TYPE_SQL = "SQLITE";
    public static QueryManager cardsDataBase;
    // SerialCount
    private static int serialID = 1;

    private static void exit() {
        System.out.println("Bye!\n");
        System.exit(0);
    }

    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        if (TYPE_SQL.equals("POSTGRES")) {
            cardsDataBase = new QueryManager(
                    "jdbc:postgresql://packy.db.elephantsql.com:5432/cmhrlftk",
                    "cmhrlftk",
                    "ImVjClUtPft6FK_lcpj0Zc5mDMOAI5uA");
            cardsDataBase.createNewDatabase();
            cardsDataBase.createNewTable("card",
                    "id BIGSERIAL PRIMARY KEY,\n"
                            + "	number TEXT,\n"
                            + " pin TEXT,\n"
                            + "	balance INTEGER DEFAULT 0\n");
        } else if (TYPE_SQL.equals("SQLITE")) {
            if (args.length >= 2) {
                if (args[0].equals("-fileName")) {
                    cardsDataBase = new QueryManager(
                            "jdbc:sqlite:./" + args[1],
                            "",
                            ""
                    );
                    File file = new File("./" + args[1]);
                    if (!file.exists()) {
                        cardsDataBase.createNewDatabase();
                        cardsDataBase.createNewTable("card",
                                "id INTEGER,\n"
                                        + "	number TEXT,\n"
                                        + " pin TEXT,\n"
                                        + "	balance INTEGER DEFAULT 0\n");
                    }
                }
            }
        } else {
            System.out.println("SQL Configuration can only be either SQLITE or POSTGRES");
            exit();
        }

        String flag = (TYPE_SQL.equals("POSTGRES")) ? "COUNT" : "COUNT(*)";
        serialID = Integer.valueOf(cardsDataBase.executeReadQuery("SELECT COUNT(*) FROM card", flag));

        // Printing Existing Cards in the DataBase
        System.out.printf("%-5.5s  %-30.30s  %-30.30s  %-30.30s%n", "id", "number", "pin", "balance");
        System.out.println(cardsDataBase.executeReadQuery("SELECT * FROM card", "select"));
        Menu mainMenu = new Menu();
        mainMenu.addOption(new Option(mainMenu.getOptionsCount(), "Exit", () -> exit()));
        mainMenu.addOption(new Option(mainMenu.getOptionsCount(), "Create account", () -> {
            String generatedCard = CardGenerator.cardNumber(BIN);
            String generatedPIN = CardGenerator.pinNumber();
            cardsDataBase.executeWriteQuery("INSERT INTO card (id, number, pin, balance) VALUES ('" + serialID++ + "', '" + generatedCard + "', '" +
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

            //Postgres version
            if (TYPE_SQL.equals("POSTGRES")) {
                ResultSet result = cardsDataBase.executeReadQuery("SELECT EXISTS(SELECT * FROM card WHERE number = '" + userCardNumInput + "' AND pin = '" + userPinInput + "') AS \"exists\"");
                result.next();
                if (result.getString("exists").equals("t")) {
                    result = cardsDataBase.executeReadQuery("SELECT * FROM card WHERE number = '" + userCardNumInput + "' AND pin = '" + userPinInput + "'");
                    result.next();
                    user = new User(result.getString("id"), result.getString("number"), result.getString("pin"), result.getLong("balance"));
                }
            }

            //SQLite version
            if (TYPE_SQL.equals("SQLITE")) {
                String result = cardsDataBase.executeReadQuery("SELECT EXISTS(SELECT * FROM card WHERE number = '" + userCardNumInput + "' AND pin = '" + userPinInput + "') AS \"exists\"", "exists");
                if (result.equals("1")) {
                    result = cardsDataBase.executeReadQuery("SELECT * FROM card WHERE number = '" + userCardNumInput + "' AND pin = '" + userPinInput + "'", "user");
                    String[] split = result.split(" ");
                    user = new User(split[0], split[1], split[2], Long.valueOf(split[3]));
                }
            }


            if (user != null) {
                MenuManager.setLoggedInUser(user);
                System.out.println("\nYou have successfully logged in\n");
                curMenu = MenuManager.getMenu("USER_PANEL");
            } else
                System.out.println("\nWrong card number or PIN!\n");
        }));

        Menu userPanel = new Menu();
        userPanel.addOption(new Option(userPanel.getOptionsCount(), "Exit", () -> exit()));
        userPanel.addOption(new Option(userPanel.getOptionsCount(), "Balance", () ->
                System.out.println("Balance: " + MenuManager.getLoggedInUser().getBalance() + "\n")));
        userPanel.addOption(new Option(userPanel.getOptionsCount(), "Add income", () -> {
            User user = MenuManager.getLoggedInUser();
            System.out.println("How much money do you want to deposit?");
            int sum = scanner.nextInt();
            user.addToBalance(sum);
            cardsDataBase.executeWriteQuery("UPDATE card SET balance = " + user.getBalance() + " WHERE number = '" + user.getCardNumber() + "' AND pin = '" + user.getPIN() + "';");
            System.out.println();
        }));
        userPanel.addOption(new Option(userPanel.getOptionsCount(), "Do transfer", () -> {
            User user = MenuManager.getLoggedInUser();
            System.out.println("Enter destination card number:");
            String userCardNumInput = scanner.next();
            int[] card15 = new int[userCardNumInput.length() - 1];
            for (int i = 0; i < userCardNumInput.length() - 1; i++) {
                card15[i] = Character.getNumericValue(userCardNumInput.charAt(i));
            }
            // Check for errors
            if (userCardNumInput.equals(user.getCardNumber())) {
                System.out.println("You can't transfer money to the same account!\n");
                return;
            } else if (Character.getNumericValue(userCardNumInput.charAt(userCardNumInput.length() - 1)) != CardGenerator.LuhnAlgorithm(card15)) {
                System.out.println("Probably you made mistake in card number. Please try again!\n");
                return;
            }
            if (TYPE_SQL.equals("SQLITE")) {
                if (cardsDataBase.executeReadQuery("SELECT EXISTS(SELECT * FROM card WHERE number = '" + userCardNumInput + "') AS \"exists\";", "exists").equals("0")) {
                    System.out.println("Such a card does not exist.\n");
                    return;
                }
            } else {
                if (cardsDataBase.executeReadQuery("SELECT EXISTS(SELECT * FROM card WHERE number = '" + userCardNumInput + "') AS \"exists\";", "exists").equals("f")) {
                    System.out.println("Such a card does not exist.\n");
                    return;
                }
            }
            System.out.println("How much money do you want to transfer?");
            int userAmountInput = scanner.nextInt();
            if (userAmountInput <= user.getBalance()) {
                int newBalance = Integer.valueOf(cardsDataBase.executeReadQuery("SELECT balance FROM card WHERE number = '" + userCardNumInput + "';", "balance")) + userAmountInput;
                cardsDataBase.executeWriteQuery("UPDATE card SET balance = " + newBalance + " WHERE number = '" + userCardNumInput + "';");
                cardsDataBase.executeWriteQuery("UPDATE card SET balance = " + (user.getBalance() - userAmountInput) + " WHERE number = '" + user.getCardNumber() + "';");
                user.subFromBalance(userAmountInput);
                System.out.println("Success!\n");
            } else {
                System.out.println("Transaction failed: insufficient funds.\n");
            }
        }));
        userPanel.addOption(new Option(userPanel.getOptionsCount(), "Close account", () -> {
            User user = MenuManager.getLoggedInUser();
            cardsDataBase.executeWriteQuery("DELETE FROM card WHERE number = '" + user.getCardNumber() + "' AND pin = '" + user.getPIN() + "';");
            System.out.println("Account is closed\n");
            MenuManager.setLoggedInUser(null);
            curMenu = MenuManager.getMenu("MAIN_MENU");
        }));
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