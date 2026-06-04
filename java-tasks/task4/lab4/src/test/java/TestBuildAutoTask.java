import model.Accessory;
import model.Auto;
import model.Body;
import model.Motor;
import org.junit.jupiter.api.Test;
import storage.Storage;
import task.BuildAutoTask;

import static org.junit.jupiter.api.Assertions.*;

public class TestBuildAutoTask {

    @Test
    public void testBuildAuto() throws Exception {

        Storage<Body> bodyStorage = new Storage<>(10);
        Storage<Motor> motorStorage = new Storage<>(10);
        Storage<Accessory> accessoryStorage = new Storage<>(10);
        Storage<Auto> autoStorage = new Storage<>(10);

        bodyStorage.put(new Body());
        motorStorage.put(new Motor());
        accessoryStorage.put(new Accessory());

        BuildAutoTask task =
                new BuildAutoTask(
                        bodyStorage,
                        motorStorage,
                        accessoryStorage,
                        autoStorage
                );

        task.execute();

        assertEquals(1, autoStorage.getSize());
    }
}