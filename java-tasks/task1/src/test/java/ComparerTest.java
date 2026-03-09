import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class ComparerTest {

    private Comparer comparer;
    private FileLogger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = FileLogger.getInstance();
    }

    @AfterEach
    void CloseUp(){
        mockLogger.close();
    }

    @Test
    @Order(1)
    @DisplayName("Тест конструктора с корректным словом")
    void testConstructorWithValidWord() {
        comparer = new Comparer("1234");
        assertNotNull(comparer);
    }

    @Test
    @Order(2)
    @DisplayName("Тест конструктора с null словом")
    void testConstructorWithNullWord() {
        assertThrows(NullPointerException.class, () -> new Comparer(null));
    }

    @ParameterizedTest
    @MethodSource("provideWordsForComparison")
    @Order(3)
    @DisplayName("Параметризованный тест обработки попыток")
    void testProcessWordTry(String secret, String guess, int expectedBulls, int expectedCows) {
        comparer = new Comparer(secret);
        GameResult result = comparer.ProccessWordTry(guess, mockLogger);

        assertEquals(expectedBulls, result.GetBulls());
        assertEquals(expectedCows, result.GetCows());

    }

    private static Stream<Arguments> provideWordsForComparison() {
        return Stream.of(
                // secret, guess, bulls, cows
                Arguments.of("1234", "1234", 4, 0),
                Arguments.of("1234", "1243", 2, 2),
                Arguments.of("1234", "5678", 0, 0),
                Arguments.of("1234", "1212", 2, 0),
                Arguments.of("1122", "1212", 2, 2),
                Arguments.of("1234", "4321", 0, 4)
        );
    }



}