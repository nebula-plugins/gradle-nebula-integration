package nebula.test.testng;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


@Test(groups = "included")
public class ParallelParameterizationTest {

    @DataProvider(name = "numbers", parallel = true)
    public static Integer[] numbers() {
        return new Integer[]{1, 3, 5, 7, 9, 11, 13, -5, -3, 15, Integer.MAX_VALUE};
    }

    @Test(description = "Test for odd numbers", dataProvider = "numbers")
    void isOdd_ShouldReturnTrueForOddNumbers(Integer number) {
        Assert.assertTrue(Numbers.isOdd(number));
    }
}
