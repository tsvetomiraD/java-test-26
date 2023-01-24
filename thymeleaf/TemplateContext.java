package thymeleaf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateContext {
    Map<String, Object> classes = new HashMap<>();
    public void put(String name, Object receivedClasse) {
        classes.put(name, receivedClasse);
    }
}
