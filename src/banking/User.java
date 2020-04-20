package banking;

public class User {
    private String id;
    private String cardNumber;
    private String PIN;
    private long balance = 0;

    User(String id, String cardNumber, String PIN, long balance) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.PIN = PIN;
        this.balance = balance;
    }

    public String getPIN() {
        return PIN;
    }

    public long getBalance() {
        return balance;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void addToBalance(long sum) {
        balance += sum;
    }

    public void subFromBalance(long sum) {
        balance -= sum;
    }
}
