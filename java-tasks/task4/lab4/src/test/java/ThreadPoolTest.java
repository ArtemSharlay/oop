import org.junit.jupiter.api.Test;
import threadpool.ThreadPool;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadPoolTest {

    @Test
    public void testTaskExecution() throws Exception {

        ThreadPool pool = new ThreadPool(2);

        AtomicLong executed = new AtomicLong(0);

        pool.addTask(() -> executed.incrementAndGet());
        pool.addTask(() -> executed.incrementAndGet());

        Thread.sleep(500);

        assertTrue(executed.get() == 2);
    }
}