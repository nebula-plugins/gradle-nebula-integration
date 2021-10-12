package netflix.test;

public class MyClient {
    public static void main(String[] args) {
        MyCommonLogic.getItems().forEach((s, integer) -> System.out.println(s));
    }
}
