import model.Body;
import org.junit.jupiter.api.Test;
import storage.Storage;

import static org.junit.jupiter.api.Assertions.*;

public class StorageTest {

    @Test
    public void testPutAndTake() throws Exception {

        Storage<Body> storage = new Storage<>(2);

        Body b1 = new Body();
        Body b2 = new Body();

        storage.put(b1);
        storage.put(b2);

        assertEquals(2, storage.getSize());

        Body result1 = storage.take();
        Body result2 = storage.take();

        assertEquals(b1, result1);
        assertEquals(b2, result2);
    }
}