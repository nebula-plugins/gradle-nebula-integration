package netflix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NumbersTest {
    @Test
    public void isOdd_ShouldReturnTrueForOddNumbers() {
        assertTrue(Numbers.isOdd(3));
    }
}
