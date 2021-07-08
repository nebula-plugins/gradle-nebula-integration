package nebula.test.testng;

import org.testng.Assert;
import org.testng.annotations.*;

@Test
public class NonParallelParameterizationTest {
    @BeforeSuite
    public static void test() throws Exception {
        System.out.println("BeforeSuite");
    }

    @BeforeClass
    public void setUpClass() {
        System.out.println("BeforeClass");
    }

    @BeforeMethod
    public void setUpMethod() {
        System.out.println("Before Method");
    }

    @DataProvider(name = "numbers", indices = {0,1})
    public static Integer[] numbers() {
        return new Integer[]{1, 3, 5, 7, 9, 11, 13, -5, -3, 15, Integer.MAX_VALUE};
    }

    @Test(description = "Test for odd numbers", dataProvider = "numbers")
    void isOdd_ShouldReturnTrueForOddNumbers(Integer number) {
        Assert.assertTrue(Numbers.isOdd(number));
    }
}
