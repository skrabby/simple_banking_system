package banking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Menu {
    private int optionsCount = 0;
    private List<Option> options = new ArrayList<>();

    Menu() { }

    public List<Option> getOptions() {
        return options;
    }

    Menu(List<Option> options) {
        this.options = options;
        this.optionsCount = options.size();
    }

    public void addOption(Option option) {
        option.setID(optionsCount);
        options.add(option);
        optionsCount++;
    }

    public void addOption(Option option, int ID) {
        option.setID(ID);
        options.add(option);
        optionsCount++;
    }

    public int getOptionsCount() {
        return optionsCount;
    }

    @Override
    public String toString() {
        if (options.size() == 0)
            return "";
        String s = "";
        for (int i = 1; i < options.size(); i++) {
            s += options.get(i).toString();
        }
        s += options.get(0).toString();
        return s;
    }
}

class MenuManager {
    private static Map<String, Menu> menuMap = new HashMap<>();
    private static User loggedInUser = null;

    public static Menu getMenu(String menu) {
        return menuMap.get(menu);
    }

    public static void addMenu(String name, Menu menu){
        menuMap.put(name, menu);
    }

    public static User getLoggedInUser() { return loggedInUser; }

    public static void setLoggedInUser(User loggedInUser) { MenuManager.loggedInUser = loggedInUser; }
}

