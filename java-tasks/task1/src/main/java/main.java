import java.io.IOException;

/**
 * Главный класс приложения, содержащий точку входа в программу.
 * Этот класс запускает игру "Быки и коровы" путем создания экземпляра
 * класса Game и вызова метода StartGame()
 */
public class main {
    public static void main(String[] args) throws IOException {
        Game game1 = new Game();
        game1.StartGame();
    }
}
