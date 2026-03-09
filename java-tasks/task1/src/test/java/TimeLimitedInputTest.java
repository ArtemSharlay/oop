import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;


class TimeLimitedInputTest {

    private TimeLimitedInput reader;
    private PipedInputStream piped_in;
    private PipedOutputStream piped_out;

    @BeforeEach
    void SetUp() throws IOException {
        piped_in = new PipedInputStream();
        piped_out = new PipedOutputStream(piped_in);
        reader = new TimeLimitedInput(piped_in);
    }

    @AfterEach
    void CloseUp() throws IOException {
        if (piped_out != null) {
            piped_out.close();
        }
        if (piped_in != null) {
            piped_in.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Тест чтения при успешном вводе")
    void TestReadWithTimeoutSuccess() throws IOException, InterruptedException {
        Thread inputThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                String test_input = "test input\n";
                piped_out.write(test_input.getBytes());
            } catch (Exception e) {
            }
        });
        inputThread.start();
        String result = reader.ReadWithTimeout();
        assertEquals("test input", result);
    }

    @Test
    @Order(2)
    @DisplayName("Тест таймаута при отсутствии ввода")
    void TestReadWithTimeoutExpiration() throws IOException,
            InterruptedException {
        long startTime = System.currentTimeMillis();
        String result = reader.ReadWithTimeout();
        long endTime = System.currentTimeMillis();

        assertNull(result);
        System.out.println(endTime - startTime);
        assertTrue(endTime - startTime >= 10000);
    }

    @Test
    @Order(3)
    @DisplayName("Тест чтения с прерыванием")
    void TestReadWithInterruption() {
        Thread.currentThread().interrupt();

        assertThrows(InterruptedException.class, () -> {
            reader.ReadWithTimeout();
        });

        Thread.interrupted();
    }

    @Test
    @Order(4)
    @DisplayName("Тест конструктора с null InputStream")
    void TestConstructorWithNullInputStream() {
        assertThrows(NullPointerException.class,
                () -> new TimeLimitedInput(null));
    }

}