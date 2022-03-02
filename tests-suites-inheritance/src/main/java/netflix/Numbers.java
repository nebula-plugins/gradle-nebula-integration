package netflix;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class Numbers {
    public static boolean isOdd(int number) {
        Map items = ImmutableMap.of("coin", 3, "glass", 4, "pencil", 1);

        items.entrySet()
                .stream()
                .forEach(System.out::println);
        return number % 2 != 0;
    }
}
