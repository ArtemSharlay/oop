/**
 * Класс, представляющий результат попытки угадать слово в игре "Быки и коровы".
 */

public class GameResult {

    /** Количество быков (букв на правильных позициях) */
    private int bulls;
    /** Количество коров (букв, присутствующих в слове, но не на своих местах) */
    private int cows;

    /**
     * Создает новый объект результата игры.
     *
     * @param bulls количество быков
     * @param cows количество коров
     */
    public GameResult(int bulls,int cows){
        if (bulls < 0 || cows < 0) {
            throw new RuntimeException();
        }

        this.bulls = bulls;
        this.cows = cows;
    }

    /**
     * Возвращает количество быков
     * @return количество быков
     */
    public int GetBulls(){
        return bulls;
    }
    /**
     * Возвращает количество коров
     * @return количество коров
     */
    public int GetCows(){
        return cows;
    }

    /**
     * Выводит результат в консоль(количество быков и коров)
     */
    public void PrintResult(){
        System.out.println("bulls:"+bulls +" cows:" + cows);
    }
}
