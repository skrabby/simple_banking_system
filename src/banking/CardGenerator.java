package banking;

import java.util.Random;
import java.util.stream.IntStream;

public class CardGenerator {
    public static boolean isOccupied(String AI) {
        if (Main.getClientCards().get(AI) != null)
            return true;
        return false;
    }

    private static int LuhnAlgorithm(int[] generatedNum) {
        for (int i = 0; i < generatedNum.length; i++) {
            if (i % 2 == 0)
                generatedNum[i] *= 2;
            if (generatedNum[i] > 9)
                generatedNum[i] -= 9;
        }
        int sum = IntStream.of(generatedNum).sum();
        return 10 - sum % 10 == 10 ? 0 : 10 - sum % 10;
    }

    public static String cardNumber(String BIN) {
        String AccountIdentifier = "";
        int lenBIN = BIN.length();
        int[] generatedNum = new int[lenBIN + 9];
        for (int i = 0; i < lenBIN; i++)
            generatedNum[i] = Character.getNumericValue(BIN.charAt(i));
        for (int i = lenBIN; i < lenBIN + 9; i++)
            generatedNum[i] = new Random().nextInt(10);
        for (int num : generatedNum)
            AccountIdentifier += num;
        int checkSum = LuhnAlgorithm(generatedNum);
        if (isOccupied(AccountIdentifier + checkSum)) {
            return cardNumber(BIN);
        }
        return AccountIdentifier + checkSum;
    }

    public static String pinNumber() {
        String pin = "";
        for (int i = 0; i < 4; i++) {
            pin += new Random().nextInt(10);
        }
        return pin;
    }
}
