package netflix;
import com.google.common.collect.ImmutableMap;

public class MapUtils {
    public static ImmutableMap<String, Integer> get() {
        return ImmutableMap.of("coin", 3, "glass", 4, "pencil", 1);
    }
}
