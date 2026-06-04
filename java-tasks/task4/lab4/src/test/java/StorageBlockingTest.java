import model.Body;
import org.junit.jupiter.api.Test;
import storage.Storage;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class StorageBlockingTest {

    @Test
    public void testBlockingWhenEmpty() throws Exception {

        Storage<Body> storage = new Storage<>(1);

        AtomicBoolean took = new AtomicBoolean(false);

        Thread t = new Thread(() -> {
            try {
                storage.take();
                took.set(true);
            } catch (Exception ignored) {}
        });

        t.start();

        Thread.sleep(300);
        assertFalse(took.get());

        storage.put(new Body());

        Thread.sleep(300);

        assertTrue(took.get());
    }
}