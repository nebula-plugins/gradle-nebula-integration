package netflix;

import org.junit.Test;


import java.util.Map;

import static org.junit.Assert.assertTrue;


public class NumbersIntegTest {
    @Test
    public void isOdd_ShouldReturnTrueForOddNumbers() {
        //Map items = ImmutableMap.of("coin", 3, "glass", 4, "pencil", 1);
        assertTrue(Numbers.isOdd(3));
    }
}
