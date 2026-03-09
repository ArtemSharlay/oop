import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GameResultTest {

    @Test
    @Order(1)
    @DisplayName("Тест конструктора ")
    void TestConstructorWithValidWord() {
        GameResult result = new GameResult(1,1);
        assertNotNull(result);
        assertEquals(1,result.GetBulls());
        assertEquals(1,result.GetCows());
    }

}
