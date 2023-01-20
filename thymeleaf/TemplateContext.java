package thymeleaf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateContext {
    Map<String, String> welcomeMessages = new HashMap<>();
    Map<String, Object[]> classes = new HashMap<>();
    public void put(String name, WelcomeMessage welcomeMessage) {
        welcomeMessages.put(name, welcomeMessage.getMessage());
    }
    public void put(String name, Object[] receivedClasses) {
            classes.put(name, receivedClasses);
    }
}
