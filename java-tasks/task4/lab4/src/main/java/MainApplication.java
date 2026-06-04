import config.Config;
import controller.AutoStorageController;
import dealer.Dealer;
import factory.Factory;
import gui.FactoryFrame;
import model.Accessory;
import model.Auto;
import model.Body;
import model.Motor;
import storage.Storage;
import supplier.AccessorySupplier;
import supplier.BodySupplier;
import supplier.MotorSupplier;
import threadpool.ThreadPool;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class MainApplication {
    public static void main(String[] args) {

        Factory factory = new Factory();

        SwingUtilities.invokeLater(() -> {

            FactoryFrame frame =
                    new FactoryFrame(factory);

            frame.setVisible(true);
        });
    }
}
