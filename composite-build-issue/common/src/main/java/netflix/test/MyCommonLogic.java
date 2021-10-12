package netflix.test;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class MyCommonLogic {
    private static final Map<String, Integer> items = ImmutableMap.of("coin", 3, "glass", 4, "pencil", 1);

    public static Map<String, Integer> getItems() {
        return items;
    }
}
