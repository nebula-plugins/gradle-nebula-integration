package nebula.test;


import org.junit.Test;

public class MyTest {

    @Test
    public void testSomething() {
        throw new RuntimeException(MyClass.doSomething());
    }
}
