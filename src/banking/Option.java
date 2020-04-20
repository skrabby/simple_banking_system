package banking;

import java.sql.SQLException;

interface Action {
    void activate() throws SQLException;
}

public class Option{
    private int ID;
    private String description;
    private Action action;

    Option(int ID, String description, Action action) {
        this.ID = ID;
        this.description = description;
        this.action = action;
    }

    public void invokeAction() throws SQLException {
        action.activate();
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return ID + ". " + description + "\n";
    }
}
