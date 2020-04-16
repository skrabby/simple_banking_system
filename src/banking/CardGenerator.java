package banking;

import java.util.Random;

public class CardGenerator {
    private static String checkSum = "5";

    public static boolean isOccupied(String AI) {
        if (Main.getClientCards().get(AI) != null)
            return true;
        return false;
    }

    public static String cardNumber(String BIN) {
        String AccountIdentifier = "";
        for (int i = 0; i < 9; i++) {
            AccountIdentifier += new Random().nextInt(10);
        }
        if (isOccupied(BIN + AccountIdentifier + checkSum)) {
            return cardNumber(BIN);
        }
        return BIN + AccountIdentifier + checkSum;
    }

    public static String pinNumber() {
        String pin = "";
        for (int i = 0; i < 4; i++) {
            pin += new Random().nextInt(10);
        }
        return pin;
    }
}
