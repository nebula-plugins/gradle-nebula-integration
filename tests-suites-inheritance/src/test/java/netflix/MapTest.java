package netflix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MapTest {
    @Test
    public void isMap() {
        assertNotNull(MapUtils.get());
    }
}
