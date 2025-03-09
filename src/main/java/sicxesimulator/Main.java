package sicxesimulator;

import javafx.application.Application;
import sicxesimulator.simulator.model.MainModel;
import sicxesimulator.simulator.view.MainApp;

public class Main {
    public static void main(String[] args) {
        MainModel model = new MainModel();  // Crie e configure seu Model aqui
        MainApp.setModel(model);           // Injete o Model na MainApp
        Application.launch(MainApp.class, args);
    }
}

