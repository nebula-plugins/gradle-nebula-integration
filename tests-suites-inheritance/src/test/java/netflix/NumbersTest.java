package netflix;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NumbersTest {
    @Test
    public void isOdd_ShouldReturnTrueForOddNumbers() {
        Map items = ImmutableMap.of("coin", 3, "glass", 4, "pencil", 1);

        assertTrue(Numbers.isOdd(3));
    }
}
