import org.junit.jupiter.api.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class FileLoggerTest {
    private FileLogger logger;

    @BeforeEach
    void setUp() throws IOException {
        logger = FileLogger.getInstance();
    }

    @Test
    @DisplayName("Тест синглтон")
    void TestSingleton() {
        FileLogger instance1 = FileLogger.getInstance();
        FileLogger instance2 = FileLogger.getInstance();

        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Тест логирования информационного сообщения")
    void TestInfoLogging() {
        assertDoesNotThrow(() -> logger.info("Test info message"));
    }

    @Test
    @DisplayName("Тест логирования сообщения об ошибке")
    void TestErrorLogging() {
        Exception testException = new RuntimeException("Test exception");
        assertDoesNotThrow(() -> logger.error("Test error message", testException));
    }

    @Test
    @DisplayName("Тест закрытия логгера")
    void TestClose() {
        assertDoesNotThrow(() -> logger.close());
        assertDoesNotThrow(() -> logger.close());
    }

}