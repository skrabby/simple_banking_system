package banking;

public class User {
    private String cardNumber;
    private String PIN;
    private long balance = 0;

    User(String cardNumber, String PIN) {
        this.cardNumber = cardNumber;
        this.PIN = PIN;
    }

    public String getPIN() { return PIN; }

    public long getBalance() { return balance; }
}
