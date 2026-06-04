package com.example;
import controller.GameController;
import model.GameModel;
import view.ConsoleView;
import view.GameWindow;


public class MainApplication {

    public static void main(String[] args) {

        GameModel model = new GameModel();


        new Thread(() -> {
            ConsoleView consoleView = new ConsoleView(model);
            consoleView.showMenu();
        }).start();


        GameWindow frame = new GameWindow(model);
        new GameController(model, frame.getPanel());

    }
}