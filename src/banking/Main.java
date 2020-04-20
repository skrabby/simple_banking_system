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
            if (args.length == 2) {
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
                                "id INT PRIMARY KEY,\n"
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